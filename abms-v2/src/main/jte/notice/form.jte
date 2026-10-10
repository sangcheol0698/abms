@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.web.ViewContext
@import kr.co.abacus.abms.notice.Notice
@import kr.co.abacus.abms.notice.NoticeController.NoticeForm
@import kr.co.abacus.abms.notice.NoticeImportance
@param ViewContext ctx
@param NoticeForm form
@param FormErrors errors
@param Notice notice = null
!{boolean editing = notice != null;}
!{String title = editing ? "공지 수정" : "공지 등록";}
@template.layout.app(ctx = ctx, title = title, content = @`
    @template.components.pageHeader(title = title, breadcrumb = @`
        <a href="/notices" class="hover:text-fg-neutral-muted">공지사항</a>${Icons.of("chevron-right", "size-3")}
        @if(editing)<a href="/notices/${notice.id()}" class="hover:text-fg-neutral-muted">${notice.getTitle()}</a>${Icons.of("chevron-right", "size-3")}@endif
        <span>${editing ? "수정" : "등록"}</span>
    `)
    <form method="post" action="${editing ? "/notices/" + notice.id() : "/notices"}" class="mx-auto max-w-4xl" novalidate>${kr.co.abacus.abms.common.web.Csrf.field()}
        @template.components.globalError(errors = errors)
        <section class="form-section">
            <div><h2 class="form-section-title">내용</h2><p class="form-section-desc">본문은 마크다운으로 작성합니다. (제목 #, 목록 -, 굵게 **글자**, 링크 [이름](주소))</p></div>
            <div class="grid grid-cols-1 gap-x4">
                @template.components.input(name = "title", label = "제목", value = Ui.value(form.title()), errors = errors, required = true)
                <div>
                    <label for="body" class="label">본문<span class="required-mark">*</span></label>
                    <textarea id="body" name="body" rows="12" maxlength="20000" required class="textarea font-mono t4-regular">${Ui.value(form.body())}</textarea>
                </div>
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">표시</h2><p class="form-section-desc">중요도는 목록과 팝업에 표시되고, 팝업은 중요도가 높은 공지부터 보여줍니다.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                @template.components.select(name = "importance", label = "중요도", options = Ui.options(NoticeImportance.values()), value = Ui.value(form.importance()), errors = errors, required = true)
                <div class="flex flex-col justify-end gap-x2">
                    <label class="inline-flex items-center gap-x2 t4-regular text-fg-neutral">
                        <input type="checkbox" name="pinned" value="true" class="checkbox" checked="${Boolean.TRUE.equals(form.pinned())}"> 목록 상단에 고정
                    </label>
                </div>
                <div class="rounded-r3 border border-stroke-neutral-subtle p-x4 @xl:col-span-2">
                    <label class="flex items-start gap-x2">
                        <input type="checkbox" name="popup" value="true" class="checkbox mt-0.5" checked="${Boolean.TRUE.equals(form.popup())}">
                        <span>
                            <span class="block t4-medium text-fg-neutral">안내 팝업으로 제공</span>
                            <span class="mt-x0_5 block t3-regular text-fg-neutral-subtle">게시 기간 동안 사용자가 화면을 열면 팝업으로 보여줍니다. 사용자는 '닫기', '오늘 하루 보지 않기', '다시 보지 않기'를 고를 수 있습니다.</span>
                        </span>
                    </label>
                </div>
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">게시 기간</h2><p class="form-section-desc">비워 두면 등록 즉시 게시하고, 종료 없이 계속 게시합니다.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                <div>
                    <label for="startsAt" class="label">게시 시작</label>
                    <input id="startsAt" name="startsAt" type="datetime-local" class="input" value="${NoticeForm.inputValue(form.startsAt())}">
                </div>
                <div>
                    <label for="endsAt" class="label">게시 종료</label>
                    <input id="endsAt" name="endsAt" type="datetime-local" class="input" value="${NoticeForm.inputValue(form.endsAt())}">
                </div>
            </div>
        </section>
        <div class="form-actions">
            <a href="${editing ? "/notices/" + notice.id() : "/notices"}" class="${Seed.button("neutralOutline")}">취소</a>
            <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "등록"}</button>
        </div>
    </form>
`)
