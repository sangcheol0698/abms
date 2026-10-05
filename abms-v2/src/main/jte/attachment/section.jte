@import kr.co.abacus.abms.attachment.Attachment
@import kr.co.abacus.abms.attachment.AttachmentCategory
@import kr.co.abacus.abms.attachment.AttachmentSection
@import kr.co.abacus.abms.attachment.AttachmentService
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.Ui
@param AttachmentSection section
<section id="attachments" class="min-w-0">
    <h2 class="section-title">첨부 파일 <span class="section-count">${section.attachments().size()}</span></h2>
    <div class="list-box">
        @if(section.canWrite())
            <form class="flex flex-wrap items-center gap-x2 border-b border-stroke-neutral-subtle px-x4 py-x3" hx-post="/attachments"
                  hx-encoding="multipart/form-data" hx-target="#attachments" hx-swap="outerHTML">
                <input type="hidden" name="ownerType" value="${section.ownerType().name()}">
                <input type="hidden" name="ownerId" value="${section.ownerId()}">
                <label class="file-drop" data-file-drop data-max-size="${AttachmentService.MAX_SIZE}">
                    <input id="attachment-file" type="file" name="file" required accept="${section.accept()}" class="sr-only">
                    <span class="flex size-8 shrink-0 items-center justify-center rounded-r2 bg-bg-neutral-weak text-fg-neutral-muted">${Icons.of("upload", "size-4")}</span>
                    <span class="flex min-w-0 flex-1 flex-wrap items-center gap-x-x2 gap-y-x0_5" data-file-drop-text>
                        <span class="t4-medium text-fg-neutral">파일을 끌어다 놓거나 눌러서 선택</span>
                        <span>문서·이미지·zip, 20MB 이하</span>
                    </span>
                </label>
                <label for="attachment-category" class="sr-only">분류</label>
                <select id="attachment-category" name="category" class="select filter-select w-28">
                    @for(var o : Ui.options(AttachmentCategory.values()))
                        <option value="${o.value()}">${o.label()}</option>
                    @endfor
                </select>
                <button type="submit" class="${Seed.button("neutralOutline", "small")} ml-auto">${Icons.of("plus", "size-4")} 첨부</button>
            </form>
        @endif
        @if(section.attachments().isEmpty())
            @template.components.empty(message = "첨부된 파일이 없습니다.", icon = "document")
        @else
            @for(Attachment a : section.attachments())
                <div class="list-row">
                    <span class="flex h-6 w-9 shrink-0 items-center justify-center rounded-r1_5 bg-bg-neutral-weak t2-bold uppercase text-fg-neutral-muted">${a.extension().isEmpty() ? "?" : a.extension()}</span>
                    @if(a.previewType().isPresent())
                        <a href="/attachments/${a.id()}/inline" class="min-w-0 flex-1 truncate t4-medium hover:underline" title="${a.getOriginalName()} 미리보기"
                           hx-get="/attachments/${a.id()}/view" hx-target="#modal-body" hx-push-url="false">${a.getOriginalName()}</a>
                    @else
                        <a href="/attachments/${a.id()}/download" class="min-w-0 flex-1 truncate t4-medium hover:underline" title="${a.getOriginalName()} 내려받기" hx-boost="false">${a.getOriginalName()}</a>
                    @endif
                    <span class="${Seed.badge("neutral", "outline")}">${a.getCategory().label()}</span>
                    <span class="list-cell-muted hidden w-16 text-right tabular-nums @xl:block">${a.sizeLabel()}</span>
                    <span class="list-cell-muted hidden w-24 text-right tabular-nums @2xl:block">${Ui.date(a.getCreatedAt() == null ? null : a.getCreatedAt().toLocalDate())}</span>
                    <a href="/attachments/${a.id()}/download" hx-boost="false" class="${Seed.iconButton("ghost", "xsmall")}" aria-label="${a.getOriginalName()} 내려받기" title="내려받기">${Icons.of("download", "size-4")}</a>
                    @if(section.canWrite())
                        <button type="button" class="${Seed.iconButton("ghost", "xsmall")} text-fg-critical" aria-label="${a.getOriginalName()} 삭제" title="삭제"
                                hx-post="/attachments/${a.id()}/delete" hx-target="#attachments" hx-swap="outerHTML" hx-confirm="${a.getOriginalName()} 파일을 삭제할까요?">${Icons.of("trash", "size-4")}</button>
                    @endif
                </div>
            @endfor
        @endif
    </div>
</section>
