@import gg.jte.Content
@param String title
@param Content content
<!DOCTYPE html>
<html lang="ko" class="h-full" data-seed data-seed-color-mode="system" data-seed-user-color-scheme="light">
<head>
    @template.layout.head(title = title)
</head>
<body class="h-full bg-bg-layer-default">
<div class="flex min-h-full items-center justify-center px-4 py-12">
    ${content}
</div>
<div id="toasts" popover="manual" class="toast-region" aria-live="polite"></div>
</body>
</html>
