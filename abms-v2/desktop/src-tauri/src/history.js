// macOS WKWebView에는 브라우저의 뒤로·앞으로·새로고침 조작이 없어 모든 페이지에 붙인다.
// - ⌘[ ⌘], 입력 중이 아닐 때 ⌘← ⌘→, 마우스 뒤로·앞으로 버튼, ⌘R
// - 트랙패드 두 손가락 가로 스와이프: Chrome처럼 화면 가장자리에서 화살표가 따라 나오고,
//   끝까지 밀어 파랗게 된 상태에서 손을 떼면 이동한다. 손을 떼기 전에 되돌리면 취소된다.
//   손가락이 닿고 떨어지는 순간은 앱(lib.rs의 watch_trackpad_touch)이 window.__abmsTrackpad로 알려 준다.
(() => {
  const editing = (target) => target instanceof HTMLElement
    && (target.isContentEditable || /^(INPUT|TEXTAREA|SELECT)$/.test(target.tagName));

  window.addEventListener('keydown', (event) => {
    if (!event.metaKey || event.altKey || event.ctrlKey || event.shiftKey || event.defaultPrevented) return;
    if (event.key === 'r' || event.key === 'R') {
      event.preventDefault();
      location.reload();
      return;
    }
    const arrow = event.key === 'ArrowLeft' || event.key === 'ArrowRight';
    if (arrow && editing(event.target)) return;
    if (event.key === '[' || event.key === 'ArrowLeft') history.back();
    else if (event.key === ']' || event.key === 'ArrowRight') history.forward();
    else return;
    event.preventDefault();
  }, true);

  window.addEventListener('mouseup', (event) => {
    if (event.button === 3) history.back();
    else if (event.button === 4) history.forward();
    else return;
    event.preventDefault();
  }, true);

  const THRESHOLD = 80;
  const SIZE = 40;
  const TRAVEL = 24;
  // 손 뗌 신호를 받지 못했을 때(창이 포커스를 잃는 등) 화살표가 남지 않도록 이동 없이 정리한다.
  const STALE_MS = 4000;

  let touching = false;
  let pulled = 0;
  let staleTimer = 0;
  let indicator = null;

  const canGo = (back) => {
    const nav = window.navigation;
    if (nav && 'canGoBack' in nav) return back ? nav.canGoBack : nav.canGoForward;
    return back ? history.length > 1 : true;
  };

  // 가로로 스크롤할 수 있는 표·목록 안에서는 스와이프보다 스크롤을 우선한다.
  const scrollsHorizontally = (target, deltaX) => {
    for (let el = target instanceof Element ? target : null; el; el = el.parentElement) {
      if (el.scrollWidth <= el.clientWidth) continue;
      const overflow = getComputedStyle(el).overflowX;
      if (overflow !== 'auto' && overflow !== 'scroll' && el !== document.scrollingElement) continue;
      if (deltaX < 0 ? el.scrollLeft > 0 : el.scrollLeft + el.clientWidth < el.scrollWidth - 1) return true;
    }
    return false;
  };

  const armed = () => Math.abs(pulled) >= THRESHOLD;

  const show = () => {
    if (!indicator) {
      indicator = document.createElement('div');
      indicator.setAttribute('aria-hidden', 'true');
      indicator.innerHTML = '<svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor"'
        + ' stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>';
      Object.assign(indicator.style, {
        position: 'fixed', top: '50%', zIndex: '2147483647', width: `${SIZE}px`, height: `${SIZE}px`,
        marginTop: `${-SIZE / 2}px`, borderRadius: '50%', display: 'flex', alignItems: 'center',
        justifyContent: 'center', pointerEvents: 'none', boxShadow: '0 1px 4px rgba(0,0,0,.3)',
      });
    }
    const back = pulled < 0;
    const progress = Math.min(Math.abs(pulled) / THRESHOLD, 1);
    const offset = `${progress * TRAVEL}px`;
    Object.assign(indicator.style, {
      left: back ? offset : 'auto',
      right: back ? 'auto' : offset,
      opacity: String(0.4 + progress * 0.6),
      transform: back ? 'none' : 'scaleX(-1)',
      backgroundColor: armed() ? '#1a73e8' : '#fff',
      color: armed() ? '#fff' : '#5f6368',
      transition: 'background-color .15s, color .15s',
    });
    if (!indicator.isConnected) document.documentElement.appendChild(indicator);
  };

  const hide = () => {
    if (!indicator?.isConnected) return;
    const el = indicator;
    el.style.transition = 'left .2s, right .2s, opacity .2s';
    el.style[el.style.left === 'auto' ? 'right' : 'left'] = `${-SIZE}px`;
    el.style.opacity = '0';
    setTimeout(() => { if (pulled === 0) el.remove(); }, 200);
  };

  const release = () => {
    clearTimeout(staleTimer);
    if (pulled === 0) return;
    if (armed()) {
      const back = pulled < 0;
      back ? history.back() : history.forward();
      pulled = 0;
      // 다음 화면이 뜨기 전까지 파란 화살표를 보여 준다. 같은 문서 안에서 이동했을 때를 위해 잠시 뒤 숨긴다.
      setTimeout(hide, 250);
      return;
    }
    pulled = 0;
    hide();
  };

  window.__abmsTrackpad = (touch) => {
    touching = touch === 'down';
    if (!touching) release();
  };

  window.addEventListener('wheel', (event) => {
    // 손을 뗀 뒤 이어지는 관성 스크롤은 스와이프로 세지 않는다.
    if (!touching || event.ctrlKey) return;
    const { deltaX, deltaY } = event;
    if (pulled === 0) {
      if (Math.abs(deltaX) < 2 || Math.abs(deltaX) <= Math.abs(deltaY) * 1.5) return;
      if (scrollsHorizontally(event.target, deltaX) || !canGo(deltaX < 0)) return;
    } else if (Math.sign(pulled + deltaX) !== Math.sign(pulled)) {
      // 반대 방향으로 되돌리면 취소한다.
      pulled = 0;
      hide();
      return;
    }
    event.preventDefault();
    pulled = Math.max(-THRESHOLD, Math.min(THRESHOLD, pulled + deltaX));
    show();
    clearTimeout(staleTimer);
    staleTimer = setTimeout(() => { touching = false; pulled = 0; hide(); }, STALE_MS);
  }, { passive: false, capture: true });
})();
