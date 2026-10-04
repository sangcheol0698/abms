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
@import kr.co.abacus.abms.employee.EmployeeInsight
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
@param EmployeeInsight insight
@param java.util.List<kr.co.abacus.abms.common.audit.AuditEntry> auditHistory = null
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
                <dt>연락처</dt>
                <dd class="tabular-nums">@if(employee.getPhone() != null)<a class="link" href="tel:${employee.getPhone()}">${employee.getPhone()}</a>@else<span class="text-fg-neutral-subtle">-</span>@endif</dd>
                <dt>직무</dt>
                <dd>${employee.getJob() == null ? "-" : employee.getJob().label()}</dd>
                <dt>근무 형태</dt>
                <dd>${employee.getWorkType() == null ? "-" : employee.getWorkType().label()}</dd>
                <dt>총 경력</dt>
                <dd>${EmployeeInsight.years(insight.careerMonths())}@if(employee.getCareerStartDate() != null)<span class="ml-x1 t3-regular text-fg-neutral-subtle">${Ui.date(employee.getCareerStartDate())}~</span>@endif</dd>
                <dt>근속</dt>
                <dd>${EmployeeInsight.years(insight.tenureMonths())}</dd>
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
                <form method="post" action="/employees/${employee.id()}/restore" hx-confirm="${employee.getName()} 직원을 복구할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
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
                            <form method="post" action="/employees/${employee.id()}/leave" hx-confirm="휴직 처리할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                                <button class="menu-item">휴직 처리</button>
                            </form>
                        @endif
                        @if(employee.getStatus() != EmployeeStatus.ACTIVE)
                            <form method="post" action="/employees/${employee.id()}/activate" hx-confirm="재직 상태로 변경할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                                <button class="menu-item">${employee.isResigned() ? "재입사 처리" : "복직 처리"}</button>
                            </form>
                        @endif
                        @if(employee.getStatus() != EmployeeStatus.RESIGNED)
                            <button type="button" class="menu-item" hx-get="/employees/${employee.id()}/resign" hx-target="#modal-body">퇴사 처리</button>
                        @endif
                        <div class="my-x1 border-t border-stroke-neutral-subtle"></div>
                        <form method="post" action="/employees/${employee.id()}/delete" hx-confirm="${employee.getName()} 직원을 삭제할까요? 삭제 후에도 복구할 수 있습니다.">${kr.co.abacus.abms.common.web.Csrf.field()}
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

        <div class="min-w-0 space-y-6">
            <div class="-mt-x3 flex flex-wrap items-center gap-x1_5">
                <span class="${Ui.badge(employee.getStatus())}">${employee.getStatus().label()}</span>
                <span class="${Ui.badge(employee.getType())}">${employee.getType().label()}</span>
                @if(employee.getJob() != null)<span class="${Seed.badge("neutral", "outline")}">${employee.getJob().label()}</span>@endif
                @if(employee.getWorkType() != null)<span class="${Seed.badge("neutral", "outline")}">${employee.getWorkType().label()}</span>@endif
                <span class="t3-regular text-fg-neutral-subtle">${employee.getPosition().label()} · ${employee.getGrade().label()} · ${tree.nameOf(employee.getDepartmentId())}</span>
            </div>

            <%-- 투입·경력 지표 --%>
            <div class="list-box grid grid-cols-2 gap-px bg-stroke-neutral-subtle @3xl:grid-cols-4 [&>div]:bg-bg-layer-default">
                <div class="px-x4 py-x3">
                    <p class="stat-label">투입 상태</p>
                    <p class="stat-value">${insight.activeAssignments()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">건 투입 중</span></p>
                    <p class="mt-x0_5 t3-regular text-fg-neutral-subtle">예정 ${insight.upcomingAssignments()}건</p>
                </div>
                <div class="px-x4 py-x3">
                    <p class="stat-label">이번 달 투입</p>
                    <p class="stat-value">${insight.currentMonthMm().toPlainString()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">M/M</span></p>
                    <p class="mt-x0_5">@if(insight.available())<span class="${Seed.badge("positive")}">투입 가능</span>@else<span class="${Seed.badge("neutral")}">전부 투입</span>@endif</p>
                </div>
                <div class="px-x4 py-x3">
                    <p class="stat-label">올해 누적 투입</p>
                    <p class="stat-value">${insight.yearMm().toPlainString()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">M/M</span></p>
                </div>
                <div class="px-x4 py-x3">
                    <p class="stat-label">총 경력</p>
                    <p class="stat-value t6-bold! @xl:t8-bold!">${EmployeeInsight.years(insight.careerMonths())}</p>
                    <p class="mt-x0_5 t3-regular text-fg-neutral-subtle">근속 ${EmployeeInsight.years(insight.tenureMonths())}</p>
                </div>
            </div>

            <%-- 올해 월별 투입 M/M --%>
            <section>
                <h2 class="section-title">${java.time.Year.now().getValue()}년 월별 투입 <span class="section-count">1.0 M/M = 한 달 전부 투입</span></h2>
                <div class="list-box p-x4">
                    <div class="flex h-28 items-end gap-x1_5">
                        @for(int i = 0; i < 12; i++)
                            !{int pct = insight.barPercent(i);}
                            <div class="group flex h-full flex-1 flex-col items-center justify-end gap-x1" title="${i + 1}월 ${insight.monthlyMm().get(i).toPlainString()} M/M">
                                <span class="t2-regular text-fg-neutral-subtle tabular-nums opacity-0 transition-opacity group-hover:opacity-100">${insight.monthlyMm().get(i).toPlainString()}</span>
                                <div class="w-full rounded-t-r1 ${pct > 0 ? "bg-bg-brand-solid" : "bg-bg-neutral-weak"} ${i + 1 == java.time.LocalDate.now().getMonthValue() ? "" : "opacity-80"}" style="height: ${Math.max(pct, 3)}%"></div>
                            </div>
                        @endfor
                    </div>
                    <div class="mt-x1_5 flex gap-x1_5">
                        @for(int i = 1; i <= 12; i++)
                            <span class="flex-1 text-center t2-regular tabular-nums ${i == java.time.LocalDate.now().getMonthValue() ? "t2-bold text-fg-neutral" : "text-fg-neutral-subtle"}">${i}월</span>
                        @endfor
                    </div>
                </div>
            </section>

            @if(!employee.skillList().isEmpty())
                <section>
                    <h2 class="section-title">보유 기술 <span class="section-count">${employee.skillList().size()}</span></h2>
                    <div class="flex flex-wrap gap-x1_5">
                        @for(String skill : employee.skillList())
                            <a href="/employees?q=${skill}" class="${Seed.badge("neutral", "outline")} hover:bg-bg-transparent-pressed" title="이 기술을 가진 직원 찾기">${skill}</a>
                        @endfor
                    </div>
                </section>
            @endif

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
        @if(auditHistory != null)
            <div class="mt-6">@template.components.auditHistory(entries = auditHistory, showTarget = true)</div>
        @endif
        </div>

`)
