// macOS WKWebView에는 브라우저의 뒤로·앞으로·새로고침 조작이 없어 모든 페이지에 붙인다.
// - ⌘[ ⌘], 입력 중이 아닐 때 ⌘← ⌘→, 마우스 뒤로·앞으로 버튼, ⌘R
// - 트랙패드 두 손가락 가로 스와이프: Chrome처럼 화면 가장자리에 화살표가 따라 나오고, 끝까지 밀면 바로 이동한다.
//   손을 뗀 뒤에도 관성 스크롤이 1초 가까이 이어져 손 뗌을 기다리면 이동이 늦어지므로 끝에 닿는 순간 이동한다.
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

  const THRESHOLD = 140;
  const SIZE = 40;
  const END_DELAY = 160;

  let pulled = 0;
  let endTimer = 0;
  let navigating = false;
  let indicator = null;

  // 이동한 직후 새 화면에 이어지는 관성 스크롤이 다시 스와이프로 잡히지 않게 잠시 무시한다.
  const SETTLE_KEY = 'abms:swipe-navigated-at';
  const SETTLE_MS = 800;
  const settling = () => {
    try {
      return Date.now() - Number(sessionStorage.getItem(SETTLE_KEY) || 0) < SETTLE_MS;
    } catch {
      return false;
    }
  };

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
        transition: 'background-color .15s, color .15s',
      });
    }
    const back = pulled < 0;
    const progress = Math.min(Math.abs(pulled) / THRESHOLD, 1);
    const armed = progress >= 1;
    const offset = -SIZE + progress * (SIZE + 24);
    Object.assign(indicator.style, {
      left: back ? `${offset}px` : 'auto',
      right: back ? 'auto' : `${offset}px`,
      opacity: String(Math.min(progress * 1.5, 1)),
      transform: back ? 'none' : 'scaleX(-1)',
      backgroundColor: armed ? '#1a73e8' : '#fff',
      color: armed ? '#fff' : '#5f6368',
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

  const go = () => {
    const back = pulled < 0;
    navigating = true;
    try { sessionStorage.setItem(SETTLE_KEY, String(Date.now())); } catch { /* 저장소를 못 써도 이동은 한다 */ }
    back ? history.back() : history.forward();
    setTimeout(() => { navigating = false; }, SETTLE_MS);
    pulled = 0;
    // 다음 화면이 뜨기 전까지 파란 화살표를 보여 준다. 같은 문서 안에서 이동했을 때를 위해 잠시 뒤 숨긴다.
    setTimeout(hide, 250);
  };

  const cancel = () => {
    pulled = 0;
    hide();
  };

  window.addEventListener('wheel', (event) => {
    if (navigating) {
      if (Math.abs(event.deltaX) > Math.abs(event.deltaY)) event.preventDefault();
      return;
    }
    if (event.ctrlKey || (pulled === 0 && settling())) return;
    const { deltaX, deltaY } = event;
    if (pulled === 0) {
      if (Math.abs(deltaX) < 2 || Math.abs(deltaX) <= Math.abs(deltaY) * 1.5) return;
      if (scrollsHorizontally(event.target, deltaX) || !canGo(deltaX < 0)) return;
    } else if (Math.sign(pulled + deltaX) !== Math.sign(pulled)) {
      // 반대 방향으로 되돌리면 취소한다.
      clearTimeout(endTimer);
      cancel();
      return;
    }
    event.preventDefault();
    pulled = Math.max(-THRESHOLD, Math.min(THRESHOLD, pulled + deltaX));
    show();
    clearTimeout(endTimer);
    if (Math.abs(pulled) >= THRESHOLD) go();
    else endTimer = setTimeout(cancel, END_DELAY);
  }, { passive: false, capture: true });
})();
