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

    // ---------------------------------------------------------------------
    // 모달: #modal-body 로 콘텐츠가 들어오면 열고, closeModal 이벤트로 닫는다.
    // ---------------------------------------------------------------------
    const modal = () => document.getElementById('modal');

    document.body.addEventListener('htmx:afterSwap', (e) => {
        if (e.detail.target && e.detail.target.id === 'modal-body') {
            const dialog = modal();
            if (dialog && !dialog.open) dialog.showModal();
            const first = dialog && dialog.querySelector('[autofocus], input:not([type=hidden]), select, textarea');
            if (first) first.focus();
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
            const box = el.closest('.overflow-y-auto');
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

    function openPalette() {
        const dialog = palette();
        if (!dialog || dialog.open) return;
        const input = document.getElementById('palette-input');
        input.value = '';
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

    document.body.addEventListener('htmx:afterSwap', (e) => {
        if (e.detail.target && e.detail.target.id === 'palette-results') setActive(0);
    });

    document.addEventListener('keydown', (e) => {
        if (palette() && palette().open) {
            if (e.key === 'ArrowDown') { e.preventDefault(); setActive(active + 1); }
            if (e.key === 'ArrowUp') { e.preventDefault(); setActive(active - 1); }
            if (e.key === 'Enter' && !e.isComposing) {
                const item = items()[active];
                if (item) { e.preventDefault(); item.click(); }
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
        } else if (e.key === '?') {
            e.preventDefault();
            document.getElementById('shortcuts').showModal();
        }
    });
})();

// 모바일 사이드바 열기/닫기 (백드롭 클릭·Esc 로 닫힘)
window.abmsSidebar = function (open) {
    document.getElementById('sidebar').classList.toggle('hidden', !open);
    document.getElementById('sidebar-backdrop').classList.toggle('hidden', !open);
};
document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && document.getElementById('sidebar-backdrop')) window.abmsSidebar(false);
});

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
        bubble.className = 'max-w-[80%] rounded-2xl rounded-tr-md bg-bg-brand-solid px-4 py-2.5 text-sm whitespace-pre-wrap text-white';
        bubble.textContent = input.value.trim();
        mine.appendChild(bubble);
        const typing = document.createElement('div');
        typing.dataset.pending = 'true';
        typing.className = 'flex items-center gap-2 text-sm text-fg-neutral-subtle';
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
