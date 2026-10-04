@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.site.Site
@import kr.co.abacus.abms.site.SiteController.SiteForm
@import kr.co.abacus.abms.site.SiteType
@param ViewContext ctx
@param SiteForm form
@param FormErrors errors
@param Site site = null
!{boolean editing = site != null;}
!{String title = editing ? site.getName() + " 수정" : "사업장 등록";}
@template.layout.app(ctx = ctx, title = title, content = @`
    @template.components.pageHeader(title = title, breadcrumb = @`
        <a href="/sites" class="hover:text-fg-neutral-muted">사업장</a>${Icons.of("chevron-right", "size-3")}<span>${editing ? "수정" : "등록"}</span>
    `)
    <form method="post" action="${editing ? "/sites/" + site.id() : "/sites"}" class="mx-auto max-w-4xl" novalidate>
        @template.components.globalError(errors = errors)
        <section class="form-section">
            <div><h2 class="form-section-title">기본 정보</h2><p class="form-section-desc">부서 정보에서 근무 사업장으로 연결할 수 있습니다.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                <div class="@xl:col-span-2">@template.components.input(name = "name", label = "사업장명", value = Ui.value(form.name()), errors = errors, required = true, placeholder = "예: 본사, 판교 연구소")</div>
                @template.components.select(name = "siteType", label = "유형", options = Ui.options(SiteType.values()), value = Ui.value(form.siteType()), errors = errors, required = true)
                @template.components.input(name = "phone", label = "대표번호", value = Ui.value(form.phone()), errors = errors, type = "tel", placeholder = "02-000-0000")
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">위치</h2><p class="form-section-desc">주소는 선택입니다. 등록하면 지도와 가까운 협력사를 보여줍니다.</p></div>
            @template.components.addressField(zipCode = form.zipCode(), address = form.address(), addressDetail = form.addressDetail(),
                    latitude = form.latitude(), longitude = form.longitude(), errors = errors, mapEnabled = ctx.mapEnabled())
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">메모</h2><p class="form-section-desc">출입·주차 안내 등을 기록합니다.</p></div>
            <div>
                <label for="memo" class="sr-only">메모</label>
                <textarea id="memo" name="memo" rows="3" class="textarea">${Ui.value(form.memo())}</textarea>
                @if(errors.has("memo"))<p class="field-error">${errors.get("memo")}</p>@endif
            </div>
        </section>
        <div class="form-actions">
            <a href="${editing ? "/sites/" + site.id() : "/sites"}" class="${Seed.button("neutralOutline")}">취소</a>
            <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "등록"}</button>
        </div>
    </form>
`)
