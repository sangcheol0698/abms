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
    @template.components.modal(title = editing ? "투입 정보 수정" : "인력 투입", description = project.getName() + " · " + Ui.period(project.getPeriod().startDate(), project.getPeriod().endDate()), body = @`
        @template.components.globalError(errors = errors)
        <div class="grid grid-cols-2 gap-4">
            <div class="col-span-2">
                <label for="employeeId" class="label">직원</label>
                <select id="employeeId" name="employeeId" class="select" required>
                    <option value="">직원 선택</option>
                    @for(Employee e : employees)
                        <option value="${e.id()}" selected="${e.id().equals(form.employeeId())}">${e.getName()} · ${e.getPosition().label()} · ${tree.nameOf(e.getDepartmentId())}${e.getType() != kr.co.abacus.abms.employee.EmployeeType.FULL_TIME ? " (" + e.getType().label() + ")" : ""}</option>
                    @endfor
                </select>
            </div>
            @template.components.select(name = "role", label = "역할", options = Ui.options(AssignmentRole.values()), value = Ui.value(form.role()), errors = errors)
            <div></div>
            <div>
                <label for="startDate" class="label">투입 시작일</label>
                <input id="startDate" name="startDate" type="date" class="input" value="${Ui.value(form.startDate())}" required>
            </div>
            <div>
                <label for="endDate" class="label">투입 종료일</label>
                <input id="endDate" name="endDate" type="date" class="input" value="${Ui.value(form.endDate())}">
            </div>
        </div>
        <p class="field-help mt-3">투입 기간은 프로젝트 기간 안이어야 하며, 퇴사자는 퇴사일을 넘길 수 없습니다.</p>
    `, footer = @`
        <button type="button" class="${Seed.button("neutralWeak")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "투입"}</button>
    `)
</form>
