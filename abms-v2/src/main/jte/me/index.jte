@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.access.Permission
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.access.PermissionScope
@import kr.co.abacus.abms.account.Account
@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.project.ProjectAssignment
@param ViewContext ctx
@param Employee employee
@param Account account
@param DepartmentTree tree
@param List<ProjectAssignment> assignments
@param Map<Long, String> projectNames
@param java.util.Set<Long> readableProjectIds
@param List<Permission> permissions
@param FormErrors errors
@template.layout.app(ctx = ctx, title = "내 정보", content = @`
    @template.components.pageHeader(title = "내 정보", actions = @`
        <a href="/employees/${employee.id()}" class="btn btn-secondary">${Icons.of("user", "size-4")} 직원 상세 보기</a>
    `)
    <div class="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <div class="space-y-4">
            <section class="card card-body">
                <div class="flex items-center gap-4">
                    @template.components.avatar(avatar = employee.getAvatar(), initial = employee.initial(), size = "size-14 text-xl")
                    <div>
                        <p class="text-lg font-semibold text-slate-900">${employee.getName()}</p>
                        <p class="text-sm text-slate-500">${tree.nameOf(employee.getDepartmentId())} · ${employee.getPosition().label()}</p>
                    </div>
                </div>
                <dl class="mt-5 space-y-2 text-sm">
                    <div class="flex justify-between"><dt class="text-slate-500">아이디</dt><dd>${account.getUsername()}</dd></div>
                    <div class="flex justify-between"><dt class="text-slate-500">최근 로그인</dt><dd>${Ui.dateTime(account.getLastLoginAt())}</dd></div>
                    <div class="flex justify-between"><dt class="text-slate-500">비밀번호 변경</dt><dd>${Ui.dateTime(account.getPasswordChangedAt())}</dd></div>
                </dl>
            </section>
            <section class="card" id="password-card">
                @template.me.passwordForm(errors = errors, done = false)
            </section>
        </div>

        <div class="space-y-4 xl:col-span-2">
            <section class="card overflow-hidden">
                <div class="card-header"><h2 class="card-title">참여 프로젝트</h2></div>
                @if(assignments.isEmpty())
                    @template.components.empty(message = "최근 참여한 프로젝트가 없습니다.")
                @else
                    <ul class="divide-y divide-slate-100">
                        @for(ProjectAssignment a : assignments)
                            <li class="flex items-center justify-between gap-3 px-5 py-3">
                                <div>
                                    @if(readableProjectIds.contains(a.getProjectId()))
                                        <a href="/projects/${a.getProjectId()}" class="link">${projectNames.get(a.getProjectId())}</a>
                                    @else
                                        <span class="font-medium">${projectNames.get(a.getProjectId())}</span>
                                    @endif
                                    <p class="text-xs text-slate-500">${a.getRole() == null ? "" : a.getRole().label() + " · "}${Ui.period(a.getPeriod().startDate(), a.getPeriod().endDate())}</p>
                                </div>
                                @if(a.isActiveOn(java.time.LocalDate.now()))
                                    <span class="badge badge-blue">투입 중</span>
                                @elseif(a.getPeriod().startDate().isAfter(java.time.LocalDate.now()))
                                    <span class="badge badge-purple">예정</span>
                                @else
                                    <span class="badge badge-gray">종료</span>
                                @endif
                            </li>
                        @endfor
                    </ul>
                @endif
            </section>

            <section class="card overflow-hidden">
                <div class="card-header">
                    <h2 class="card-title">내 권한</h2>
                    <span class="text-xs text-slate-500">로그인 시점 기준</span>
                </div>
                <ul class="divide-y divide-slate-100">
                    @for(Permission p : permissions)
                        !{var code = PermissionCode.fromCode(p.getCode());}
                        !{java.util.Set<PermissionScope> scopes = code.isPresent() ? ctx.requireUser().scopes(code.get()) : java.util.Set.of();}
                        <li class="flex items-center justify-between gap-3 px-5 py-2.5 text-sm">
                            <span class="${scopes.isEmpty() ? "text-slate-400" : "text-slate-700"}">${p.getName()}</span>
                            <span class="flex flex-wrap justify-end gap-1">
                                @if(scopes.isEmpty())
                                    <span class="text-xs text-slate-400">없음</span>
                                @else
                                    @for(PermissionScope s : scopes)
                                        <span class="badge badge-blue">${s.label()}</span>
                                    @endfor
                                @endif
                            </span>
                        </li>
                    @endfor
                </ul>
            </section>
        </div>
    </div>
`)
