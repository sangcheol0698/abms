@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.account.Account
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.employee.EmployeeStatus
@import kr.co.abacus.abms.employee.Payroll
@import kr.co.abacus.abms.employee.PositionHistory
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.project.ProjectAssignment
@param ViewContext ctx
@param Employee employee
@param DepartmentTree tree
@param List<ProjectAssignment> assignments
@param Map<Long, Project> projects
@param List<Payroll> payrolls
@param List<PositionHistory> positions
@param boolean canWrite
@param boolean canEditOwn
@param Account account = null
@template.layout.app(ctx = ctx, title = employee.getName(), content = @`
    @template.components.pageHeader(title = employee.getName(), breadcrumb = @`
        <a href="/employees" class="hover:text-slate-700">직원</a>${Icons.of("chevron-right", "size-3")}<span>${employee.getName()}</span>
    `, actions = @`
        @if(employee.isDeleted())
            @if(canWrite)
                <form method="post" action="/employees/${employee.id()}/restore" hx-confirm="${employee.getName()} 직원을 복구할까요?">
                    <button class="btn btn-primary">${Icons.of("refresh", "size-4")} 복구</button>
                </form>
            @endif
        @else
            @if(canWrite || canEditOwn)
                <a href="/employees/${employee.id()}/edit" class="btn btn-secondary">${Icons.of("pencil", "size-4")} 정보 수정</a>
            @endif
            @if(canWrite)
                <details class="relative" data-dropdown>
                    <summary class="btn btn-secondary">상태 변경 ${Icons.of("chevron-down", "size-4")}</summary>
                    <div class="dropdown-panel w-48 py-1.5">
                        @if(employee.getStatus() != EmployeeStatus.RESIGNED)
                            <button type="button" class="block w-full px-4 py-2 text-left text-sm hover:bg-slate-50" hx-get="/employees/${employee.id()}/promote" hx-target="#modal-body">승진 / 등급 변경</button>
                            <button type="button" class="block w-full px-4 py-2 text-left text-sm hover:bg-slate-50" hx-get="/employees/${employee.id()}/salary" hx-target="#modal-body">연봉 변경</button>
                        @endif
                        @if(employee.getStatus() == EmployeeStatus.ACTIVE)
                            <form method="post" action="/employees/${employee.id()}/leave" hx-confirm="휴직 처리할까요?">
                                <button class="block w-full px-4 py-2 text-left text-sm hover:bg-slate-50">휴직 처리</button>
                            </form>
                        @endif
                        @if(employee.getStatus() != EmployeeStatus.ACTIVE)
                            <form method="post" action="/employees/${employee.id()}/activate" hx-confirm="재직 상태로 변경할까요?">
                                <button class="block w-full px-4 py-2 text-left text-sm hover:bg-slate-50">${employee.isResigned() ? "재입사 처리" : "복직 처리"}</button>
                            </form>
                        @endif
                        @if(employee.getStatus() != EmployeeStatus.RESIGNED)
                            <button type="button" class="block w-full px-4 py-2 text-left text-sm hover:bg-slate-50" hx-get="/employees/${employee.id()}/resign" hx-target="#modal-body">퇴사 처리</button>
                        @endif
                        <div class="my-1 border-t border-slate-100"></div>
                        <form method="post" action="/employees/${employee.id()}/delete" hx-confirm="${employee.getName()} 직원을 삭제할까요? 삭제 후에도 복구할 수 있습니다.">
                            <button class="block w-full px-4 py-2 text-left text-sm text-rose-600 hover:bg-rose-50">삭제</button>
                        </form>
                    </div>
                </details>
            @endif
        @endif
    `)

    @if(employee.isDeleted())
        <div class="mb-4 rounded-lg bg-rose-50 px-4 py-3 text-sm text-rose-700 ring-1 ring-rose-200">삭제된 직원입니다. (${Ui.dateTime(employee.getDeletedAt())})</div>
    @endif

    <div class="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <section class="card xl:col-span-1">
            <div class="card-body">
                <div class="flex items-center gap-4">
                    @template.components.avatar(avatar = employee.getAvatar(), initial = employee.initial(), size = "size-16 text-2xl")
                    <div class="min-w-0">
                        <p class="text-lg font-semibold text-slate-900">${employee.getName()}</p>
                        <p class="text-sm text-slate-500">${employee.getPosition().label()} · ${employee.getGrade().label()}</p>
                        <div class="mt-1.5 flex flex-wrap gap-1.5">
                            <span class="${Ui.badge(employee.getStatus())}">${employee.getStatus().label()}</span>
                            <span class="${Ui.badge(employee.getType())}">${employee.getType().label()}</span>
                        </div>
                    </div>
                </div>
                <dl class="mt-6 space-y-3 text-sm [&_dt]:shrink-0">
                    <div class="flex justify-between gap-4"><dt class="text-slate-500">부서</dt>
                        <dd class="text-right">
                            @for(Department d : tree.path(employee.getDepartmentId()))
                                @if(d.id().equals(employee.getDepartmentId()))
                                    <a class="link" href="/departments?selected=${d.id()}">${d.getName()}</a>
                                @else
                                    <span class="text-slate-400">${d.getName()} ›</span>
                                @endif
                            @endfor
                        </dd>
                    </div>
                    <div class="flex justify-between gap-4"><dt class="text-slate-500">이메일</dt><dd class="truncate">${employee.isDeleted() ? employee.originalEmail() : employee.getEmail()}</dd></div>
                    <div class="flex justify-between gap-4"><dt class="text-slate-500">입사일</dt><dd class="tabular-nums">${Ui.date(employee.getJoinDate())}</dd></div>
                    @if(employee.getResignationDate() != null)
                        <div class="flex justify-between gap-4"><dt class="text-slate-500">퇴사일</dt><dd class="tabular-nums">${Ui.date(employee.getResignationDate())}</dd></div>
                    @endif
                    <div class="flex justify-between gap-4"><dt class="text-slate-500">생년월일</dt><dd class="tabular-nums">${Ui.date(employee.getBirthDate())}</dd></div>
                    @if(ctx.can(PermissionCode.ACCOUNT_MANAGE))
                        <div class="flex justify-between gap-4"><dt class="text-slate-500">계정</dt>
                            <dd>
                                @if(account != null)
                                    <a href="/admin/accounts" class="link">${account.getUsername()}</a>
                                @else
                                    <a href="/admin/accounts?issue=${employee.id()}" class="link">계정 발급</a>
                                @endif
                            </dd>
                        </div>
                    @endif
                </dl>
                @if(employee.getMemo() != null)
                    <div class="mt-5 rounded-lg bg-slate-50 p-3 text-sm whitespace-pre-line text-slate-600">${employee.getMemo()}</div>
                @endif
            </div>
        </section>

        <div class="space-y-4 xl:col-span-2">
            <section class="card overflow-hidden">
                <div class="card-header"><h2 class="card-title">프로젝트 투입 이력</h2></div>
                @if(assignments.isEmpty())
                    @template.components.empty(message = "투입 이력이 없습니다.")
                @else
                    <div class="overflow-x-auto">
                        <table class="table">
                            <thead><tr><th>프로젝트</th><th>역할</th><th>기간</th><th>상태</th></tr></thead>
                            <tbody class="divide-y divide-slate-100">
                            @for(ProjectAssignment a : assignments)
                                !{Project p = projects.get(a.getProjectId());}
                                <tr>
                                    <td>
                                        @if(ctx.can(PermissionCode.PROJECT_READ))
                                            <a class="link" href="/projects/${p.id()}">${p.getName()}</a>
                                        @else
                                            ${p.getName()}
                                        @endif
                                        <p class="text-xs text-slate-400">${p.getCode()}</p>
                                    </td>
                                    <td>${a.getRole() == null ? "-" : a.getRole().label()}</td>
                                    <td class="tabular-nums">${Ui.period(a.getPeriod().startDate(), a.getPeriod().endDate())}</td>
                                    <td>
                                        @if(a.isActiveOn(java.time.LocalDate.now()))
                                            <span class="badge badge-blue">투입 중</span>
                                        @elseif(a.getPeriod().startDate().isAfter(java.time.LocalDate.now()))
                                            <span class="badge badge-purple">예정</span>
                                        @else
                                            <span class="badge badge-gray">종료</span>
                                        @endif
                                    </td>
                                </tr>
                            @endfor
                            </tbody>
                        </table>
                    </div>
                @endif
            </section>

            <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
                <section class="card overflow-hidden">
                    <div class="card-header">
                        <h2 class="card-title">연봉 이력</h2>
                        @if(canWrite && !employee.isDeleted() && !employee.isResigned())
                            <button type="button" class="btn btn-ghost btn-sm" hx-get="/employees/${employee.id()}/salary" hx-target="#modal-body">${Icons.of("plus", "size-4")} 연봉 변경</button>
                        @endif
                    </div>
                    @if(payrolls.isEmpty())
                        @template.components.empty(message = "등록된 연봉이 없습니다. 손익 집계에서 원가가 0원으로 계산됩니다.", icon = "money")
                    @else
                        <ul class="divide-y divide-slate-100">
                            @for(Payroll p : payrolls)
                                <li class="flex items-center justify-between px-5 py-3 text-sm">
                                    <span class="text-slate-500 tabular-nums">${Ui.period(p.getPeriod().startDate(), p.getPeriod().endDate())}</span>
                                    <span class="font-semibold tabular-nums">${Ui.won(p.getAnnualSalary())}원</span>
                                </li>
                            @endfor
                        </ul>
                    @endif
                </section>
                <section class="card overflow-hidden">
                    <div class="card-header"><h2 class="card-title">직급 이력</h2></div>
                    @if(positions.isEmpty())
                        @template.components.empty(message = "직급 이력이 없습니다.")
                    @else
                        <ul class="divide-y divide-slate-100">
                            @for(PositionHistory h : positions)
                                <li class="flex items-center justify-between px-5 py-3 text-sm">
                                    <span class="text-slate-500 tabular-nums">${Ui.period(h.getPeriod().startDate(), h.getPeriod().endDate())}</span>
                                    <span class="font-medium">${h.getPosition().label()} · ${h.getGrade().label()}</span>
                                </li>
                            @endfor
                        </ul>
                    @endif
                </section>
            </div>
        </div>
    </div>
`)
