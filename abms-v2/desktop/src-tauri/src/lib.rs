use std::collections::HashMap;
use std::net::{TcpStream, ToSocketAddrs};
use std::path::{Path, PathBuf};
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::{Arc, Mutex};
use std::time::Duration;

use tauri::webview::{DownloadEvent, NewWindowResponse};
use tauri::{AppHandle, Manager, State, WebviewUrl, WebviewWindowBuilder};
use tauri_plugin_opener::OpenerExt;
use url::Url;

const DEFAULT_SERVER_URL: &str = "http://localhost:8080";
const CONNECT_TIMEOUT: Duration = Duration::from_secs(3);

/// 앱이 띄울 ABMS 서버 주소. 실행 시 환경 변수 → 빌드 시 환경 변수 → 로컬 기본값 순으로 정한다.
struct Server(Url);

/// 진행 중인 다운로드의 저장 경로. macOS는 완료 이벤트에 경로를 주지 않아 요청 시점에 기억해 둔다.
#[derive(Default, Clone)]
struct Downloads(Arc<Mutex<HashMap<Url, PathBuf>>>);

static POPUP_SEQ: AtomicUsize = AtomicUsize::new(0);

/// macOS WKWebView에는 브라우저의 뒤로·앞으로·새로고침 조작이 없어 페이지마다 붙인다. Windows WebView2는 기본으로 지원한다.
#[cfg(target_os = "macos")]
const HISTORY_CONTROLS: &str = include_str!("history.js");

fn server_url() -> Url {
    std::env::var("ABMS_SERVER_URL")
        .ok()
        .or_else(|| option_env!("ABMS_SERVER_URL").map(String::from))
        .and_then(|value| Url::parse(&value).ok())
        .unwrap_or_else(|| Url::parse(DEFAULT_SERVER_URL).expect("기본 서버 주소가 올바르지 않습니다"))
}

fn is_server(url: &Url, server: &Url) -> bool {
    url.origin() == server.origin()
}

/// 서버에 연결하기 전에 보여주는 앱 내장 페이지(dist/index.html)인지 확인한다.
/// `tauri dev`는 dist/를 내장 개발 서버(devUrl)에서 띄우므로 그 출처도 내장 페이지로 본다.
fn is_bundled_page(url: &Url, dev_url: Option<&Url>) -> bool {
    url.scheme() == "tauri"
        || url.host_str() == Some("tauri.localhost")
        || url.scheme() == "about"
        || dev_url.is_some_and(|dev| dev.origin() == url.origin())
}

fn reachable(server: &Url) -> Result<(), String> {
    let host = server.host_str().ok_or("서버 주소에 호스트가 없습니다")?;
    let port = server.port_or_known_default().ok_or("서버 주소에 포트가 없습니다")?;
    let addrs = (host, port).to_socket_addrs().map_err(|e| format!("{host} 주소를 찾지 못했습니다 ({e})"))?;
    let mut last_error = format!("{host}:{port}에 연결하지 못했습니다");
    for addr in addrs {
        match TcpStream::connect_timeout(&addr, CONNECT_TIMEOUT) {
            Ok(_) => return Ok(()),
            Err(e) => last_error = format!("{host}:{port}에 연결하지 못했습니다 ({e})"),
        }
    }
    Err(last_error)
}

/// 내장 페이지가 호출한다. 서버에 닿으면 서버 주소를, 아니면 사유를 돌려준다.
/// 이동은 내장 페이지가 `location.replace`로 해서 뒤로 가기로 연결 화면에 돌아오지 않게 한다.
#[tauri::command]
async fn connect(server: State<'_, Server>) -> Result<String, String> {
    let url = server.0.clone();
    let target = url.clone();
    tauri::async_runtime::spawn_blocking(move || reachable(&target))
        .await
        .map_err(|e| e.to_string())??;
    Ok(url.to_string())
}

#[tauri::command]
fn server_address(server: State<'_, Server>) -> String {
    server.0.to_string()
}

/// 이미 있는 이름이면 `이름 (1).확장자`처럼 번호를 붙여 덮어쓰지 않게 한다.
fn unique_path(dir: &Path, file_name: &str) -> PathBuf {
    let candidate = dir.join(file_name);
    if !candidate.exists() {
        return candidate;
    }
    let path = Path::new(file_name);
    let stem = path.file_stem().and_then(|s| s.to_str()).unwrap_or("download");
    let ext = path.extension().and_then(|s| s.to_str());
    (1..)
        .map(|n| match ext {
            Some(ext) => dir.join(format!("{stem} ({n}).{ext}")),
            None => dir.join(format!("{stem} ({n})")),
        })
        .find(|p| !p.exists())
        .expect("사용할 수 있는 파일 이름이 없습니다")
}

/// 서버 화면의 스낵바(window.abmsToast)로 결과를 알린다.
fn toast(webview: &tauri::Webview, kind: &str, message: &str) {
    let script = format!(
        "window.abmsToast && window.abmsToast({}, {});",
        serde_json::to_string(kind).unwrap_or_default(),
        serde_json::to_string(message).unwrap_or_default()
    );
    let _ = webview.eval(script);
}

fn file_name_of(path: &Path) -> String {
    path.file_name().map(|n| n.to_string_lossy().into_owned()).unwrap_or_default()
}

/// macOS 메인 창은 제목 표시줄 없이 서버 화면의 상단 바(48px)가 창 맨 위에 붙는다.
/// 서버 화면은 html[data-shell="macos"]를 보고 창 버튼 자리를 비우고 뒤로·앞으로 버튼을 보인다(app.css).
#[cfg(target_os = "macos")]
const MAC_SHELL_MARKER: &str = r#"
(() => {
  const mark = () => document.documentElement && (document.documentElement.dataset.shell = 'macos');
  if (!mark()) new MutationObserver((_, observer) => { if (mark()) observer.disconnect(); }).observe(document, { childList: true });
})();
"#;

/// 서버 화면에는 Tauri API를 열지 않지만, 제목 표시줄이 없는 창을 상단 바로 옮길 수 있도록 창 끌기·확대만 허용한다.
#[cfg(target_os = "macos")]
fn allow_window_drag(app: &AppHandle) -> tauri::Result<()> {
    let server = &app.state::<Server>().0;
    app.add_capability(
        tauri::ipc::CapabilityBuilder::new("server-window-drag")
            .remote(format!("{}/*", server.origin().ascii_serialization()))
            .window("main")
            .permission("core:window:allow-start-dragging")
            .permission("core:window:allow-internal-toggle-maximize"),
    )
}

/// 트랙패드에 손가락이 닿고 떨어지는 순간을 화면 스크립트(history.js)에 알린다.
/// 웹 화면의 wheel 이벤트로는 손을 뗐는지와 관성 스크롤을 구분할 수 없어, Chrome처럼 macOS 이벤트의 phase로 판단한다.
#[cfg(target_os = "macos")]
fn watch_trackpad_touch(app: &AppHandle) {
    use objc2_app_kit::{NSEvent, NSEventMask, NSEventPhase};
    use std::ptr::NonNull;

    let app = app.clone();
    let handler = block2::RcBlock::new(move |event: NonNull<NSEvent>| -> *mut NSEvent {
        let phase = unsafe { event.as_ref() }.phase();
        let touch = if phase.contains(NSEventPhase::Began) {
            Some("down")
        } else if phase.intersects(NSEventPhase::Ended | NSEventPhase::Cancelled) {
            Some("up")
        } else {
            None
        };
        if let Some(touch) = touch {
            for window in app.webview_windows().values() {
                let _ = window.eval(format!("window.__abmsTrackpad && window.__abmsTrackpad('{touch}');"));
            }
        }
        event.as_ptr()
    });
    // 앱이 끝날 때까지 감시하므로 해제하지 않는다.
    let monitor = unsafe { NSEvent::addLocalMonitorForEventsMatchingMask_handler(NSEventMask::ScrollWheel, &handler) };
    std::mem::forget(monitor);
}

/// 메인 창과 새 창이 같은 규칙(서버 밖 주소는 기본 브라우저, 다운로드는 다운로드 폴더)을 따르도록 공통 설정을 붙인다.
fn configure<'a>(
    app: &'a AppHandle,
    builder: WebviewWindowBuilder<'a, tauri::Wry, AppHandle>,
) -> WebviewWindowBuilder<'a, tauri::Wry, AppHandle> {
    let server = app.state::<Server>().0.clone();
    let downloads = app.state::<Downloads>().inner().clone();

    let nav_app = app.clone();
    let nav_server = server.clone();
    let popup_app = app.clone();
    let popup_server = server;
    let download_app = app.clone();
    let dev_url = if tauri::is_dev() { app.config().build.dev_url.clone() } else { None };

    #[cfg(target_os = "macos")]
    let builder = builder.initialization_script(HISTORY_CONTROLS);

    builder
        .on_navigation(move |url| {
            if is_server(url, &nav_server) || is_bundled_page(url, dev_url.as_ref()) {
                return true;
            }
            let _ = nav_app.opener().open_url(url.as_str(), None::<&str>);
            false
        })
        .on_new_window(move |url, features| {
            if !is_server(&url, &popup_server) {
                let _ = popup_app.opener().open_url(url.as_str(), None::<&str>);
                return NewWindowResponse::Deny;
            }
            // 첨부 미리보기처럼 서버 안의 새 창은 로그인 세션을 이어받도록 앱 창으로 연다.
            let label = format!("popup-{}", POPUP_SEQ.fetch_add(1, Ordering::Relaxed));
            let builder = WebviewWindowBuilder::new(&popup_app, label, WebviewUrl::External("about:blank".parse().unwrap()))
                .window_features(features)
                .title("ABMS")
                .on_document_title_changed(|window, title| {
                    let _ = window.set_title(&title);
                });
            match configure(&popup_app, builder).build() {
                Ok(window) => NewWindowResponse::Create { window },
                Err(_) => NewWindowResponse::Deny,
            }
        })
        .on_download(move |webview, event| match event {
            DownloadEvent::Requested { url, destination } => {
                let Ok(dir) = download_app.path().download_dir() else {
                    return false;
                };
                let suggested = file_name_of(destination);
                let name = if suggested.is_empty() { "download".to_string() } else { suggested };
                *destination = unique_path(&dir, &name);
                downloads.0.lock().unwrap().insert(url, destination.clone());
                true
            }
            DownloadEvent::Finished { url, success, .. } => {
                let path = downloads.0.lock().unwrap().remove(&url);
                let name = path.as_deref().map(file_name_of).unwrap_or_default();
                if success {
                    toast(&webview, "success", &format!("다운로드 폴더에 저장했습니다: {name}"));
                } else {
                    toast(&webview, "error", &format!("파일을 내려받지 못했습니다: {name}"));
                }
                true
            }
            _ => true,
        })
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_single_instance::init(|app, _args, _cwd| {
            if let Some(window) = app.get_webview_window("main") {
                let _ = window.unminimize();
                let _ = window.show();
                let _ = window.set_focus();
            }
        }))
        .plugin(tauri_plugin_window_state::Builder::default().build())
        .plugin(tauri_plugin_opener::init())
        .manage(Server(server_url()))
        .manage(Downloads::default())
        .invoke_handler(tauri::generate_handler![connect, server_address])
        .setup(|app| {
            let handle = app.handle();
            let builder = WebviewWindowBuilder::new(handle, "main", WebviewUrl::App("index.html".into()))
                .title("ABMS")
                .inner_size(1440.0, 900.0)
                .min_inner_size(1024.0, 640.0);
            #[cfg(target_os = "macos")]
            let builder = builder
                .title_bar_style(tauri::TitleBarStyle::Overlay)
                .hidden_title(true)
                .traffic_light_position(tauri::LogicalPosition::new(18.0, 24.0))
                .initialization_script(MAC_SHELL_MARKER);
            configure(handle, builder).build()?;
            #[cfg(target_os = "macos")]
            {
                allow_window_drag(handle)?;
                watch_trackpad_touch(handle);
            }
            Ok(())
        })
        .run(tauri::generate_context!())
        .expect("ABMS 데스크톱 앱을 시작하지 못했습니다");
}

#[cfg(test)]
mod tests {
    use super::*;

    fn url(value: &str) -> Url {
        Url::parse(value).unwrap()
    }

    #[test]
    fn 서버와_출처가_같을_때만_앱_안에서_연다() {
        let server = url("https://abms.example.com");
        assert!(is_server(&url("https://abms.example.com/projects?page=2"), &server));
        assert!(!is_server(&url("http://abms.example.com/"), &server));
        assert!(!is_server(&url("https://map.kakao.com/link/map/1"), &server));
        assert!(!is_server(&url("https://abms.example.com:8443/"), &server));
    }

    #[test]
    fn 내장_연결_페이지는_플랫폼별_주소_모두_허용한다() {
        assert!(is_bundled_page(&url("tauri://localhost"), None));
        assert!(is_bundled_page(&url("http://tauri.localhost/index.html"), None));
        assert!(!is_bundled_page(&url("https://example.com"), None));
    }

    #[test]
    fn 개발_모드에서는_내장_개발_서버_주소도_내장_페이지로_본다() {
        let dev_url = url("http://127.0.0.1:1430");
        assert!(is_bundled_page(&url("http://127.0.0.1:1430/index.html"), Some(&dev_url)));
        assert!(!is_bundled_page(&url("http://127.0.0.1:1430/"), None));
        assert!(!is_bundled_page(&url("http://127.0.0.1:8080/"), Some(&dev_url)));
    }

    #[test]
    fn 같은_이름의_파일이_있으면_번호를_붙인다() {
        let dir = std::env::temp_dir().join(format!("abms-desktop-test-{}", std::process::id()));
        std::fs::create_dir_all(&dir).unwrap();
        assert_eq!(unique_path(&dir, "직원.csv"), dir.join("직원.csv"));

        std::fs::write(dir.join("직원.csv"), "").unwrap();
        std::fs::write(dir.join("직원 (1).csv"), "").unwrap();
        assert_eq!(unique_path(&dir, "직원.csv"), dir.join("직원 (2).csv"));

        std::fs::write(dir.join("README"), "").unwrap();
        assert_eq!(unique_path(&dir, "README"), dir.join("README (1)"));
        std::fs::remove_dir_all(&dir).unwrap();
    }

    #[test]
    fn 연결할_수_없는_서버는_사유를_돌려준다() {
        let error = reachable(&url("http://127.0.0.1:9")).unwrap_err();
        assert!(error.contains("127.0.0.1:9"));
    }
}
