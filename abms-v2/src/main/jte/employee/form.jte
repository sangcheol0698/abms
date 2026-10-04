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
@import kr.co.abacus.abms.employee.WorkType
@import kr.co.abacus.abms.employee.EmployeeJob
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param EmployeeForm form
@param FormErrors errors
@param Employee employee = null
@param List<SelectOption> departmentOptions
!{boolean editing = employee != null;}
!{String title = editing ? employee.getName() + " 정보 수정" : "직원 등록";}
@template.layout.app(ctx = ctx, title = title, content = @`
    @template.components.pageHeader(title = title, breadcrumb = @`
        <a href="/employees" class="hover:text-fg-neutral-muted">직원</a>${Icons.of("chevron-right", "size-3")}
        @if(editing)<a href="/employees/${employee.id()}" class="hover:text-fg-neutral-muted">${employee.getName()}</a>${Icons.of("chevron-right", "size-3")}@endif
        <span>${editing ? "수정" : "등록"}</span>
    `)
    <form method="post" action="${editing ? "/employees/" + employee.id() : "/employees"}" class="mx-auto max-w-4xl" novalidate>${kr.co.abacus.abms.common.web.Csrf.field()}
        @template.components.globalError(errors = errors)
        <section class="form-section">
            <div><h2 class="form-section-title">기본 정보</h2><p class="form-section-desc">이메일은 로그인 아이디로도 쓰입니다.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                @template.components.input(name = "name", label = "이름", value = Ui.value(form.name()), errors = errors, required = true)
                @template.components.input(name = "email", label = "이메일", value = Ui.value(form.email()), errors = errors, type = "email", required = true)
                @template.components.input(name = "birthDate", label = "생년월일", value = Ui.value(form.birthDate()), errors = errors, type = "date", required = true)
                @template.components.select(name = "departmentId", label = "부서", options = departmentOptions, value = Ui.value(form.departmentId()), errors = errors, required = true, emptyLabel = "부서 선택")
                @template.components.input(name = "phone", label = "연락처", value = Ui.value(form.phone()), errors = errors, type = "tel", placeholder = "010-0000-0000")
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">인사 정보</h2><p class="form-section-desc">고용유형은 원가 정책 적용 기준입니다.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                @template.components.input(name = "joinDate", label = "입사일", value = Ui.value(form.joinDate()), errors = errors, type = "date", required = true)
                @template.components.select(name = "type", label = "고용유형", options = Ui.options(EmployeeType.values()), value = Ui.value(form.type()), errors = errors, required = true)
                @template.components.select(name = "position", label = "직급", options = Ui.options(EmployeePosition.values()), value = Ui.value(form.position()), errors = errors, required = true)
                @template.components.select(name = "grade", label = "등급", options = Ui.options(EmployeeGrade.values()), value = Ui.value(form.grade()), errors = errors, required = true)
                @if(!editing)
                    @template.components.input(name = "annualSalary", label = "연봉 (원)", value = Ui.value(form.annualSalary()), errors = errors, type = "number", help = "입력하면 입사일부터 적용되는 연봉 이력이 생성됩니다.")
                @endif
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">직무·역량</h2><p class="form-section-desc">인력 배치와 검색에 쓰입니다. 총 경력은 경력 시작일부터 계산합니다.</p></div>
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                @template.components.select(name = "job", label = "직무", options = Ui.options(EmployeeJob.values()), value = Ui.value(form.job()), errors = errors, emptyLabel = "미지정")
                @template.components.select(name = "workType", label = "근무 형태", options = Ui.options(WorkType.values()), value = Ui.value(form.workType()), errors = errors, emptyLabel = "미지정")
                @template.components.input(name = "careerStartDate", label = "경력 시작일", value = Ui.value(form.careerStartDate()), errors = errors, type = "date", help = "이전 직장 경력을 포함한 첫 근무일. 비우면 입사일 기준입니다.")
                <div class="@xl:col-span-2">
                    @template.components.input(name = "skills", label = "보유 기술", value = Ui.value(form.skills()), errors = errors, placeholder = "Java, Spring Boot, AWS", help = "쉼표로 구분해 입력하세요. 중복은 자동으로 정리됩니다.")
                </div>
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">아바타</h2><p class="form-section-desc">목록과 상세 화면에 표시됩니다.</p></div>
            <div>@template.employee.avatarPicker(selected = form.avatar() == null ? EmployeeAvatar.SKY_GLOW : form.avatar(), initial = form.name() == null || form.name().isBlank() ? "?" : form.name().substring(0, 1))</div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">메모</h2><p class="form-section-desc">관리자만 볼 수 있는 참고 사항입니다.</p></div>
            <div>
                <label for="memo" class="sr-only">메모</label>
                <textarea id="memo" name="memo" rows="3" class="textarea">${Ui.value(form.memo())}</textarea>
                @if(errors.has("memo"))<p class="field-error">${errors.get("memo")}</p>@endif
            </div>
        </section>
        <div class="form-actions">
            <a href="${editing ? "/employees/" + employee.id() : "/employees"}" class="${Seed.button("neutralOutline")}">취소</a>
            <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "등록"}</button>
        </div>
    </form>
`)
