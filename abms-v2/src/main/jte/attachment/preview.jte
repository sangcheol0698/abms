@import kr.co.abacus.abms.attachment.Attachment
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@param Attachment attachment
<%-- data-modal-wide: 모달을 넓게 연다 (app.js) --%>
<div data-modal-wide class="flex h-12 items-center gap-x1_5 border-b border-stroke-neutral-subtle pr-x2 pl-x5">
    <h2 class="min-w-0 flex-1 truncate t4-bold text-fg-neutral" title="${attachment.getOriginalName()}">${attachment.getOriginalName()}</h2>
    <span class="hidden t3-regular text-fg-neutral-subtle tabular-nums sm:inline">${attachment.sizeLabel()}</span>
    <a href="/attachments/${attachment.id()}/inline" target="_blank" rel="noopener" hx-boost="false" class="${Seed.iconButton("ghost", "xsmall")}" aria-label="새 탭에서 열기" title="새 탭에서 열기">${Icons.of("external", "size-4")}</a>
    <a href="/attachments/${attachment.id()}/download" hx-boost="false" class="${Seed.iconButton("ghost", "xsmall")}" aria-label="내려받기" title="내려받기">${Icons.of("download", "size-4")}</a>
    <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" data-close-modal aria-label="닫기" title="닫기 (Esc)">${Icons.of("x", "size-4")}</button>
</div>
@if(attachment.isImage())
    <div class="flex max-h-[80vh] items-center justify-center overflow-auto bg-bg-neutral-weak p-x3">
        <img src="/attachments/${attachment.id()}/inline" alt="${attachment.getOriginalName()}" class="max-h-[76vh] max-w-full object-contain">
    </div>
@else
    <iframe src="/attachments/${attachment.id()}/inline" title="${attachment.getOriginalName()} 미리보기" class="block h-[80vh] w-full border-0 bg-bg-neutral-weak"></iframe>
@endif
