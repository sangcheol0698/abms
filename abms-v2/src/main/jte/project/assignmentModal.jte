@import java.util.List
@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.project.AssignmentRole
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.project.ProjectAssignment
@import kr.co.abacus.abms.project.ProjectAssignmentController.AssignmentForm
@import kr.co.abacus.abms.common.web.Seed
@param Project project
@param ProjectAssignment assignment = null
@param AssignmentForm form
@param FormErrors errors
@param List<Employee> employees
@param DepartmentTree tree
!{boolean editing = assignment != null;}
<form hx-post="/projects/${project.id()}/assignments${editing ? "/" + assignment.id() : ""}" hx-target="#modal-body">
    @template.components.modal(title = editing ? "투입 정보 수정" : "인력 투입", context = project.getName(), hint = editing ? "저장" : "투입", body = @`
        @template.components.globalError(errors = errors)
        <div class="prop-form">
            @template.components.propRow(label = "직원", forId = "employeeId", required = true, error = errors.get("employeeId"), control = @`
                <select id="employeeId" name="employeeId" class="select" required autofocus>
                    <option value="">직원 선택</option>
                    @for(Employee e : employees)
                        <option value="${e.id()}" selected="${e.id().equals(form.employeeId())}">${e.getName()} · ${e.getPosition().label()} · ${tree.nameOf(e.getDepartmentId())}${e.getType() != kr.co.abacus.abms.employee.EmployeeType.FULL_TIME ? " (" + e.getType().label() + ")" : ""}</option>
                    @endfor
                </select>
            `)
            @template.components.propRow(label = "역할", forId = "role", error = errors.get("role"), control = @`
                <select id="role" name="role" class="select">
                    <option value="">-</option>
                    @for(var o : Ui.options(AssignmentRole.values()))
                        <option value="${o.value()}" selected="${o.value().equals(Ui.value(form.role()))}">${o.label()}</option>
                    @endfor
                </select>
            `)
            @template.components.propRow(label = "투입 기간", forId = "startDate", required = true, error = errors.get("startDate"),
                help = "프로젝트 기간 " + Ui.period(project.getPeriod().startDate(), project.getPeriod().endDate()) + " 안이어야 합니다.", control = @`
                <div class="flex items-center gap-x2">
                    <input id="startDate" name="startDate" type="date" class="input" value="${Ui.value(form.startDate())}" required aria-label="투입 시작일">
                    <span class="text-fg-neutral-subtle">~</span>
                    <input id="endDate" name="endDate" type="date" class="input" value="${Ui.value(form.endDate())}" aria-label="투입 종료일">
                </div>
            `)
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "투입"}</button>
    `)
</form>
