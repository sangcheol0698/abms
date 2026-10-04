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
@import kr.co.abacus.abms.common.web.Seed
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
@template.layout.app(ctx = ctx, aside = @`
            <div class="mb-x5 flex items-center gap-x3">
                @template.components.avatar(avatar = employee.getAvatar(), initial = employee.initial(), size = "size-10 text-base")
                <div class="min-w-0">
                    <p class="truncate t5-bold text-fg-neutral">${employee.getName()}</p>
                    <p class="truncate t3-regular text-fg-neutral-subtle">${employee.getPosition().label()} · ${employee.getGrade().label()}</p>
                </div>
            </div>
            <dl class="property-list">
                <dt>상태</dt>
                <dd><span class="${Ui.badge(employee.getStatus())}">${employee.getStatus().label()}</span></dd>
                <dt>고용유형</dt>
                <dd><span class="${Ui.badge(employee.getType())}">${employee.getType().label()}</span></dd>
                <dt>부서</dt>
                <dd class="whitespace-normal">
                    @for(Department d : tree.path(employee.getDepartmentId()))
                        @if(d.id().equals(employee.getDepartmentId()))
                            <a class="link" href="/departments?selected=${d.id()}">${d.getName()}</a>
                        @else
                            <span class="text-fg-neutral-subtle">${d.getName()} ›</span>
                        @endif
                    @endfor
                </dd>
                <dt>이메일</dt>
                <dd title="${employee.isDeleted() ? employee.originalEmail() : employee.getEmail()}">${employee.isDeleted() ? employee.originalEmail() : employee.getEmail()}</dd>
                <dt>입사일</dt>
                <dd class="tabular-nums">${Ui.date(employee.getJoinDate())}</dd>
                @if(employee.getResignationDate() != null)
                    <dt>퇴사일</dt>
                    <dd class="tabular-nums">${Ui.date(employee.getResignationDate())}</dd>
                @endif
                <dt>생년월일</dt>
                <dd class="tabular-nums">${Ui.date(employee.getBirthDate())}</dd>
                @if(ctx.can(PermissionCode.ACCOUNT_MANAGE))
                    <dt>계정</dt>
                    <dd>
                        @if(account != null)
                            <a href="/admin/accounts" class="link">${account.getUsername()}</a>
                        @else
                            <a href="/admin/accounts?issue=${employee.id()}" class="link">계정 발급</a>
                        @endif
                    </dd>
                @endif
            </dl>
`, title = employee.getName(), content = @`
    @template.components.pageHeader(title = employee.getName(), breadcrumb = @`
        <a href="/employees" class="hover:text-fg-neutral-muted">직원</a>${Icons.of("chevron-right", "size-3")}<span>${employee.getName()}</span>
    `, actions = @`
        @if(employee.isDeleted())
            @if(canWrite)
                <form method="post" action="/employees/${employee.id()}/restore" hx-confirm="${employee.getName()} 직원을 복구할까요?">
                    <button class="${Seed.button("brandSolid")}">${Icons.of("refresh", "size-4")} 복구</button>
                </form>
            @endif
        @else
            @if(canWrite || canEditOwn)
                <a href="/employees/${employee.id()}/edit" class="${Seed.button("neutralOutline")}">${Icons.of("pencil", "size-4")} 정보 수정</a>
            @endif
            @if(canWrite)
                <details class="relative" data-dropdown>
                    <summary class="${Seed.button("neutralOutline")}">상태 변경 ${Icons.of("chevron-down", "size-4")}</summary>
                    <div class="dropdown-panel w-48 p-x1">
                        @if(employee.getStatus() != EmployeeStatus.RESIGNED)
                            <button type="button" class="menu-item" hx-get="/employees/${employee.id()}/promote" hx-target="#modal-body">승진 / 등급 변경</button>
                            <button type="button" class="menu-item" hx-get="/employees/${employee.id()}/salary" hx-target="#modal-body">연봉 변경</button>
                        @endif
                        @if(employee.getStatus() == EmployeeStatus.ACTIVE)
                            <form method="post" action="/employees/${employee.id()}/leave" hx-confirm="휴직 처리할까요?">
                                <button class="menu-item">휴직 처리</button>
                            </form>
                        @endif
                        @if(employee.getStatus() != EmployeeStatus.ACTIVE)
                            <form method="post" action="/employees/${employee.id()}/activate" hx-confirm="재직 상태로 변경할까요?">
                                <button class="menu-item">${employee.isResigned() ? "재입사 처리" : "복직 처리"}</button>
                            </form>
                        @endif
                        @if(employee.getStatus() != EmployeeStatus.RESIGNED)
                            <button type="button" class="menu-item" hx-get="/employees/${employee.id()}/resign" hx-target="#modal-body">퇴사 처리</button>
                        @endif
                        <div class="my-x1 border-t border-stroke-neutral-subtle"></div>
                        <form method="post" action="/employees/${employee.id()}/delete" hx-confirm="${employee.getName()} 직원을 삭제할까요? 삭제 후에도 복구할 수 있습니다.">
                            <button class="menu-item text-fg-critical">삭제</button>
                        </form>
                    </div>
                </details>
            @endif
        @endif
    `)

    @if(employee.isDeleted())
        <div role="alert" class="mb-x4 rounded-r2 bg-bg-critical-weak px-x4 py-x2_5 t4-regular text-fg-critical">삭제된 직원입니다. (${Ui.dateTime(employee.getDeletedAt())})</div>
    @endif

        <div class="min-w-0 space-y-4">
            @if(employee.getMemo() != null)
                <p class="rounded-r2 bg-bg-neutral-muted p-x3 t4-regular whitespace-pre-line text-fg-neutral-muted">${employee.getMemo()}</p>
            @endif

        <section class="card overflow-hidden">
                <div class="card-header"><h2 class="card-title">프로젝트 투입 이력</h2></div>
                @if(assignments.isEmpty())
                    @template.components.empty(message = "투입 이력이 없습니다.")
                @else
                    <div class="overflow-x-auto">
                        <table class="table">
                            <thead><tr><th>프로젝트</th><th>역할</th><th>기간</th><th>상태</th></tr></thead>
                            <tbody class="divide-y divide-stroke-neutral-subtle">
                            @for(ProjectAssignment a : assignments)
                                !{Project p = projects.get(a.getProjectId());}
                                <tr>
                                    <td>
                                        @if(ctx.can(PermissionCode.PROJECT_READ))
                                            <a class="link" href="/projects/${p.id()}">${p.getName()}</a>
                                        @else
                                            ${p.getName()}
                                        @endif
                                        <p class="text-xs text-fg-placeholder">${p.getCode()}</p>
                                    </td>
                                    <td>${a.getRole() == null ? "-" : a.getRole().label()}</td>
                                    <td class="tabular-nums">${Ui.period(a.getPeriod().startDate(), a.getPeriod().endDate())}</td>
                                    <td>
                                        @if(a.isActiveOn(java.time.LocalDate.now()))
                                            <span class="${Seed.badge("informative")}">투입 중</span>
                                        @elseif(a.getPeriod().startDate().isAfter(java.time.LocalDate.now()))
                                            <span class="${Seed.badge("neutral", "outline")}">예정</span>
                                        @else
                                            <span class="${Seed.badge("neutral")}">종료</span>
                                        @endif
                                    </td>
                                </tr>
                            @endfor
                            </tbody>
                        </table>
                    </div>
                @endif
            </section>


            <div class="grid grid-cols-1 gap-x4 @4xl:grid-cols-2">
                <section class="card overflow-hidden">
                    <div class="card-header">
                        <h2 class="card-title">연봉 이력</h2>
                        @if(canWrite && !employee.isDeleted() && !employee.isResigned())
                            <button type="button" class="${Seed.button("ghost", "xsmall")}" hx-get="/employees/${employee.id()}/salary" hx-target="#modal-body">${Icons.of("plus", "size-4")} 연봉 변경</button>
                        @endif
                    </div>
                    @if(payrolls.isEmpty())
                        @template.components.empty(message = "등록된 연봉이 없습니다. 손익 집계에서 원가가 0원으로 계산됩니다.", icon = "money")
                    @else
                        <ul class="divide-y divide-stroke-neutral-subtle">
                            @for(Payroll p : payrolls)
                                <li class="flex items-center justify-between px-x4 py-x2_5 t4-regular">
                                    <span class="text-fg-neutral-subtle tabular-nums">${Ui.period(p.getPeriod().startDate(), p.getPeriod().endDate())}</span>
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
                        <ul class="divide-y divide-stroke-neutral-subtle">
                            @for(PositionHistory h : positions)
                                <li class="flex items-center justify-between px-x4 py-x2_5 t4-regular">
                                    <span class="text-fg-neutral-subtle tabular-nums">${Ui.period(h.getPeriod().startDate(), h.getPeriod().endDate())}</span>
                                    <span class="font-medium">${h.getPosition().label()} · ${h.getGrade().label()}</span>
                                </li>
                            @endfor
                        </ul>
                    @endif
                </section>
            </div>
        </div>

`)
