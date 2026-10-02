@import gg.jte.Content
@param String title
@param String subtitle = null
@param Content actions = null
@param Content breadcrumb = null
<div class="mb-6">
    @if(breadcrumb != null)
        <nav class="mb-2 flex items-center gap-1 text-xs text-slate-500">${breadcrumb}</nav>
    @endif
    <div class="flex flex-wrap items-end justify-between gap-3">
        <div class="min-w-0">
            <h1 class="text-xl font-semibold tracking-tight text-slate-900 sm:text-2xl">${title}</h1>
            @if(subtitle != null)
                <p class="mt-1 text-sm text-slate-500">${subtitle}</p>
            @endif
        </div>
        @if(actions != null)
            <div class="flex flex-wrap items-center gap-2">${actions}</div>
        @endif
    </div>
</div>
