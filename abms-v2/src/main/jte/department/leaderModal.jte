@import java.util.List
@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@param Department department
@param List<Employee> employees
@param DepartmentTree tree
<form hx-post="/departments/${department.id()}/leader" hx-target="#modal-body">
    @template.components.modal(title = "부서장 지정", description = department.getName(), body = @`
        <label for="leaderEmployeeId" class="label">부서장</label>
        <select id="leaderEmployeeId" name="leaderEmployeeId" class="select">
            <option value="">(지정 안 함)</option>
            @for(Employee e : employees)
                <option value="${e.id()}" selected="${e.id().equals(department.getLeaderEmployeeId())}">${e.getName()} · ${e.getPosition().label()} · ${tree.nameOf(e.getDepartmentId())}</option>
            @endfor
        </select>
    `, footer = @`
        <button type="button" class="btn btn-secondary" data-close-modal>취소</button>
        <button type="submit" class="btn btn-primary">저장</button>
    `)
</form>
