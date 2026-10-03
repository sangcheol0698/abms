@import gg.jte.Content
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@param String title
@param Content body
@param Content footer = null
@param String description = null
<div class="flex items-start justify-between gap-x4 px-x6 pt-x6 pb-x2">
    <div>
        <h2 class="t7-bold text-fg-neutral">${title}</h2>
        @if(description != null)
            <p class="mt-x1 t4-regular text-fg-neutral-subtle">${description}</p>
        @endif
    </div>
    <button type="button" class="${Seed.iconButton("ghost", "small")} -mr-2 -mt-1" data-close-modal aria-label="닫기">${Icons.of("x", "size-5")}</button>
</div>
<div class="px-x6 py-x4">${body}</div>
@if(footer != null)
    <div class="flex justify-end gap-x2 px-x6 pt-x2 pb-x6">${footer}</div>
@endif
