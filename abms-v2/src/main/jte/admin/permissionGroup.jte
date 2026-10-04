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
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param PermissionGroup group
@param List<Permission> permissions
@param Map<Long, Set<PermissionScope>> grants
@param List<Account> members
@param List<Account> candidates
@param Map<Long, Employee> employees
@param java.util.List<kr.co.abacus.abms.common.audit.AuditEntry> auditHistory = null
!{boolean readOnly = group.isSystem();}
@template.layout.app(ctx = ctx, aside = @`
            <h2 class="section-title">할당된 계정 <span class="section-count">${members.size()}</span></h2>
            @if(!candidates.isEmpty())
                <form method="post" action="/admin/permission-groups/${group.id()}/members" class="mb-x3 flex gap-x2">${kr.co.abacus.abms.common.web.Csrf.field()}
                    <select name="accountId" class="select" required aria-label="추가할 계정">
                        <option value="">계정 선택</option>
                        @for(Account a : candidates)
                            !{Employee e = employees.get(a.getEmployeeId());}
                            <option value="${a.id()}">${e == null ? a.getUsername() : e.getName() + " (" + a.getUsername() + ")"}</option>
                        @endfor
                    </select>
                    <button class="${Seed.button("neutralOutline")}">추가</button>
                </form>
            @endif
            <div class="list-box">
                @if(members.isEmpty())
                    @template.components.empty(message = "할당된 계정이 없습니다.", icon = "users")
                @else
                    @for(Account a : members)
                        !{Employee e = employees.get(a.getEmployeeId());}
                        <div class="list-row">
                            @if(e != null)
                                @template.components.avatar(avatar = e.getAvatar(), initial = e.initial(), size = "size-6 text-[11px]")
                            @endif
                            <span class="min-w-0 flex-1">
                                <span class="block truncate t4-medium">${e == null ? "-" : e.getName()}</span>
                                <span class="block truncate t3-regular text-fg-neutral-subtle">${a.getUsername()}</span>
                            </span>
                            <form method="post" action="/admin/permission-groups/${group.id()}/members/${a.id()}/delete" hx-confirm="그룹에서 제외할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                                <button class="${Seed.iconButton("ghost", "xsmall")}" aria-label="${a.getUsername()} 제외" title="그룹에서 제외">${Icons.of("x", "size-4")}</button>
                            </form>
                        </div>
                    @endfor
                @endif
            </div>
`, title = group.getName(), content = @`
    @template.components.pageHeader(title = group.getName(), subtitle = readOnly ? "시스템 그룹은 권한 구성을 변경할 수 없고 계정 할당만 가능합니다." : "변경 내용은 해당 사용자가 다시 로그인하면 반영됩니다.", breadcrumb = @`
        <a href="/admin/permission-groups" class="hover:text-fg-neutral-muted">권한 그룹</a>${Icons.of("chevron-right", "size-3")}<span>${group.getName()}</span>
    `, actions = @`
        @if(!readOnly)
            <form method="post" action="/admin/permission-groups/${group.id()}/delete" hx-confirm="${group.getName()} 그룹을 삭제할까요? 할당된 계정에서 권한이 제거됩니다.">${kr.co.abacus.abms.common.web.Csrf.field()}
                <button class="${Seed.button("neutralOutline")} text-fg-critical">${Icons.of("trash", "size-4")} 그룹 삭제</button>
            </form>
        @endif
    `)
        <form method="post" action="/admin/permission-groups/${group.id()}" class="min-w-0 space-y-6">${kr.co.abacus.abms.common.web.Csrf.field()}
            <div class="grid grid-cols-1 gap-x4 @xl:grid-cols-2">
                <div>
                    <label for="name" class="label">그룹명</label>
                    <input id="name" name="name" class="input" value="${group.getName()}" disabled="${readOnly}" required>
                </div>
                <div>
                    <label for="description" class="label">설명</label>
                    <input id="description" name="description" class="input" value="${group.getDescription()}" disabled="${readOnly}">
                </div>
            </div>
            <section>
                <h2 class="section-title">권한 × 범위 <span class="section-count">범위가 여러 개면 합집합 · 전체는 다른 범위를 포함</span></h2>
                <div class="list-box overflow-x-auto">
                    <table class="table">
                        <thead>
                        <tr>
                            <th class="bg-bg-neutral-muted">권한</th>
                            @for(PermissionScope scope : PermissionScope.values())
                                <th class="bg-bg-neutral-muted text-center">${scope.label()}</th>
                            @endfor
                        </tr>
                        </thead>
                        <tbody>
                        @for(Permission p : permissions)
                            !{Set<PermissionScope> granted = grants.getOrDefault(p.id(), Set.of());}
                            <tr>
                                <td class="min-w-64 !whitespace-normal">
                                    <p class="t4-medium text-fg-neutral">${p.getName()}</p>
                                    <p class="t3-regular text-fg-neutral-subtle"><code>${p.getCode()}</code> · ${p.getDescription()}</p>
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
            </section>
            @if(!readOnly)
                <div class="sticky bottom-4 flex items-center justify-end gap-x2 rounded-r3 border border-stroke-neutral-subtle bg-bg-layer-floating px-x4 py-x2_5 shadow-s2">
                    <p class="mr-auto t3-regular text-fg-neutral-subtle">변경 내용은 해당 사용자가 다시 로그인하면 반영됩니다.</p>
                    <button type="submit" class="${Seed.button("brandSolid")}">저장</button>
                </div>
            @endif
        </form>
        @if(auditHistory != null)
            <div class="mt-6">@template.components.auditHistory(entries = auditHistory, showTarget = true)</div>
        @endif

`)
