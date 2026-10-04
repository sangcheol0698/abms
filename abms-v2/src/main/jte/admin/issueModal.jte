@import java.util.List
@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.common.web.Seed
@param List<Employee> candidates
@param DepartmentTree tree
@param Long selected = null
@param FormErrors errors
<form hx-post="/admin/accounts/issue" hx-target="#modal-body">
    @template.components.modal(title = "계정 발급", context = "계정 관리", hint = candidates.isEmpty() ? null : "발급",
        description = "직원 이메일이 아이디가 되며, 임시 비밀번호가 한 번만 표시됩니다.", body = @`
        @template.components.globalError(errors = errors)
        @if(candidates.isEmpty())
            @template.components.empty(message = "계정이 없는 재직 직원이 없습니다.", icon = "users")
        @else
            <div class="prop-form">
                @template.components.propRow(label = "직원", forId = "employeeId", required = true, error = errors.get("employeeId"),
                    help = "발급된 계정에는 기본 권한 그룹(일반 사용자)이 할당됩니다.", control = @`
                    <select id="employeeId" name="employeeId" class="select" required autofocus>
                        <option value="">직원 선택</option>
                        @for(Employee e : candidates)
                            <option value="${e.id()}" selected="${e.id().equals(selected)}">${e.getName()} · ${tree.nameOf(e.getDepartmentId())} · ${e.getEmail()}</option>
                        @endfor
                    </select>
                `)
            </div>
        @endif
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}" disabled="${candidates.isEmpty()}">발급</button>
    `)
</form>
