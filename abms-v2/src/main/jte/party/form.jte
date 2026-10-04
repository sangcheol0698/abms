@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.party.Party
@import kr.co.abacus.abms.party.PartyController.PartyForm
@import kr.co.abacus.abms.party.PartyType
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
    <form method="post" action="${editing ? "/parties/" + party.id() : "/parties"}" class="mx-auto max-w-4xl" novalidate>${kr.co.abacus.abms.common.web.Csrf.field()}
        @template.components.globalError(errors = errors)
        <section class="form-section">
            <div><h2 class="form-section-title">기본 정보</h2><p class="form-section-desc">구분에 따라 고객사(발주처)와 협력사(외주·파트너)를 함께 관리합니다.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                <div class="@xl:col-span-2">@template.components.input(name = "name", label = "협력사명", value = Ui.value(form.name()), errors = errors, required = true)</div>
                @template.components.select(name = "partyType", label = "구분", options = Ui.options(PartyType.values()), value = Ui.value(form.partyType()), errors = errors, required = true)
                @template.components.input(name = "businessNumber", label = "사업자등록번호", value = Ui.value(form.businessNumber()), errors = errors, placeholder = "000-00-00000")
                @template.components.input(name = "ceoName", label = "대표자", value = Ui.value(form.ceoName()), errors = errors)
                @template.components.input(name = "industry", label = "업종", value = Ui.value(form.industry()), errors = errors, placeholder = "예: 금융, 통신, 이커머스")
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">연락처·주소</h2><p class="form-section-desc">주소는 선택입니다. 등록하면 상세 화면에 지도가 표시됩니다.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                @template.components.input(name = "phone", label = "대표번호", value = Ui.value(form.phone()), errors = errors, type = "tel", placeholder = "02-000-0000")
                @template.components.input(name = "website", label = "웹사이트", value = Ui.value(form.website()), errors = errors, placeholder = "example.com")
                <div class="@xl:col-span-2">@template.components.addressField(zipCode = form.zipCode(), address = form.address(), addressDetail = form.addressDetail(),
                        latitude = form.latitude(), longitude = form.longitude(), errors = errors, mapEnabled = ctx.mapEnabled())</div>
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">메모</h2><p class="form-section-desc">거래 조건, 특이 사항 등을 기록합니다.</p></div>
            <div>
                <label for="memo" class="sr-only">메모</label>
                <textarea id="memo" name="memo" rows="3" class="textarea">${Ui.value(form.memo())}</textarea>
                @if(errors.has("memo"))<p class="field-error">${errors.get("memo")}</p>@endif
            </div>
        </section>
        <div class="form-actions">
            <a href="${editing ? "/parties/" + party.id() : "/parties"}" class="${Seed.button("neutralOutline")}">취소</a>
            <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "등록"}</button>
        </div>
    </form>
`)
