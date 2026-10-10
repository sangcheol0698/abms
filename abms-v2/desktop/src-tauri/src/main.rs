// 릴리스 빌드에서 Windows 콘솔 창이 함께 뜨지 않게 한다.
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

fn main() {
    abms_desktop_lib::run()
}
