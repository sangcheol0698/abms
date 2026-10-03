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

    // ---------------------------------------------------------------------
    // 토스트
    // ---------------------------------------------------------------------
    const TOAST_STYLE = {
        success: {ring: 'ring-emerald-200', icon: '✓', iconClass: 'bg-emerald-100 text-emerald-700'},
        error: {ring: 'ring-rose-200', icon: '!', iconClass: 'bg-rose-100 text-rose-700'},
        info: {ring: 'ring-slate-200', icon: 'i', iconClass: 'bg-brand-100 text-brand-700'}
    };

    function showToast(type, message) {
        const container = document.getElementById('toasts');
        if (!container || !message) return;
        const style = TOAST_STYLE[type] || TOAST_STYLE.info;
        const el = document.createElement('div');
        el.setAttribute('role', 'status');
        el.className = 'pointer-events-auto flex w-80 items-start gap-3 rounded-xl bg-white p-3.5 text-sm shadow-lg ring-1 transition duration-300 translate-y-2 opacity-0 ' + style.ring;
        const icon = document.createElement('span');
        icon.className = 'mt-0.5 flex size-5 shrink-0 items-center justify-center rounded-full text-xs font-bold ' + style.iconClass;
        icon.textContent = style.icon;
        const text = document.createElement('p');
        text.className = 'flex-1 text-slate-700';
        text.textContent = message;
        el.append(icon, text);
        container.appendChild(el);
        requestAnimationFrame(() => el.classList.remove('translate-y-2', 'opacity-0'));
        setTimeout(() => {
            el.classList.add('opacity-0');
            setTimeout(() => el.remove(), 300);
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

    function renderCharts(root) {
        if (typeof Chart === 'undefined') return;
        root.querySelectorAll('canvas[data-chart]').forEach((canvas) => {
            if (canvas.dataset.rendered) return;
            canvas.dataset.rendered = 'true';
            const data = JSON.parse(canvas.dataset.chart);
            new Chart(canvas, {
                data: {
                    labels: data.labels,
                    datasets: [
                        {type: 'line', label: '이익', data: data.profit, borderColor: '#10b981', backgroundColor: '#10b981', cubicInterpolationMode: 'monotone', pointRadius: 3, yAxisID: 'y'},
                        {type: 'bar', label: '매출', data: data.revenue, backgroundColor: '#3b65f5', borderRadius: 4, maxBarThickness: 22},
                        {type: 'bar', label: '비용', data: data.cost, backgroundColor: '#cbd5e1', borderRadius: 4, maxBarThickness: 22}
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
                        y: {ticks: {callback: (v) => compact(v)}, grid: {color: '#f1f5f9'}},
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

    function enhance(root) {
        renderCharts(root);
        renderMarkdown(root);
        root.querySelectorAll('[data-scroll-bottom]').forEach((el) => el.scrollTop = el.scrollHeight);
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
        bubble.className = 'max-w-[80%] rounded-2xl rounded-tr-md bg-brand-600 px-4 py-2.5 text-sm whitespace-pre-wrap text-white';
        bubble.textContent = input.value.trim();
        mine.appendChild(bubble);
        const typing = document.createElement('div');
        typing.dataset.pending = 'true';
        typing.className = 'flex items-center gap-2 text-sm text-slate-500';
        typing.innerHTML = '<span class="flex gap-1"><span class="size-2 animate-bounce rounded-full bg-brand-400"></span><span class="size-2 animate-bounce rounded-full bg-brand-400 [animation-delay:120ms]"></span><span class="size-2 animate-bounce rounded-full bg-brand-400 [animation-delay:240ms]"></span></span> 답변을 생성하고 있어요…';
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
