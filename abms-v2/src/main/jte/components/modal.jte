@import gg.jte.Content
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@param String title
@param Content body
@param Content footer = null
@param String description = null
@param String context = null
@param String hint = null
<%-- Linear 풍 모달: 맥락 › 제목 머리, 속성 행 본문, 단축키 힌트가 있는 바닥 --%>
<div class="flex h-12 items-center gap-x1_5 border-b border-stroke-neutral-subtle pr-x2 pl-x5">
    @if(context != null)
        <span class="max-w-[50%] truncate t4-regular text-fg-neutral-subtle">${context}</span>
        ${Icons.of("chevron-right", "size-3 shrink-0 text-fg-neutral-subtle")}
    @endif
    <h2 class="min-w-0 flex-1 truncate t4-bold text-fg-neutral">${title}</h2>
    <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" data-close-modal aria-label="닫기" title="닫기 (Esc)">${Icons.of("x", "size-4")}</button>
</div>
@if(description != null)
    <p class="px-x5 pt-x4 t3-regular text-fg-neutral-subtle">${description}</p>
@endif
<div class="px-x5 py-x4">${body}</div>
@if(footer != null)
    <div class="flex items-center gap-x2 border-t border-stroke-neutral-subtle px-x5 py-x3">
        <span class="mr-auto flex items-center gap-x1 t3-regular text-fg-neutral-subtle">
            @if(hint != null)<span class="kbd">⌘</span><span class="kbd">↵</span> ${hint}@endif
        </span>
        ${footer}
    </div>
@endif
