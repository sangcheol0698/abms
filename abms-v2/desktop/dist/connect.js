const { invoke } = window.__TAURI__.core;

const status = document.getElementById('status');
const detail = document.getElementById('detail');
const retry = document.getElementById('retry');

async function connect() {
    status.textContent = '서버에 연결하는 중입니다…';
    detail.hidden = true;
    retry.disabled = true;
    try {
        location.replace(await invoke('connect'));
    } catch (error) {
        status.textContent = '서버에 연결할 수 없습니다.';
        detail.textContent = String(error);
        detail.hidden = false;
        retry.hidden = false;
        retry.disabled = false;
    }
}

retry.addEventListener('click', connect);

invoke('server_address').then((address) => {
    document.getElementById('address').textContent = address;
});

connect();
