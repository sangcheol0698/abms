@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.party.Party
@import kr.co.abacus.abms.party.PartyController.PartyForm
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param PartyForm form
@param FormErrors errors
@param Party party = null
!{boolean editing = party != null;}
!{String title = editing ? party.getName() + " 수정" : "협력사 등록";}
@template.layout.app(ctx = ctx, title = title, content = @`
    @template.components.pageHeader(title = title, breadcrumb = @`
        <a href="/parties" class="hover:text-fg-neutral-muted">협력사</a>${Icons.of("chevron-right", "size-3")}<span>${editing ? "수정" : "등록"}</span>
    `)
    <form method="post" action="${editing ? "/parties/" + party.id() : "/parties"}" class="mx-auto max-w-4xl" novalidate>
        @template.components.globalError(errors = errors)
        <section class="form-section">
            <div><h2 class="form-section-title">기본 정보</h2><p class="form-section-desc">협력사명은 중복될 수 없습니다.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                <div class="@xl:col-span-2">@template.components.input(name = "name", label = "협력사명", value = Ui.value(form.name()), errors = errors, required = true)</div>
                @template.components.input(name = "ceoName", label = "대표자", value = Ui.value(form.ceoName()), errors = errors)
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">영업 담당자</h2><p class="form-section-desc">청구·계약 연락 창구입니다.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                @template.components.input(name = "salesRepName", label = "이름", value = Ui.value(form.salesRepName()), errors = errors)
                @template.components.input(name = "salesRepPhone", label = "연락처", value = Ui.value(form.salesRepPhone()), errors = errors, type = "tel", placeholder = "010-0000-0000")
                <div class="@xl:col-span-2">@template.components.input(name = "salesRepEmail", label = "이메일", value = Ui.value(form.salesRepEmail()), errors = errors, type = "email")</div>
            </div>
        </section>
        <div class="form-actions">
            <a href="${editing ? "/parties/" + party.id() : "/parties"}" class="${Seed.button("neutralOutline")}">취소</a>
            <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "등록"}</button>
        </div>
    </form>
`)
