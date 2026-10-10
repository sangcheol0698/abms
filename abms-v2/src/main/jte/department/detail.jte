@import java.util.List
@import kr.co.abacus.abms.common.domain.Money
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.web.ViewContext
@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.web.department.DepartmentInsight
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.web.employee.EmployeeInsight
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.summary.ProfitQueryService.MonthPoint
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.web.Badges
@param ViewContext ctx
@param Department department
@param DepartmentTree tree
@param List<Employee> members
@param int subtreeMemberCount
@param Employee leader = null
@param List<Project> projects
@param List<MonthPoint> trend = null
@param boolean canWrite
@param DepartmentInsight insight
@param String trendChart = null
@param java.util.List<kr.co.abacus.abms.common.audit.AuditEntry> auditHistory = null
<div>
    <div class="min-w-0 space-y-6">
        <div class="flex flex-wrap items-start justify-between gap-x3">
            <div class="min-w-0">
                <p class="truncate t3-regular text-fg-neutral-subtle">
                    @for(Department d : tree.path(department.id()))
                        @if(!d.id().equals(department.id()))${d.getName()} › @endif
                    @endfor
                </p>
                <h2 class="mt-x1 flex items-center gap-x2 t7-bold text-fg-neutral">
                    ${department.getName()} <span class="${Seed.badge("neutral")}">${department.getType().label()}</span>
                </h2>
                @if(department.getDescription() != null)
                    <p class="mt-x1_5 max-w-2xl t4-regular text-fg-neutral-muted">${department.getDescription()}</p>
                @endif
            </div>
            @if(canWrite)
                <div class="flex flex-wrap items-center gap-x1_5">
                    <button type="button" class="${Seed.button("neutralOutline", "xsmall")}" hx-get="/departments/new?parentId=${department.id()}" hx-target="#modal-body">${Icons.of("plus", "size-4")} 하위 부서</button>
                    <button type="button" class="${Seed.button("neutralOutline", "xsmall")}" hx-get="/departments/${department.id()}/edit" hx-target="#modal-body">${Icons.of("pencil", "size-4")} 수정</button>
                    <button type="button" class="${Seed.button("neutralOutline", "xsmall")}" hx-get="/departments/${department.id()}/leader" hx-target="#modal-body">${Icons.of("user", "size-4")} 부서장</button>
                    <form method="post" action="/departments/${department.id()}/delete" hx-confirm="${department.getName()} 부서를 삭제할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                        <button class="${Seed.iconButton("ghost", "xsmall")} text-fg-critical" aria-label="부서 삭제" title="부서 삭제">${Icons.of("trash", "size-4")}</button>
                    </form>
                </div>
            @else
                @template.components.readOnly(reason = "부서를 수정할 권한이 없습니다.")
            @endif
        </div>

        <%-- 운영 지표 (하위 부서 포함) --%>
        <div class="list-box grid grid-cols-2 gap-px bg-stroke-neutral-subtle @3xl:grid-cols-4 [&>div]:bg-bg-layer-default">
            <div class="px-x4 py-x3">
                <p class="stat-label">인원</p>
                <p class="stat-value">${insight.headcount()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">명</span></p>
                <p class="mt-x0_5 t3-regular text-fg-neutral-subtle">직속 ${members.size()}명 · 재직 ${insight.activeCount()}명</p>
            </div>
            <div class="px-x4 py-x3">
                <p class="stat-label">가동률</p>
                <p class="stat-value">${insight.utilizationPercent()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">%</span></p>
                <p class="mt-x0_5 t3-regular text-fg-neutral-subtle">투입 중 ${insight.assignedCount()}명 / 재직 ${insight.activeCount()}명</p>
            </div>
            <div class="px-x4 py-x3">
                <p class="stat-label">평균 근속</p>
                <p class="stat-value t6-bold! @xl:t8-bold!">${EmployeeInsight.years(insight.averageTenureMonths())}</p>
            </div>
            <div class="px-x4 py-x3">
                <p class="stat-label">진행 중 프로젝트</p>
                <p class="stat-value">${insight.inProgressProjects()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">건</span></p>
                <p class="mt-x0_5 t3-regular text-fg-neutral-subtle">주관 ${projects.size()}건</p>
            </div>
        </div>

        @if(trend != null)
            !{Money revenue = trend.stream().map(MonthPoint::revenue).reduce(Money.ZERO, Money::plus);}
            !{Money cost = trend.stream().map(MonthPoint::cost).reduce(Money.ZERO, Money::plus);}
            <section>
                <h3 class="section-title">${java.time.Year.now().getValue()}년 손익 <span class="section-count">매출 ${revenue.compact()} · 비용 ${cost.compact()} · 이익 <span class="${Ui.signClass(revenue.minus(cost))}">${revenue.minus(cost).compact()}</span></span></h3>
                <div class="list-box p-x4">
                    <div class="h-56"><canvas data-chart="${trendChart}" aria-label="월별 손익 차트"></canvas></div>
                </div>
            </section>
        @endif

        @if(insight.headcount() > 0)
            <section>
                <h3 class="section-title">인원 구성 <span class="section-count">하위 부서 포함</span></h3>
                <div class="grid grid-cols-1 gap-x4 @3xl:grid-cols-3">
                    @for(var group : java.util.List.of(
                            java.util.Map.entry("직급", insight.positions().entrySet().stream().map(e -> java.util.Map.entry(e.getKey().label(), e.getValue())).toList()),
                            java.util.Map.entry("고용유형", insight.types().entrySet().stream().map(e -> java.util.Map.entry(e.getKey().label(), e.getValue())).toList()),
                            java.util.Map.entry("직무", insight.jobs().entrySet().stream().map(e -> java.util.Map.entry(e.getKey().label(), e.getValue())).toList())))
                        <div class="list-box p-x4">
                            <p class="mb-x3 t3-medium text-fg-neutral-subtle">${group.getKey()}</p>
                            @if(group.getValue().isEmpty())
                                <p class="t3-regular text-fg-neutral-subtle">미지정</p>
                            @endif
                            <div class="space-y-x2">
                                @for(var row : group.getValue())
                                    <div class="flex items-center gap-x2">
                                        <span class="w-20 shrink-0 truncate t3-regular text-fg-neutral-muted">${row.getKey()}</span>
                                        <div class="meter flex-1"><span class="h-full bg-bg-brand-solid" style="width: ${insight.percentOf(row.getValue())}%"></span></div>
                                        <span class="w-8 shrink-0 text-right t3-medium tabular-nums">${row.getValue()}</span>
                                    </div>
                                @endfor
                            </div>
                        </div>
                    @endfor
                </div>
            </section>
        @endif

        @if(!insight.children().isEmpty())
            <section>
                <h3 class="section-title">하위 부서 <span class="section-count">${insight.children().size()}</span></h3>
                <div class="list-box">
                    @for(DepartmentInsight.ChildDepartment child : insight.children())
                        <a href="/departments?selected=${child.department().id()}" class="list-row">
                            ${Icons.of("building", "size-4 shrink-0 text-fg-neutral-subtle")}
                            <span class="min-w-0 flex-1 truncate t4-medium">${child.department().getName()}</span>
                            <span class="${Seed.badge("neutral")}">${child.department().getType().label()}</span>
                            <span class="w-16 shrink-0 text-right t3-regular text-fg-neutral-subtle tabular-nums">${child.headcount()}명</span>
                        </a>
                    @endfor
                </div>
            </section>
        @endif

        <section>
            <h3 class="section-title">구성원 <span class="section-count">${members.size()}</span></h3>
            @if(members.isEmpty())
                <div class="list-box">@template.components.empty(message = "소속 직원이 없습니다.", icon = "users")</div>
            @else
                <div class="list-box">
                    @for(Employee e : members)
                        <a href="/employees/${e.id()}" class="list-row">
                            @template.components.avatar(photoUrl = e.photoUrl(), name = e.getName(), size = "size-6")
                            <span class="min-w-0 flex-1 truncate t4-medium">${e.getName()}</span>
                            <span class="list-cell-muted hidden w-28 @xl:block">${e.getPosition().label()} · ${e.getGrade().label()}</span>
                            <span class="w-14 shrink-0"><span class="${Badges.of(e.getStatus())}">${e.getStatus().label()}</span></span>
                        </a>
                    @endfor
                </div>
            @endif
        </section>

        @if(!projects.isEmpty())
            <section>
                <h3 class="section-title">주관 프로젝트 <span class="section-count">${projects.size()} · 하위 부서 포함</span></h3>
                <div class="list-box">
                    @for(Project p : projects)
                        @template.components.projectRow(project = p, tree = tree)
                    @endfor
                </div>
            </section>
        @endif
        @if(auditHistory != null)
            <div class="mt-6">@template.components.auditHistory(entries = auditHistory, showTarget = true)</div>
        @endif
    </div>

</div>
