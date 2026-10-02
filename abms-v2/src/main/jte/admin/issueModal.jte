@import java.util.List
@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@param List<Employee> candidates
@param DepartmentTree tree
@param Long selected = null
@param FormErrors errors
<form hx-post="/admin/accounts/issue" hx-target="#modal-body">
    @template.components.modal(title = "계정 발급", description = "직원 이메일이 아이디가 되며, 임시 비밀번호가 한 번만 표시됩니다.", body = @`
        @template.components.globalError(errors = errors)
        @if(candidates.isEmpty())
            <p class="text-sm text-slate-500">계정이 없는 재직 직원이 없습니다.</p>
        @else
            <label for="employeeId" class="label">직원</label>
            <select id="employeeId" name="employeeId" class="select" required>
                <option value="">직원 선택</option>
                @for(Employee e : candidates)
                    <option value="${e.id()}" selected="${e.id().equals(selected)}">${e.getName()} · ${tree.nameOf(e.getDepartmentId())} · ${e.getEmail()}</option>
                @endfor
            </select>
            <p class="field-help">발급된 계정에는 기본 권한 그룹(일반 사용자)이 할당됩니다.</p>
        @endif
    `, footer = @`
        <button type="button" class="btn btn-secondary" data-close-modal>취소</button>
        <button type="submit" class="btn btn-primary" disabled="${candidates.isEmpty()}">발급</button>
    `)
</form>
