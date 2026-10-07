// ABMS 클라이언트 동작: 토스트, 모달, 차트, 마크다운 렌더링
(function () {
    'use strict';

    // ---------------------------------------------------------------------
    // HTMX 설정: 검증 오류(422)는 폼을 다시 그리도록 교체를 허용한다.
    // ---------------------------------------------------------------------
    htmx.config.responseHandling = [
        {code: '204', swap: false},
        {code: '[23]..', swap: true},
        {code: '422', swap: true},
        {code: '[45]..', swap: false, error: true}
    ];
    htmx.config.defaultSwapStyle = 'innerHTML';
    // 뒤로·앞으로 가기는 떠날 때 찍은 스냅샷이 아니라 서버의 최신 화면을 보여준다. (읽음 표시·수정 결과 등이 바로 반영되도록)
    // 복원 요청은 일반 페이지 요청으로 보내 사이드바 배지 등 화면 공통 정보도 새로 계산되게 한다. 스크롤 위치는 따로 복원한다.
    htmx.config.historyCacheSize = 0;
    htmx.config.historyRestoreAsHxRequest = false;
    // CSP(unsafe-eval 없음): hx-on·트리거 필터 같은 eval 기능을 쓰지 않는다.
    htmx.config.allowEval = false;

    // GET 검색 폼의 빈 값은 URL 에 남기지 않는다.
    document.body.addEventListener('htmx:configRequest', (e) => {
        if (e.detail.verb !== 'get' || !e.detail.formData) return;
        for (const [key, value] of Array.from(e.detail.formData.entries())) {
            if (value === '') e.detail.formData.delete(key);
        }
    });
    htmx.config.scrollIntoViewOnBoost = false;

    // 요청 중인 버튼은 SEED Action Button 로딩 상태(data-loading)로 표시하고 중복 클릭을 막는다.
    const loadingButton = (e) => {
        const submitter = e.detail.requestConfig && e.detail.requestConfig.triggeringEvent && e.detail.requestConfig.triggeringEvent.submitter;
        const el = submitter || e.detail.elt;
        return el && el.classList && el.classList.contains('seed-action-button') ? el : null;
    };
    document.body.addEventListener('htmx:beforeRequest', (e) => {
        const button = loadingButton(e);
        if (button) {
            button.dataset.loading = '';
            button.setAttribute('aria-busy', 'true');
        }
    });
    document.body.addEventListener('htmx:afterRequest', (e) => {
        const button = loadingButton(e);
        if (button) {
            delete button.dataset.loading;
            button.removeAttribute('aria-busy');
        }
    });

    // ---------------------------------------------------------------------
    // 토스트 — SEED Snackbar (화면 하단 중앙, data-open 해제 시 퇴장 애니메이션)
    // ---------------------------------------------------------------------
    const SNACKBAR_VARIANT = {success: 'positive', error: 'critical', info: 'default'};
    const SNACKBAR_ICON = {
        positive: '<path d="M20 6 9 17l-5-5"/>',
        critical: '<circle cx="12" cy="12" r="9"/><path d="M12 8v4m0 4h.01"/>'
    };

    function showToast(type, message) {
        const container = document.getElementById('toasts');
        if (!container || !message) return;
        if (container.showPopover) {
            // 나중에 열린 모달보다도 위에 오도록 top layer 맨 위로 다시 올린다.
            if (container.matches(':popover-open')) container.hidePopover();
            container.showPopover();
        }
        const variant = SNACKBAR_VARIANT[type] || 'default';
        const el = document.createElement('div');
        el.setAttribute('role', variant === 'critical' ? 'alert' : 'status');
        el.className = 'seed-snackbar__root pointer-events-auto';
        el.dataset.open = '';
        if (SNACKBAR_ICON[variant]) {
            el.insertAdjacentHTML('beforeend', '<svg class="seed-snackbar__prefixIcon seed-snackbar__prefixIcon--variant_' + variant
                + '" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">'
                + SNACKBAR_ICON[variant] + '</svg>');
        }
        const content = document.createElement('div');
        content.className = 'seed-snackbar__content';
        const text = document.createElement('p');
        text.className = 'seed-snackbar__message';
        text.textContent = message;
        content.appendChild(text);
        el.appendChild(content);
        container.appendChild(el);
        setTimeout(() => {
            delete el.dataset.open;
            el.addEventListener('animationend', () => el.remove(), {once: true});
        }, type === 'error' ? 6000 : 3500);
    }

    window.abmsToast = showToast;

    document.body.addEventListener('toast', (e) => showToast(e.detail.type, e.detail.message));

    document.body.addEventListener('htmx:responseError', (e) => {
        const xhr = e.detail.xhr;
        if (xhr && !xhr.getResponseHeader('HX-Trigger') && xhr.status >= 500) {
            showToast('error', '요청을 처리하는 중 오류가 발생했습니다.');
        }
    });

    document.body.addEventListener('htmx:sendError', () => showToast('error', '서버에 연결할 수 없습니다.'));

    // 세그먼트 전환([data-segmented]): 응답을 기다리지 않고 누른 항목을 바로 선택 상태로 보여준다.
    document.addEventListener('click', (e) => {
        const item = e.target.closest('[data-segmented] > a');
        if (!item || e.metaKey || e.ctrlKey || e.shiftKey) return;
        item.parentElement.querySelectorAll(':scope > a').forEach((a) => a.removeAttribute('aria-current'));
        item.setAttribute('aria-current', 'true');
    });

    // ---------------------------------------------------------------------
    // 모달: #modal-body 로 콘텐츠가 들어오면 열고, closeModal 이벤트로 닫는다.
    // ---------------------------------------------------------------------
    const modal = () => document.getElementById('modal');

    document.body.addEventListener('htmx:afterSwap', (e) => {
        if (e.detail.target && e.detail.target.id === 'modal-body') {
            const dialog = modal();
            if (dialog) dialog.classList.toggle('modal-wide', !!dialog.querySelector('[data-modal-wide]'));
            if (dialog && !dialog.open) dialog.showModal();
            const first = dialog && dialog.querySelector('[autofocus], input:not([type=hidden]), select, textarea');
            if (first) first.focus();
        }
    });

    // 모달 폼은 ⌘/Ctrl + Enter 로 제출한다.
    document.addEventListener('keydown', (e) => {
        const dialog = modal();
        if (!dialog || !dialog.open || !(e.metaKey || e.ctrlKey) || e.key !== 'Enter') return;
        const form = dialog.querySelector('form');
        if (form) {
            e.preventDefault();
            form.requestSubmit();
        }
    });

    document.body.addEventListener('closeModal', () => {
        const dialog = modal();
        if (dialog && dialog.open) dialog.close();
    });

    document.addEventListener('click', (e) => {
        if (e.target.closest('[data-close-modal]')) {
            const dialog = modal();
            if (dialog) dialog.close();
        }
        // 드롭다운(details) 바깥 클릭 시 닫기
        document.querySelectorAll('details[data-dropdown][open]').forEach((d) => {
            if (!d.contains(e.target)) d.removeAttribute('open');
        });
    });

    // ---------------------------------------------------------------------
    // 차트 (Chart.js) — data-chart 속성에 JSON 데이터를 가진 canvas
    // ---------------------------------------------------------------------
    const won = (v) => new Intl.NumberFormat('ko-KR').format(v);
    const compact = (v) => {
        const abs = Math.abs(v);
        if (abs >= 100000000) return (v / 100000000).toFixed(1).replace(/\.0$/, '') + '억';
        if (abs >= 10000) return Math.round(v / 10000).toLocaleString('ko-KR') + '만';
        return won(v);
    };

    // 차트 색은 SEED 토큰(CSS 변수)에서 읽는다. (테마가 바뀌어도 토큰만 따라가면 된다)
    const token = (name) => getComputedStyle(document.documentElement).getPropertyValue('--seed-color-' + name).trim();

    function renderCharts(root) {
        if (typeof Chart === 'undefined') return;
        Chart.defaults.font.family = getComputedStyle(document.body).fontFamily;
        Chart.defaults.color = token('fg-neutral-subtle');
        root.querySelectorAll('canvas[data-chart]').forEach((canvas) => {
            if (canvas.dataset.rendered) return;
            canvas.dataset.rendered = 'true';
            const data = JSON.parse(canvas.dataset.chart);
            canvas._chart = new Chart(canvas, {
                data: {
                    labels: data.labels,
                    datasets: [
                        {type: 'line', label: '이익', data: data.profit, borderColor: token('palette-green-600'), backgroundColor: token('palette-green-600'), cubicInterpolationMode: 'monotone', pointRadius: 3, yAxisID: 'y'},
                        {type: 'bar', label: '매출', data: data.revenue, backgroundColor: token('bg-brand-solid'), borderRadius: 4, maxBarThickness: 22},
                        {type: 'bar', label: '비용', data: data.cost, backgroundColor: token('palette-gray-400'), borderRadius: 4, maxBarThickness: 22}
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    interaction: {mode: 'index', intersect: false},
                    plugins: {
                        legend: {position: 'bottom', labels: {usePointStyle: true, boxWidth: 8}},
                        tooltip: {callbacks: {label: (ctx) => ctx.dataset.label + ': ' + won(ctx.parsed.y) + '원'}}
                    },
                    scales: {
                        y: {ticks: {callback: (v) => compact(v)}, grid: {color: token('stroke-neutral-subtle')}},
                        x: {grid: {display: false}}
                    }
                }
            });
        });
    }

    // ---------------------------------------------------------------------
    // 마크다운 — data-markdown 요소의 텍스트를 렌더링한다. 원문 HTML 은 이스케이프한다.
    // ---------------------------------------------------------------------
    function escapeHtml(s) {
        return s.replace(/[&<>"']/g, (c) => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]));
    }

    function renderMarkdown(root) {
        if (typeof marked === 'undefined') return;
        root.querySelectorAll('[data-markdown]').forEach((el) => {
            if (el.dataset.rendered) return;
            el.dataset.rendered = 'true';
            const source = el.textContent;
            const renderer = new marked.Renderer();
            renderer.html = ({text}) => escapeHtml(text);
            const html = marked.parse(source, {renderer, gfm: true, breaks: true});
            el.innerHTML = html;
            el.querySelectorAll('a').forEach((a) => {
                const href = a.getAttribute('href') || '';
                if (!/^(\/|https?:)/.test(href)) a.removeAttribute('href');
                if (/^https?:/.test(href)) {
                    a.target = '_blank';
                    a.rel = 'noopener noreferrer';
                }
            });
        });
    }

    // 테마가 바뀌면 토큰 색으로 차트를 다시 그린다.
    window.abmsThemeChanged = () => {
        document.querySelectorAll('canvas[data-chart]').forEach((canvas) => {
            if (canvas._chart) canvas._chart.destroy();
            delete canvas.dataset.rendered;
        });
        renderCharts(document);
        syncThemeOptions();
    };

    function syncThemeOptions() {
        let mode = 'system';
        try { mode = localStorage.getItem('abms-theme') || 'system'; } catch (e) {}
        document.querySelectorAll('[data-theme-option]').forEach((b) => b.setAttribute('aria-pressed', String(b.dataset.themeOption === mode)));
    }

    window.abmsTheme = (mode) => {
        try { localStorage.setItem('abms-theme', mode); } catch (e) {}
        document.documentElement.dataset.seedColorMode = mode === 'light' ? 'light-only' : mode === 'dark' ? 'dark-only' : 'system';
        window.abmsThemeChanged();
    };

    function enhance(root) {
        syncThemeOptions();
        renderCharts(root);
        renderMarkdown(root);
        root.querySelectorAll('[data-scroll-bottom]').forEach((el) => el.scrollTop = el.scrollHeight);
        // 조직도에서 선택된 부서가 스크롤 영역 안에 보이도록 맞춘다.
        root.querySelectorAll('[data-dept-link][aria-current="page"]').forEach((el) => {
            const box = el.closest('.scroll-pane, .overflow-y-auto');
            if (box) box.scrollTop += el.getBoundingClientRect().top - box.getBoundingClientRect().top - (box.clientHeight - el.offsetHeight) / 2;
        });
    }

    htmx.onLoad((el) => enhance(el));

    // 서버에서 렌더링한 플래시 토스트 (리다이렉트 후 메시지)
    function showFlash() {
        const flash = document.getElementById('flash-toast');
        if (flash && !flash.dataset.shown) {
            flash.dataset.shown = 'true';
            showToast(flash.dataset.type, flash.dataset.message);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', showFlash);
    } else {
        showFlash();
    }
    document.body.addEventListener('htmx:afterSettle', showFlash);
})();

// ---------------------------------------------------------------------
// Cmd+K 명령 팔레트 + 키보드 단축키
// ---------------------------------------------------------------------
(function () {
    'use strict';
    const palette = () => document.getElementById('palette');
    const items = () => Array.from(document.querySelectorAll('#palette-results [data-palette-item]'));
    let active = 0;
    // 검색어를 바꾼 뒤 새 결과가 오기 전(stale)에 누른 Enter 는 이전 결과를 실행하지 않고, 새 결과가 오면 첫 항목에 적용한다.
    let stale = false;
    let enterPending = false;

    function setActive(index) {
        const list = items();
        if (!list.length) return;
        active = (index + list.length) % list.length;
        list.forEach((el, i) => {
            if (i === active) {
                el.dataset.active = '';
                el.scrollIntoView({block: 'nearest'});
            } else {
                delete el.dataset.active;
            }
        });
    }

    // 빈 검색어의 기본 목록을 기억해 두고 열 때 바로 보여준다. (팔레트는 페이지 이동마다 비워지므로,
    // 그대로 열면 빈 창이 먼저 떴다가 목록이 와서 창이 길어지며 깜빡인다) 최신 목록은 뒤에서 다시 받아 바꾼다.
    let defaultResults = null;

    function prefetchDefault() {
        if (defaultResults !== null) return;
        fetch('/palette?q=', {headers: {'HX-Request': 'true'}, credentials: 'same-origin'})
            .then((r) => (r.ok ? r.text() : null))
            .then((html) => { if (html !== null && defaultResults === null) defaultResults = html; })
            .catch(() => {});
    }
    if (palette()) (window.requestIdleCallback || ((f) => setTimeout(f, 1500)))(prefetchDefault);

    function openPalette() {
        const dialog = palette();
        if (!dialog || dialog.open) return;
        const input = document.getElementById('palette-input');
        input.value = '';
        stale = true;
        enterPending = false;
        const results = document.getElementById('palette-results');
        if (defaultResults !== null && results) {
            results.innerHTML = defaultResults;
            htmx.process(results);
            setActive(0);
        }
        dialog.showModal();
        input.focus();
        htmx.trigger(input, 'palette-open');
    }

    function closePalette() {
        const dialog = palette();
        if (dialog && dialog.open) dialog.close();
    }

    function runAction(action) {
        closePalette();
        if (action.startsWith('theme:')) window.abmsTheme(action.substring(6));
        if (action === 'shortcuts') document.getElementById('shortcuts').showModal();
        if (action.startsWith('layout:')) window.abmsLayout.toggle(action.substring(7));
    }

    window.abmsPalette = {open: openPalette, close: closePalette};

    document.addEventListener('click', (e) => {
        if (e.target.closest('[data-open-palette]')) {
            e.preventDefault();
            openPalette();
            return;
        }
        const item = e.target.closest('#palette-results [data-palette-item]');
        if (!item) return;
        if (item.dataset.paletteAction) {
            runAction(item.dataset.paletteAction);
        } else {
            closePalette();
        }
    });

    document.addEventListener('mousemove', (e) => {
        const item = e.target.closest && e.target.closest('#palette-results [data-palette-item]');
        if (item) setActive(items().indexOf(item));
    });

    document.addEventListener('input', (e) => {
        if (e.target.id === 'palette-input') {
            stale = true;
            enterPending = false;
        }
    });

    document.body.addEventListener('htmx:afterSwap', (e) => {
        if (!e.detail.target || e.detail.target.id !== 'palette-results') return;
        if (!document.getElementById('palette-input').value.trim()) defaultResults = e.detail.target.innerHTML;
        // 마지막 입력에 대한 응답만 최신 결과로 본다. (hx-sync=replace 로 이전 요청은 취소된다)
        stale = false;
        setActive(0);
        if (enterPending) {
            enterPending = false;
            const item = items()[0];
            if (item) item.click();
        }
    });

    document.addEventListener('keydown', (e) => {
        if (palette() && palette().open) {
            if (e.key === 'ArrowDown') { e.preventDefault(); setActive(active + 1); }
            if (e.key === 'ArrowUp') { e.preventDefault(); setActive(active - 1); }
            // 한글 입력기의 조합 확정 Enter 는 무시한다. (Safari 는 isComposing 대신 keyCode 229 로 알려 준다)
            if (e.key === 'Enter' && !e.isComposing && e.keyCode !== 229) {
                e.preventDefault();
                if (stale) {
                    enterPending = true;
                    return;
                }
                const item = items()[active];
                if (item) item.click();
            }
        }
    });

    // 전역 단축키 — 입력 중이거나 다른 대화상자가 열려 있으면 무시한다.
    const GO = {d: '/', e: '/employees', o: '/departments', p: '/projects', c: '/parties', s: '/summary', r: '/reports', a: '/assistant', m: '/me'};
    let goPending = false;
    let goTimer;

    function typing(target) {
        return target.closest('input, textarea, select, [contenteditable="true"]') !== null;
    }

    document.addEventListener('keydown', (e) => {
        if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
            e.preventDefault();
            palette() && palette().open ? closePalette() : openPalette();
            return;
        }
        if (e.metaKey || e.ctrlKey || e.altKey || e.isComposing || typing(e.target) || document.querySelector('dialog[open]')) return;
        const key = e.key.toLowerCase();
        if (goPending) {
            goPending = false;
            clearTimeout(goTimer);
            if (GO[key] && document.querySelector('a[href="' + GO[key] + '"]')) {
                e.preventDefault();
                document.querySelector('a[href="' + GO[key] + '"]').click();
            }
            return;
        }
        if (key === 'g') {
            goPending = true;
            goTimer = setTimeout(() => goPending = false, 1200);
        } else if (key === '/') {
            const search = document.querySelector('main input[type="search"]');
            if (search) { e.preventDefault(); search.focus(); search.select(); }
        } else if (key === 'c') {
            const create = document.querySelector('[data-shortcut="create"]');
            if (create) { e.preventDefault(); create.click(); }
        } else if (e.key === '[') {
            e.preventDefault();
            window.abmsLayout.toggle('left');
        } else if (e.key === ']') {
            e.preventDefault();
            window.abmsLayout.toggle('right');
        } else if (e.key === '?') {
            e.preventDefault();
            document.getElementById('shortcuts').showModal();
        }
    });
})();

// ---------------------------------------------------------------------
// 앱 셸: 양쪽 사이드바 열기/닫기 · 오른쪽 사이드바 탭 · 본문 스크롤(고정 툴바, 위치 복원)
// ---------------------------------------------------------------------
(function () {
    'use strict';
    const root = document.documentElement;
    const desktop = window.matchMedia('(min-width: 1024px)');
    // fill 레이아웃 화면(직원 목록 등)은 본문 안의 [data-fill-scroll] 이 스크롤된다.
    const scroller = () => document.querySelector('#app-scroll.app-scroll-fill [data-fill-scroll]') || document.getElementById('app-scroll');
    // 데스크톱은 #app-scroll(또는 fill 화면의 목록), 모바일은 문서가 스크롤된다.
    const scrollTarget = () => (desktop.matches ? scroller() : document.scrollingElement);

    function store(key, value) {
        try { localStorage.setItem(key, value); } catch (e) {}
    }

    // 데스크톱: 접기/펼치기 상태를 저장, 모바일: 서랍으로 연다.
    function togglePanel(side, open) {
        if (desktop.matches) {
            const attr = side === 'left' ? 'left' : 'right';
            const next = open === undefined ? (root.dataset[attr] === 'closed' ? 'open' : 'closed') : (open ? 'open' : 'closed');
            root.dataset[attr] = next;
            store('abms-' + attr, next);
            updateStickyTop();
            return next === 'open';
        }
        const opened = open === undefined ? root.dataset.drawer !== side : open;
        if (opened) root.dataset.drawer = side; else delete root.dataset.drawer;
        return opened;
    }

    function selectRightTab(name) {
        const tabs = Array.from(document.querySelectorAll('[data-right-tab]'));
        if (!tabs.length) return;
        if (!tabs.some((t) => t.dataset.rightTab === name)) name = tabs[0].dataset.rightTab;
        tabs.forEach((t) => t.setAttribute('aria-selected', String(t.dataset.rightTab === name)));
        document.querySelectorAll('[data-right-pane]').forEach((p) => p.classList.toggle('is-active', p.dataset.rightPane === name));
    }

    window.abmsLayout = {toggle: togglePanel, tab: selectRightTab};

    document.addEventListener('click', (e) => {
        const toggle = e.target.closest('[data-toggle-panel]');
        if (toggle) {
            togglePanel(toggle.dataset.togglePanel);
            return;
        }
        if (e.target.closest('[data-open-notifications]')) {
            selectRightTab('notifications');
            togglePanel('right', true);
            return;
        }
        const tab = e.target.closest('[data-right-tab]');
        if (tab) selectRightTab(tab.dataset.rightTab);
        if (e.target.closest('[data-close-panels]')) delete root.dataset.drawer;
        // 모바일 서랍 안의 링크를 누르면 서랍을 닫는다.
        if (!desktop.matches && e.target.closest('#left-sidebar a')) delete root.dataset.drawer;
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && root.dataset.drawer && !document.querySelector('dialog[open]')) delete root.dataset.drawer;
    });

    // 고정 툴바 높이를 --sticky-top 으로 반영 (그룹 헤더 등 다른 고정 요소의 기준)
    function updateStickyTop() {
        const toolbar = document.querySelector('[data-page-toolbar]');
        const header = desktop.matches ? 0 : (document.querySelector('.app-header') || {offsetHeight: 0}).offsetHeight;
        const height = toolbar && getComputedStyle(toolbar).position === 'sticky' ? toolbar.offsetHeight : 0;
        root.style.setProperty('--sticky-top', (header + height) + 'px');
    }

    function syncToolbarBorder() {
        const toolbar = document.querySelector('[data-page-toolbar]');
        const target = scrollTarget();
        if (!toolbar || !target) return;
        toolbar.toggleAttribute('data-scrolled', target.scrollTop > 0);
    }

    let scrollBound = null;
    function bindScroll() {
        const target = desktop.matches ? scroller() : window;
        if (scrollBound === target || !target) return;
        if (scrollBound) scrollBound.removeEventListener('scroll', syncToolbarBorder);
        target.addEventListener('scroll', syncToolbarBorder, {passive: true});
        scrollBound = target;
    }
    // fill 화면의 목록([data-fill-scroll])은 검색·페이지 이동 때 새로 그려지므로 다시 연결한다.
    document.body.addEventListener('htmx:afterSwap', () => bindScroll());

    // 화면 이동(boost) 시 본문 스크롤은 맨 위로, 뒤로 가기는 이전 위치로 복원한다.
    // 뒤로 가기(popstate) 때는 주소가 먼저 바뀐 뒤 떠나는 화면이 저장되므로, 화면이 열릴 때의 주소를 키로 쓴다.
    const scrollKey = () => 'abms-scroll:' + location.pathname + location.search;
    let pageKey = scrollKey();
    document.body.addEventListener('htmx:beforeHistorySave', (e) => {
        const target = scrollTarget();
        try { if (target) sessionStorage.setItem(pageKey, String(target.scrollTop)); } catch (err) {}
        cleanTransientState(e.detail.historyElt || document.body);
    });

    // 뒤로 가기로 복원되는 스냅샷에 요청 중 표시·열린 모달/메뉴가 남지 않게 지운다.
    // (남으면 버튼이 눌리지 않거나, 이전 폼이 다시 보여 요청이 다시 나가는 것처럼 보인다)
    function cleanTransientState(root) {
        root.querySelectorAll('[data-loading]').forEach((el) => {
            delete el.dataset.loading;
            el.removeAttribute('aria-busy');
        });
        root.querySelectorAll('details[data-dropdown][open]').forEach((el) => el.removeAttribute('open'));
        root.querySelectorAll('dialog[open]').forEach((el) => el.removeAttribute('open'));
        const modalBody = root.querySelector('#modal-body');
        if (modalBody) modalBody.innerHTML = '';
        root.querySelectorAll('[data-scrolled]').forEach((el) => el.removeAttribute('data-scrolled'));
    }

    function restoreScroll() {
        const target = scrollTarget();
        let y = 0;
        try { y = Number(sessionStorage.getItem(scrollKey()) || 0); } catch (e) {}
        if (target) target.scrollTop = y;
    }

    // boost 링크 이동인지 기억했다가, 화면 교체가 끝나면 맨 위로 보낸다. (뒤로 가기 복원과 구분)
    let boostedNavigation = false;
    document.body.addEventListener('htmx:beforeRequest', (e) => {
        if (e.detail.boosted && e.detail.target === document.body) boostedNavigation = true;
    });
    document.body.addEventListener('htmx:historyRestore', () => setTimeout(restoreScroll, 30));
    document.body.addEventListener('htmx:historyCacheMissLoad', () => setTimeout(restoreScroll, 30));

    function onPageReady(navigated) {
        pageKey = scrollKey();
        cleanTransientState(document.body);
        selectRightTab(document.querySelector('[data-right-tab="props"]') ? 'props' : 'notifications');
        bindScroll();
        updateStickyTop();
        if (navigated) {
            const target = scrollTarget();
            if (target) target.scrollTop = 0;
            // 키보드(Space/PageDown)로 바로 본문을 스크롤할 수 있게 포커스를 둔다.
            const main = scroller();
            if (main && desktop.matches && !document.querySelector('dialog[open]') && !main.contains(document.activeElement)) {
                main.focus({preventScroll: true});
            }
        }
        syncToolbarBorder();
    }

    document.body.addEventListener('htmx:afterSettle', (e) => {
        if (e.detail.target !== document.body) return;
        onPageReady(boostedNavigation);
        boostedNavigation = false;
    });
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => onPageReady(false));
    } else {
        onPageReady(false);
    }
    // 전체 이동(HX-Redirect 등) 후 뒤로 가기는 브라우저 bfcache 가 이전 DOM 을 그대로 되살리므로 같은 정리를 한다.
    window.addEventListener('pageshow', (e) => {
        if (!e.persisted) return;
        document.querySelectorAll('dialog[open]').forEach((d) => d.close());
        cleanTransientState(document.body);
        delete root.dataset.drawer;
    });
    window.addEventListener('resize', updateStickyTop);
    desktop.addEventListener('change', () => {
        delete root.dataset.drawer;
        bindScroll();
        updateStickyTop();
    });
    if ('ResizeObserver' in window) {
        new ResizeObserver(updateStickyTop).observe(document.body);
    }
})();

// AI 어시스턴트: 전송 즉시 내 메시지와 "답변 생성 중" 표시
window.abmsChat = {
    pending(form) {
        const input = form.querySelector('textarea');
        const messages = document.getElementById('messages');
        if (!input || !messages || !input.value.trim()) return;
        if (!form.hasAttribute('hx-target')) {
            messages.innerHTML = '';
        }
        const mine = document.createElement('div');
        mine.className = 'flex justify-end';
        mine.dataset.pending = 'true';
        const bubble = document.createElement('div');
        bubble.className = 'max-w-[80%] rounded-r4 bg-bg-neutral-weak px-x4 py-x2_5 t4-regular whitespace-pre-wrap text-fg-neutral';
        bubble.textContent = input.value.trim();
        mine.appendChild(bubble);
        const typing = document.createElement('div');
        typing.dataset.pending = 'true';
        typing.className = 'flex items-center gap-x2 t4-regular text-fg-neutral-subtle';
        typing.innerHTML = '<span class="flex gap-1"><span class="size-2 animate-bounce rounded-full bg-palette-carrot-400"></span><span class="size-2 animate-bounce rounded-full bg-palette-carrot-400 [animation-delay:120ms]"></span><span class="size-2 animate-bounce rounded-full bg-palette-carrot-400 [animation-delay:240ms]"></span></span> 답변을 생성하고 있어요…';
        messages.append(mine, typing);
        messages.scrollTop = messages.scrollHeight;
    },
    done(form, event) {
        document.querySelectorAll('#messages [data-pending]').forEach((el) => el.remove());
        if (event.detail.successful) {
            form.reset();
            const input = form.querySelector('textarea');
            if (input) {
                input.style.height = 'auto';
                setTimeout(() => input.focus(), 0);
            }
        }
        const messages = document.getElementById('messages');
        if (messages) setTimeout(() => messages.scrollTop = messages.scrollHeight, 50);
    }
};

// ---------------------------------------------------------------------
// 선언형 동작 (CSP: 인라인 이벤트 핸들러·hx-on 대신 data 속성 + 위임 이벤트)
// ---------------------------------------------------------------------
(function () {
    document.addEventListener('click', (e) => {
        const target = e.target.closest('[data-history-back], [data-chat-example], [data-demo-username], [data-open-dialog], [data-close-dialog], [data-theme-option], [data-copy-from], [data-print]');
        if (!target) return;
        if (target.matches('[data-history-back]')) {
            history.back();
        } else if (target.matches('[data-chat-example]')) {
            const input = document.getElementById('chat-input');
            if (input) {
                input.value = target.textContent.trim();
                input.focus();
            }
        } else if (target.matches('[data-demo-username]')) {
            document.getElementById('username').value = target.dataset.demoUsername;
            const password = document.getElementById('password');
            password.value = 'abms1234!';
            password.focus();
        } else if (target.matches('[data-open-dialog]')) {
            document.getElementById(target.dataset.openDialog)?.showModal();
        } else if (target.matches('[data-close-dialog]')) {
            target.closest('dialog')?.close();
        } else if (target.matches('[data-theme-option]')) {
            window.abmsTheme(target.dataset.themeOption);
        } else if (target.matches('[data-copy-from]')) {
            const source = document.getElementById(target.dataset.copyFrom);
            if (source) navigator.clipboard.writeText(source.textContent).then(() => window.abmsToast('success', target.dataset.copyMessage || '복사했습니다.'));
        } else if (target.matches('[data-print]')) {
            window.print();
        }
    });

    document.addEventListener('change', (e) => {
        if (e.target.matches('[data-submit-on-change]')) e.target.form?.requestSubmit();
    });

    // AI 어시스턴트 입력: Enter 전송(한글 조합 중 제외), Shift+Enter 줄바꿈, 높이 자동 조절
    document.addEventListener('keydown', (e) => {
        if (e.target.matches('[data-chat-input]') && e.key === 'Enter' && !e.shiftKey && !e.isComposing) {
            e.preventDefault();
            e.target.form.requestSubmit();
        }
    });
    document.addEventListener('input', (e) => {
        if (e.target.matches('[data-chat-input]')) {
            e.target.style.height = 'auto';
            e.target.style.height = e.target.scrollHeight + 'px';
        }
    });

    document.body.addEventListener('htmx:beforeRequest', (e) => {
        if (e.target.matches('[data-chat-form]')) window.abmsChat.pending(e.target);
    });
    document.body.addEventListener('htmx:afterRequest', (e) => {
        if (e.target.matches('[data-chat-form]')) window.abmsChat.done(e.target, e);
        if (e.target.matches('[data-clears-notification-dot]') && e.detail.successful) document.getElementById('notification-dot')?.remove();
    });
})();

// ---------------------------------------------------------------------
// 가벼운 대화상자(명령 팔레트·단축키)는 바깥(배경)을 누르면 닫는다. 입력을 잃을 수 있는 폼 모달은 제외.
// 누르기 시작한 곳과 뗀 곳이 모두 배경일 때만 닫는다. (안에서 드래그하다 밖에서 놓는 경우 제외)
// ---------------------------------------------------------------------
(function () {
    const LIGHT_DISMISS = ['palette', 'shortcuts'];
    let downOnBackdrop = null;
    document.addEventListener('pointerdown', (e) => {
        downOnBackdrop = e.target instanceof HTMLDialogElement && LIGHT_DISMISS.includes(e.target.id) ? e.target : null;
    });
    document.addEventListener('click', (e) => {
        if (downOnBackdrop && e.target === downOnBackdrop && downOnBackdrop.open) downOnBackdrop.close();
        downOnBackdrop = null;
    });
})();

// ---------------------------------------------------------------------
// 안내 팝업을 Esc·배경 클릭 등으로 닫아도 '닫기'(이번 로그인 동안 숨김)로 기록한다.
// 팝업 버튼으로 닫으면 서버가 다음 팝업을 보여주거나 closeModal 로 닫으므로 기록이 이미 되어 있다.
// ---------------------------------------------------------------------
(function () {
    const modal = document.getElementById('modal');
    if (!modal) return;
    let dismissedByButton = false;
    document.body.addEventListener('htmx:beforeRequest', (e) => {
        if (e.detail.pathInfo && /\/notices\/\d+\/popup\/dismiss/.test(e.detail.pathInfo.requestPath || '')) dismissedByButton = true;
    });
    modal.addEventListener('close', () => {
        const popup = modal.querySelector('[data-notice-popup]');
        if (popup && !dismissedByButton) {
            htmx.ajax('POST', '/notices/' + popup.dataset.noticePopup + '/popup/dismiss?mode=close', {swap: 'none'});
        }
        dismissedByButton = false;
    });
})();


// ---------------------------------------------------------------------
// 금액 입력 도우미: [data-money] 입력 아래에 '= 1억 1,230만 원' 처럼 읽기 쉬운 금액을 보여주고,
// data-money-max(상한) 를 넘거나 data-money-prev(이전 값) 의 3배를 넘으면 제출 전에 한 번 더 확인한다. (자릿수 실수 방지)
// ---------------------------------------------------------------------
(function () {
    function korean(value) {
        if (!/^\d+$/.test(value)) return '';
        let n = BigInt(value);
        if (n === 0n) return '0원';
        const eok = n / 100000000n;
        const man = (n % 100000000n) / 10000n;
        const won = n % 10000n;
        const fmt = (x) => Number(x).toLocaleString('ko-KR');
        const parts = [eok > 0n ? fmt(eok) + '억' : '', man > 0n ? fmt(man) + '만' : '', won > 0n ? fmt(won) : ''].filter(Boolean).join(' ');
        return won > 0n ? parts + '원' : parts + ' 원';
    }

    function hint(input) {
        let el = input.parentElement.querySelector(':scope > [data-money-text]');
        if (!el) {
            el = document.createElement('p');
            el.className = 'field-help tabular-nums';
            el.dataset.moneyText = '';
            el.setAttribute('aria-live', 'polite');
            input.insertAdjacentElement('afterend', el);
        }
        return el;
    }

    function render(input) {
        const text = korean(input.value.replace(/,/g, '').trim());
        hint(input).textContent = text ? '= ' + text : '';
    }

    document.addEventListener('input', (e) => {
        if (e.target.matches && e.target.matches('[data-money]')) render(e.target);
    });
    const renderAll = (root) => root.querySelectorAll && root.querySelectorAll('[data-money]').forEach((input) => { if (input.value) render(input); });
    htmx.onLoad(renderAll);
    document.body.addEventListener('htmx:afterSwap', () => renderAll(document.body));

    // htmx 보다 먼저(캡처 단계) 막고 확인창(confirm.js)으로 묻는다. 확인하면 같은 제출 버튼으로 다시 제출한다.
    document.addEventListener('submit', (e) => {
        const form = e.target;
        if (form.dataset.moneyConfirmed) {
            delete form.dataset.moneyConfirmed;
            return;
        }
        for (const input of form.querySelectorAll('[data-money]')) {
            const raw = input.value.replace(/,/g, '').trim();
            if (!/^\d+$/.test(raw)) continue;
            const value = BigInt(raw);
            const reasons = [];
            if (input.dataset.moneyMax && value > BigInt(input.dataset.moneyMax)) reasons.push('상한(' + korean(input.dataset.moneyMax) + ')을 넘습니다.');
            if (input.dataset.moneyPrev && BigInt(input.dataset.moneyPrev) > 0n && value > BigInt(input.dataset.moneyPrev) * 3n) {
                reasons.push('이전 값(' + korean(input.dataset.moneyPrev) + ')의 3배를 넘습니다.');
            }
            if (!reasons.length) continue;
            e.preventDefault();
            e.stopImmediatePropagation();
            const submitter = e.submitter;
            window.abmsConfirm({
                title: input.dataset.money + ' ' + korean(raw) + '이 맞나요?',
                description: Number(raw).toLocaleString('ko-KR') + '원\n' + reasons.join('\n') + '\n자릿수를 잘못 입력하지 않았는지 확인하세요.',
                confirmLabel: '이 금액으로 저장'
            }).then((yes) => {
                if (yes) {
                    form.dataset.moneyConfirmed = 'true';
                    form.requestSubmit(submitter && submitter.form === form ? submitter : undefined);
                } else {
                    input.focus();
                }
            });
            return;
        }
    }, true);
})();
