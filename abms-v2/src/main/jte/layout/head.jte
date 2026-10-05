@param String title
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="color-scheme" content="light dark">
<script nonce="${kr.co.abacus.abms.common.web.Csp.nonce()}">
    // 테마 초기화(첫 페인트 전): 저장된 선택(system|light|dark) → SEED data-seed-color-mode, 시스템 설정 → data-seed-user-color-scheme
    (function () {
        try {
            var root = document.documentElement;
            var mode = localStorage.getItem('abms-theme') || 'system';
            root.dataset.left = localStorage.getItem('abms-left') || 'open';
            root.dataset.right = localStorage.getItem('abms-right') || (window.innerWidth >= 1440 ? 'open' : 'closed');
            root.dataset.seedColorMode = mode === 'light' ? 'light-only' : mode === 'dark' ? 'dark-only' : 'system';
            var query = window.matchMedia('(prefers-color-scheme: dark)');
            var apply = function () { root.dataset.seedUserColorScheme = query.matches ? 'dark' : 'light'; };
            apply();
            query.addEventListener('change', function () { apply(); window.abmsThemeChanged && window.abmsThemeChanged(); });
        } catch (e) {}
    })();
</script>
<title>${title} · ABMS</title>
<link rel="icon" href="/favicon.svg" type="image/svg+xml">
<link rel="preconnect" href="https://cdn.jsdelivr.net">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.min.css">
<link rel="stylesheet" href="/css/app.css">
<script nonce="${kr.co.abacus.abms.common.web.Csp.nonce()}" defer src="/webjars/htmx.org/dist/htmx.min.js"></script>
<script nonce="${kr.co.abacus.abms.common.web.Csp.nonce()}" defer src="/webjars/chart.js/dist/chart.umd.min.js"></script>
<script nonce="${kr.co.abacus.abms.common.web.Csp.nonce()}" defer src="/webjars/marked/lib/marked.umd.js"></script>
<script nonce="${kr.co.abacus.abms.common.web.Csp.nonce()}" defer src="/js/app.js"></script>
<script nonce="${kr.co.abacus.abms.common.web.Csp.nonce()}" defer src="/js/map.js"></script>
<script nonce="${kr.co.abacus.abms.common.web.Csp.nonce()}" defer src="/js/forms.js"></script>
<script nonce="${kr.co.abacus.abms.common.web.Csp.nonce()}" defer src="/js/confirm.js"></script>
