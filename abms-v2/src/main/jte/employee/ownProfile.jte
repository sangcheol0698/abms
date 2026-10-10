@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.web.ViewContext
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.employee.OwnProfileForm
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param Employee employee
@param OwnProfileForm form
@param FormErrors errors
@template.layout.app(ctx = ctx, title = "내 정보 수정", content = @`
    @template.components.pageHeader(title = "내 정보 수정", subtitle = "이름, 생년월일, 아바타만 직접 수정할 수 있습니다. 그 밖의 정보는 인사 담당자에게 요청하세요.", breadcrumb = @`
        <a href="/employees" class="hover:text-fg-neutral-muted">직원</a>${Icons.of("chevron-right", "size-3")}
        <a href="/employees/${employee.id()}" class="hover:text-fg-neutral-muted">${employee.getName()}</a>${Icons.of("chevron-right", "size-3")}<span>수정</span>
    `)
    <form method="post" action="/employees/${employee.id()}/profile" class="mx-auto max-w-4xl" novalidate>${kr.co.abacus.abms.common.web.Csrf.field()}
        @template.components.globalError(errors = errors)
        <section class="form-section">
            <div><h2 class="form-section-title">기본 정보</h2><p class="form-section-desc">그 밖의 정보는 인사 담당자에게 요청하세요.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                @template.components.input(name = "name", label = "이름", value = Ui.value(form.name()), errors = errors, required = true)
                @template.components.input(name = "birthDate", label = "생년월일", value = Ui.value(form.birthDate()), errors = errors, type = "date", required = true)
                @template.components.input(name = "phone", label = "연락처", value = Ui.value(form.phone()), errors = errors, type = "tel", placeholder = "010-0000-0000")
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">보유 기술</h2><p class="form-section-desc">인력 배치와 검색에 쓰입니다.</p></div>
            <div>@template.components.input(name = "skills", label = "보유 기술", value = Ui.value(form.skills()), errors = errors, placeholder = "Java, Spring Boot, AWS", help = "쉼표로 구분해 입력하세요.")</div>
        </section>
        <div class="form-actions">
            <a href="/employees/${employee.id()}" class="${Seed.button("neutralOutline")}">취소</a>
            <button type="submit" class="${Seed.button("brandSolid")}">저장</button>
        </div>
    </form>
`)
