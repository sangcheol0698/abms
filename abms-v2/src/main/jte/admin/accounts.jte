@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.account.Account
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@param ViewContext ctx
@param List<Account> accounts
@param Map<Long, Employee> employees
@param Map<Long, List<String>> accountGroups
@param DepartmentTree tree
@param Long issueEmployeeId = null
@template.layout.app(ctx = ctx, title = "계정 관리", content = @`
    @template.components.pageHeader(title = "계정 관리", subtitle = "직원에게 로그인 계정을 발급하고 비밀번호·잠금 상태를 관리합니다.", actions = @`
        <button type="button" class="btn btn-primary" hx-get="/admin/accounts/issue${issueEmployeeId == null ? "" : "?employeeId=" + issueEmployeeId}" hx-target="#modal-body"
                hx-trigger="click${issueEmployeeId == null ? "" : ", load"}">${Icons.of("plus", "size-4")} 계정 발급</button>
    `)
    <div class="card overflow-hidden" hx-get="/admin/accounts" hx-trigger="accountsChanged from:body" hx-select="#account-table" hx-target="#account-table" hx-swap="outerHTML">
        <div id="account-table" class="overflow-x-auto">
            <table class="table">
                <thead><tr><th>직원</th><th>아이디</th><th>권한 그룹</th><th>상태</th><th>최근 로그인</th><th>비밀번호 변경</th><th></th></tr></thead>
                <tbody class="divide-y divide-slate-100">
                @for(Account a : accounts)
                    !{Employee e = employees.get(a.getEmployeeId());}
                    <tr>
                        <td>
                            @if(e != null)
                                <a href="/employees/${e.id()}" class="flex items-center gap-2.5">
                                    @template.components.avatar(avatar = e.getAvatar(), initial = e.initial(), size = "size-7 text-xs")
                                    <span><span class="block font-medium text-slate-900">${e.getName()}</span><span class="text-xs text-slate-500">${tree.nameOf(e.getDepartmentId())}</span></span>
                                </a>
                            @endif
                        </td>
                        <td class="text-slate-600">${a.getUsername()}</td>
                        <td>
                            <div class="flex flex-wrap gap-1">
                                @for(String g : accountGroups.getOrDefault(a.id(), List.of()))
                                    <span class="badge badge-blue">${g}</span>
                                @endfor
                            </div>
                        </td>
                        <td>
                            @if(!a.isEnabled())
                                <span class="badge badge-gray">비활성</span>
                            @elseif(a.isLocked())
                                <span class="badge badge-red">${Icons.of("lock", "size-3")} 잠김</span>
                            @else
                                <span class="badge badge-green">사용 중</span>
                            @endif
                        </td>
                        <td class="text-xs text-slate-500">${Ui.dateTime(a.getLastLoginAt())}</td>
                        <td class="text-xs text-slate-500">${Ui.dateTime(a.getPasswordChangedAt())}</td>
                        <td class="text-right">
                            <details class="relative inline-block text-left" data-dropdown>
                                <summary class="btn btn-ghost btn-sm">관리 ${Icons.of("chevron-down", "size-3")}</summary>
                                <div class="dropdown-panel w-44 py-1.5">
                                    <button type="button" class="block w-full px-4 py-2 text-left text-sm hover:bg-slate-50"
                                            hx-post="/admin/accounts/${a.id()}/reset-password" hx-target="#modal-body" hx-confirm="${a.getUsername()} 비밀번호를 초기화할까요?">비밀번호 초기화</button>
                                    @if(a.isLocked())
                                        <form method="post" action="/admin/accounts/${a.id()}/unlock"><button class="block w-full px-4 py-2 text-left text-sm hover:bg-slate-50">잠금 해제</button></form>
                                    @endif
                                    @if(a.isEnabled())
                                        <form method="post" action="/admin/accounts/${a.id()}/disable" hx-confirm="계정을 비활성화할까요? 해당 사용자는 로그인할 수 없습니다."><button class="block w-full px-4 py-2 text-left text-sm text-rose-600 hover:bg-rose-50">비활성화</button></form>
                                    @else
                                        <form method="post" action="/admin/accounts/${a.id()}/enable"><button class="block w-full px-4 py-2 text-left text-sm hover:bg-slate-50">활성화</button></form>
                                    @endif
                                </div>
                            </details>
                        </td>
                    </tr>
                @endfor
                </tbody>
            </table>
        </div>
    </div>
`)
