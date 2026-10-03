@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.employee.EmployeeAvatar
@import kr.co.abacus.abms.employee.OwnProfileForm
@param ViewContext ctx
@param Employee employee
@param OwnProfileForm form
@param FormErrors errors
@template.layout.app(ctx = ctx, title = "내 정보 수정", content = @`
    @template.components.pageHeader(title = "내 정보 수정", subtitle = "이름, 생년월일, 아바타만 직접 수정할 수 있습니다. 그 밖의 정보는 인사 담당자에게 요청하세요.", breadcrumb = @`
        <a href="/employees" class="hover:text-slate-700">직원</a>${Icons.of("chevron-right", "size-3")}
        <a href="/employees/${employee.id()}" class="hover:text-slate-700">${employee.getName()}</a>${Icons.of("chevron-right", "size-3")}<span>수정</span>
    `)
    <form method="post" action="/employees/${employee.id()}/profile" class="card max-w-xl" novalidate>
        <div class="card-body space-y-4">
            @template.components.globalError(errors = errors)
            @template.components.input(name = "name", label = "이름", value = Ui.value(form.name()), errors = errors, required = true)
            @template.components.input(name = "birthDate", label = "생년월일", value = Ui.value(form.birthDate()), errors = errors, type = "date", required = true)
            <div>
                <p class="label">아바타</p>
                @template.employee.avatarPicker(selected = form.avatar() == null ? EmployeeAvatar.SKY_GLOW : form.avatar(), initial = employee.initial())
            </div>
        </div>
        <div class="flex justify-end gap-2 rounded-b-xl border-t border-slate-100 bg-slate-50 px-5 py-3.5">
            <a href="/employees/${employee.id()}" class="btn btn-secondary">취소</a>
            <button type="submit" class="btn btn-primary">저장</button>
        </div>
    </form>
`)
