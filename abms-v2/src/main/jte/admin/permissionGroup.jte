@import java.util.List
@import java.util.Map
@import java.util.Set
@import kr.co.abacus.abms.access.Permission
@import kr.co.abacus.abms.access.PermissionGroup
@import kr.co.abacus.abms.access.PermissionScope
@import kr.co.abacus.abms.account.Account
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.employee.Employee
@param ViewContext ctx
@param PermissionGroup group
@param List<Permission> permissions
@param Map<Long, Set<PermissionScope>> grants
@param List<Account> members
@param List<Account> candidates
@param Map<Long, Employee> employees
!{boolean readOnly = group.isSystem();}
@template.layout.app(ctx = ctx, title = group.getName(), content = @`
    @template.components.pageHeader(title = group.getName(), subtitle = readOnly ? "시스템 그룹은 권한 구성을 변경할 수 없고 계정 할당만 가능합니다." : "변경 내용은 해당 사용자가 다시 로그인하면 반영됩니다.", breadcrumb = @`
        <a href="/admin/permission-groups" class="hover:text-slate-700">권한 그룹</a>${Icons.of("chevron-right", "size-3")}<span>${group.getName()}</span>
    `, actions = @`
        @if(!readOnly)
            <form method="post" action="/admin/permission-groups/${group.id()}/delete" hx-confirm="${group.getName()} 그룹을 삭제할까요? 할당된 계정에서 권한이 제거됩니다.">
                <button class="btn btn-danger-ghost">${Icons.of("trash", "size-4")} 그룹 삭제</button>
            </form>
        @endif
    `)
    <div class="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <form method="post" action="/admin/permission-groups/${group.id()}" class="card overflow-hidden xl:col-span-2">
            <div class="card-body grid grid-cols-1 gap-4 border-b border-slate-100 sm:grid-cols-2">
                <div>
                    <label for="name" class="label">그룹명</label>
                    <input id="name" name="name" class="input" value="${group.getName()}" disabled="${readOnly}" required>
                </div>
                <div>
                    <label for="description" class="label">설명</label>
                    <input id="description" name="description" class="input" value="${group.getDescription()}" disabled="${readOnly}">
                </div>
            </div>
            <div class="overflow-x-auto">
                <table class="table">
                    <thead>
                    <tr>
                        <th>권한</th>
                        @for(PermissionScope scope : PermissionScope.values())
                            <th class="text-center">${scope.label()}</th>
                        @endfor
                    </tr>
                    </thead>
                    <tbody class="divide-y divide-slate-100">
                    @for(Permission p : permissions)
                        !{Set<PermissionScope> granted = grants.getOrDefault(p.id(), Set.of());}
                        <tr>
                            <td class="min-w-64 !whitespace-normal">
                                <p class="font-medium text-slate-900">${p.getName()}</p>
                                <p class="text-xs text-slate-500"><code>${p.getCode()}</code> · ${p.getDescription()}</p>
                            </td>
                            @for(PermissionScope scope : PermissionScope.values())
                                <td class="text-center">
                                    <input type="checkbox" class="checkbox" name="grant" value="${p.id()}:${scope.name()}"
                                           checked="${granted.contains(scope)}" disabled="${readOnly}" aria-label="${p.getName()} ${scope.label()}">
                                </td>
                            @endfor
                        </tr>
                    @endfor
                    </tbody>
                </table>
            </div>
            @if(!readOnly)
                <div class="flex items-center justify-between gap-2 border-t border-slate-100 bg-slate-50 px-5 py-3.5">
                    <p class="text-xs text-slate-500">범위가 여러 개면 합집합으로 적용됩니다. 전체(ALL)는 다른 범위를 포함합니다.</p>
                    <button type="submit" class="btn btn-primary">저장</button>
                </div>
            @endif
        </form>

        <section class="card h-fit overflow-hidden">
            <div class="card-header"><h2 class="card-title">할당된 계정 (${members.size()})</h2></div>
            @if(!candidates.isEmpty())
                <form method="post" action="/admin/permission-groups/${group.id()}/members" class="flex gap-2 border-b border-slate-100 p-4">
                    <select name="accountId" class="select" required aria-label="추가할 계정">
                        <option value="">계정 선택</option>
                        @for(Account a : candidates)
                            !{Employee e = employees.get(a.getEmployeeId());}
                            <option value="${a.id()}">${e == null ? a.getUsername() : e.getName() + " (" + a.getUsername() + ")"}</option>
                        @endfor
                    </select>
                    <button class="btn btn-secondary">추가</button>
                </form>
            @endif
            @if(members.isEmpty())
                @template.components.empty(message = "할당된 계정이 없습니다.", icon = "users")
            @else
                <ul class="divide-y divide-slate-100">
                    @for(Account a : members)
                        !{Employee e = employees.get(a.getEmployeeId());}
                        <li class="flex items-center justify-between gap-3 px-5 py-3">
                            <div class="flex min-w-0 items-center gap-2.5">
                                @if(e != null)
                                    @template.components.avatar(avatar = e.getAvatar(), initial = e.initial(), size = "size-7 text-xs")
                                @endif
                                <div class="min-w-0">
                                    <p class="text-sm font-medium text-slate-900">${e == null ? "-" : e.getName()}</p>
                                    <p class="truncate text-xs text-slate-500">${a.getUsername()}</p>
                                </div>
                            </div>
                            <form method="post" action="/admin/permission-groups/${group.id()}/members/${a.id()}/delete" hx-confirm="그룹에서 제외할까요?">
                                <button class="btn btn-ghost btn-sm" aria-label="제외">${Icons.of("x", "size-4")}</button>
                            </form>
                        </li>
                    @endfor
                </ul>
            @endif
        </section>
    </div>
`)
