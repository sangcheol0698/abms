@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.employee.EmployeeSearchForm
@param PageView<Employee> page
@param DepartmentTree tree
@param EmployeeSearchForm search
@if(page.isEmpty())
    @template.components.empty(message = "조건에 맞는 직원이 없습니다.", icon = "users")
@else
    <div class="overflow-x-auto">
        <table class="table">
            <thead>
            <tr><th>이름</th><th>부서</th><th>직급</th><th>고용유형</th><th>상태</th><th>입사일</th></tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
            @for(Employee e : page.content())
                <tr>
                    <td>
                        <a href="/employees/${e.id()}" class="flex items-center gap-3">
                            @template.components.avatar(avatar = e.getAvatar(), initial = e.initial())
                            <span class="min-w-0">
                                <span class="block font-medium text-slate-900 hover:text-brand-600">${e.getName()}</span>
                                <span class="block truncate text-xs text-slate-500">${e.isDeleted() ? e.originalEmail() : e.getEmail()}</span>
                            </span>
                        </a>
                    </td>
                    <td>${tree.nameOf(e.getDepartmentId())}</td>
                    <td>${e.getPosition().label()} <span class="text-xs text-slate-400">${e.getGrade().label()}</span></td>
                    <td><span class="${Ui.badge(e.getType())}">${e.getType().label()}</span></td>
                    <td>
                        @if(e.isDeleted())
                            <span class="badge badge-red">삭제됨</span>
                        @else
                            <span class="${Ui.badge(e.getStatus())}">${e.getStatus().label()}</span>
                        @endif
                    </td>
                    <td class="tabular-nums">${Ui.date(e.getJoinDate())}</td>
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
@endif
@template.components.pagination(page = page, target = "#employee-results")
