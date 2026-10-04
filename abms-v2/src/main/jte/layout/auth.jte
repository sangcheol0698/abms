@import gg.jte.Content
@param String title
@param Content content
<!DOCTYPE html>
<html lang="ko" class="h-full" data-seed data-seed-color-mode="light-only">
<head>
    @template.layout.head(title = title)
</head>
<body class="h-full bg-bg-neutral-weak">
<div class="flex min-h-full items-center justify-center px-4 py-12">
    ${content}
</div>
<div id="toasts" class="pointer-events-none fixed inset-x-0 bottom-6 z-50 flex flex-col items-center gap-x2 px-x4" aria-live="polite"></div>
</body>
</html>
