@import gg.jte.Content
@param String title
@param String subtitle = null
@param Content actions = null
@param Content breadcrumb = null
<div class="mb-x6">
    @if(breadcrumb != null)
        <nav class="mb-x2 flex items-center gap-x1 t3-regular text-fg-neutral-subtle">${breadcrumb}</nav>
    @endif
    <div class="flex flex-wrap items-end justify-between gap-x3">
        <div class="min-w-0">
            <h1 class="t10-bold text-fg-neutral">${title}</h1>
            @if(subtitle != null)
                <p class="mt-x1 t4-regular text-fg-neutral-subtle">${subtitle}</p>
            @endif
        </div>
        @if(actions != null)
            <div class="flex flex-wrap items-center gap-x2">${actions}</div>
        @endif
    </div>
</div>
