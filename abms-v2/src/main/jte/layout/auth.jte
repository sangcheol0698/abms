@import gg.jte.Content
@param String title
@param Content content
<!DOCTYPE html>
<html lang="ko" class="h-full">
<head>
    @template.layout.head(title = title)
</head>
<body class="h-full bg-slate-100">
<div class="flex min-h-full items-center justify-center px-4 py-12">
    ${content}
</div>
<div id="toasts" class="pointer-events-none fixed top-4 right-4 z-50 flex flex-col gap-2"></div>
</body>
</html>
