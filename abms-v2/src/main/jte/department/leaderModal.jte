@import java.util.List
@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.common.web.Seed
@param Department department
@param List<Employee> employees
@param DepartmentTree tree
<form hx-post="/departments/${department.id()}/leader" hx-target="#modal-body">
    @template.components.modal(title = "부서장 지정", context = department.getName(), hint = "저장", body = @`
        <div class="prop-form">
            @template.components.propRow(label = "부서장", forId = "leaderEmployeeId", control = @`
                <select id="leaderEmployeeId" name="leaderEmployeeId" class="select" autofocus>
                    <option value="">(지정 안 함)</option>
                    @for(Employee e : employees)
                        <option value="${e.id()}" selected="${e.id().equals(department.getLeaderEmployeeId())}">${e.getName()} · ${e.getPosition().label()} · ${tree.nameOf(e.getDepartmentId())}</option>
                    @endfor
                </select>
            `)
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">저장</button>
    `)
</form>
