@import java.util.List
@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.Ui.SelectOption
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.employee.EmployeeAvatar
@import kr.co.abacus.abms.employee.EmployeeForm
@import kr.co.abacus.abms.employee.EmployeeGrade
@import kr.co.abacus.abms.employee.EmployeePosition
@import kr.co.abacus.abms.employee.EmployeeType
@param ViewContext ctx
@param EmployeeForm form
@param FormErrors errors
@param Employee employee = null
@param List<SelectOption> departmentOptions
!{boolean editing = employee != null;}
!{String title = editing ? employee.getName() + " 정보 수정" : "직원 등록";}
@template.layout.app(ctx = ctx, title = title, content = @`
    @template.components.pageHeader(title = title, breadcrumb = @`
        <a href="/employees" class="hover:text-slate-700">직원</a>${Icons.of("chevron-right", "size-3")}
        @if(editing)<a href="/employees/${employee.id()}" class="hover:text-slate-700">${employee.getName()}</a>${Icons.of("chevron-right", "size-3")}@endif
        <span>${editing ? "수정" : "등록"}</span>
    `)
    <form method="post" action="${editing ? "/employees/" + employee.id() : "/employees"}" class="card max-w-3xl" novalidate>
        <div class="card-body space-y-6">
            @template.components.globalError(errors = errors)
            <fieldset class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <legend class="mb-3 text-sm font-semibold text-slate-900">기본 정보</legend>
                @template.components.input(name = "name", label = "이름", value = Ui.value(form.name()), errors = errors, required = true)
                @template.components.input(name = "email", label = "이메일", value = Ui.value(form.email()), errors = errors, type = "email", required = true)
                @template.components.input(name = "birthDate", label = "생년월일", value = Ui.value(form.birthDate()), errors = errors, type = "date", required = true)
                @template.components.select(name = "departmentId", label = "부서", options = departmentOptions, value = Ui.value(form.departmentId()), errors = errors, required = true, emptyLabel = "부서 선택")
            </fieldset>
            <fieldset class="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <legend class="mb-3 text-sm font-semibold text-slate-900">인사 정보</legend>
                @template.components.input(name = "joinDate", label = "입사일", value = Ui.value(form.joinDate()), errors = errors, type = "date", required = true)
                @template.components.select(name = "type", label = "고용유형", options = Ui.options(EmployeeType.values()), value = Ui.value(form.type()), errors = errors, required = true)
                @template.components.select(name = "position", label = "직급", options = Ui.options(EmployeePosition.values()), value = Ui.value(form.position()), errors = errors, required = true)
                @template.components.select(name = "grade", label = "등급", options = Ui.options(EmployeeGrade.values()), value = Ui.value(form.grade()), errors = errors, required = true)
                @if(!editing)
                    @template.components.input(name = "annualSalary", label = "연봉 (원)", value = Ui.value(form.annualSalary()), errors = errors, type = "number", help = "입력하면 입사일부터 적용되는 연봉 이력이 생성됩니다.")
                @endif
            </fieldset>
            <fieldset>
                <legend class="mb-3 text-sm font-semibold text-slate-900">아바타</legend>
                @template.employee.avatarPicker(selected = form.avatar() == null ? EmployeeAvatar.SKY_GLOW : form.avatar(), initial = form.name() == null || form.name().isBlank() ? "?" : form.name().substring(0, 1))
            </fieldset>
            <div>
                <label for="memo" class="label">메모</label>
                <textarea id="memo" name="memo" rows="3" class="textarea">${Ui.value(form.memo())}</textarea>
                @if(errors.has("memo"))<p class="field-error">${errors.get("memo")}</p>@endif
            </div>
        </div>
        <div class="flex justify-end gap-2 rounded-b-xl border-t border-slate-100 bg-slate-50 px-5 py-3.5">
            <a href="${editing ? "/employees/" + employee.id() : "/employees"}" class="btn btn-secondary">취소</a>
            <button type="submit" class="btn btn-primary">${editing ? "저장" : "등록"}</button>
        </div>
    </form>
`)
