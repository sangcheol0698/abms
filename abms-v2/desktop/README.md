# ABMS 데스크톱

ABMS 서버 화면을 그대로 띄우는 macOS·Windows 앱입니다. [Tauri 2](https://v2.tauri.app)로 만들었고, 화면과 업무 로직은 모두 서버에 있습니다. 서버를 배포하면 앱을 다시 설치하지 않아도 바로 반영됩니다.

## 동작

| 상황 | 처리 |
|---|---|
| 실행 | 내장 연결 화면(`dist/`)에서 서버 접속을 확인한 뒤 서버 화면으로 이동. 접속하지 못하면 사유와 `다시 연결` 버튼 표시 |
| 서버 안의 링크 | 앱 창 안에서 이동 |
| 서버 밖 링크 (카카오맵, 협력사 웹사이트 등) | 기본 브라우저로 열기 |
| 서버 안의 새 창 (첨부 미리보기) | 로그인 세션을 이어받는 앱 창으로 열기 |
| 파일 내려받기 (CSV, 첨부) | 다운로드 폴더에 저장. 같은 이름이 있으면 `이름 (1).csv`처럼 번호를 붙이고 결과는 스낵바로 알림 |
| 뒤로·앞으로 | macOS: 트랙패드 두 손가락 가로 스와이프(Chrome처럼 화면 가장자리에서 화살표가 따라 나오고, 끝까지 밀어 파랗게 된 상태에서 손을 떼면 이동. 떼기 전에 되돌리면 취소. 가로 스크롤 표 안에서는 스크롤 우선), `⌘[` `⌘]`, 입력 중이 아닐 때 `⌘←` `⌘→`, 마우스 뒤로·앞으로 버튼. Windows: WebView2 기본(`Alt+←` `Alt+→`, 마우스 버튼). 연결 화면은 기록에 남기지 않아 뒤로 가도 돌아오지 않음 |
| 새로고침 | macOS: `⌘R`. Windows: WebView2 기본(`Ctrl+R`, `F5`) |
| 창 모양 (macOS) | 제목 표시줄 없이 서버 상단 바가 창 맨 위에 붙고, 창 버튼 옆에 사이드바 토글·뒤로·앞으로 버튼 표시 (서버 `html[data-shell="macos"]`) |
| 창 크기·위치 | 다음 실행 때 복원 |
| 중복 실행 | 이미 떠 있는 창을 앞으로 가져옴 |

서버에서 받은 화면에는 Tauri API(IPC)를 열지 않습니다. 예외로 macOS 메인 창은 제목 표시줄이 없어 상단 바를 끌어 창을 옮길 수 있도록 창 끌기·확대(`core:window:allow-start-dragging`, `allow-internal-toggle-maximize`)만 서버 주소에 허용합니다(`allow_window_drag`). `capabilities/default.json`은 내장 연결 화면에만 적용됩니다.

## 서버 주소

아래 순서로 정합니다.

1. 실행 시 환경 변수 `ABMS_SERVER_URL`
2. 빌드 시 환경 변수 `ABMS_SERVER_URL` (배포 빌드에 운영 주소를 넣을 때 사용)
3. 기본값 `http://localhost:8080`

## 준비

- Node.js, [Rust](https://rustup.rs)
- macOS: Xcode Command Line Tools
- Windows: Microsoft C++ Build Tools. WebView2는 Windows 10/11에 기본 포함되어 있고, 없으면 설치 프로그램이 내려받습니다.

## 실행과 빌드

```bash
cd abms-v2/desktop
npm install

# 개발 실행 (서버는 abms-v2에서 ./gradlew bootRun 으로 먼저 띄운다)
npm run dev

# 배포 빌드
ABMS_SERVER_URL=https://abms.example.com npm run build
```

결과물은 `src-tauri/target/release/bundle/`에 생깁니다.

| OS | 결과물 |
|---|---|
| macOS | `macos/ABMS.app`, `dmg/ABMS_<버전>_<아키텍처>.dmg` |
| Windows | `nsis/ABMS_<버전>_x64-setup.exe`, `msi/ABMS_<버전>_x64_en-US.msi` |

Windows 설치 파일은 Windows에서 빌드해야 합니다.

## 테스트

```bash
cd src-tauri && cargo test
```

## 배포 전에 필요한 것

- **macOS 서명·공증**: Apple Developer ID 인증서가 없으면 다른 Mac에서 Gatekeeper가 실행을 막습니다.
- **Windows 코드 서명**: 서명하지 않으면 SmartScreen 경고가 뜹니다.
- **자동 업데이트**: `tauri-plugin-updater`를 쓰려면 업데이트 서명 키와 `latest.json`을 올려 둘 주소가 필요합니다.
