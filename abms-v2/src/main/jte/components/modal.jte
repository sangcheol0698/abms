@import gg.jte.Content
@import kr.co.abacus.abms.common.web.Icons
@param String title
@param Content body
@param Content footer = null
@param String description = null
<div class="flex items-start justify-between gap-4 border-b border-slate-100 px-6 py-4">
    <div>
        <h2 class="text-base font-semibold text-slate-900">${title}</h2>
        @if(description != null)
            <p class="mt-0.5 text-sm text-slate-500">${description}</p>
        @endif
    </div>
    <button type="button" class="btn btn-ghost -mr-2 !p-1.5" data-close-modal aria-label="닫기">${Icons.of("x", "size-5")}</button>
</div>
<div class="px-6 py-5">${body}</div>
@if(footer != null)
    <div class="flex justify-end gap-2 rounded-b-2xl border-t border-slate-100 bg-slate-50 px-6 py-3.5">${footer}</div>
@endif
