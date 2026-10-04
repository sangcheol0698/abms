@import java.util.List
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.dashboard.DashboardService.Dashboard
@import kr.co.abacus.abms.dashboard.DashboardService.UpcomingRevenue
@import kr.co.abacus.abms.project.ProjectStatus
@import kr.co.abacus.abms.summary.ProfitRow
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param Dashboard dashboard
@param List<Integer> years
@template.layout.app(ctx = ctx, title = "대시보드", content = @`
    @template.components.pageHeader(
        title = "대시보드",
        subtitle = dashboard.year() + "년 손익 현황 · " + (dashboard.companyWide() ? "전사 기준" : "내 권한 범위 기준"),
        actions = @`
            <form action="/" method="get" class="flex items-center gap-x2">
                <select name="year" class="select filter-select" data-submit-on-change aria-label="연도">
                    @for(Integer y : years)
                        <option value="${y}" selected="${y == dashboard.year()}">${y}년</option>
                    @endfor
                </select>
            </form>
            @if(ctx.can(kr.co.abacus.abms.access.PermissionCode.DASHBOARD_READ))
                <a href="/summary" class="${Seed.button("neutralOutline")}">${Icons.of("chart", "size-4")} 월별 손익</a>
            @endif
        `)

    <div class="space-y-6">
        <%-- 지표 줄 --%>
        <div class="list-box grid grid-cols-2 gap-px bg-stroke-neutral-subtle @3xl:grid-cols-4 [&>div]:bg-bg-layer-default">
            <div class="px-x4 py-x3"><p class="stat-label">누적 매출</p><p class="stat-value">${dashboard.revenue().compact()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">원</span></p><p class="mt-x0_5 t3-regular text-fg-neutral-subtle tabular-nums">${Ui.won(dashboard.revenue())}원</p></div>
            <div class="px-x4 py-x3"><p class="stat-label">누적 비용</p><p class="stat-value">${dashboard.cost().compact()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">원</span></p><p class="mt-x0_5 t3-regular text-fg-neutral-subtle tabular-nums">${Ui.won(dashboard.cost())}원</p></div>
            <div class="px-x4 py-x3"><p class="stat-label">누적 이익</p><p class="stat-value ${Ui.signClass(dashboard.profit())}">${dashboard.profit().compact()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">원</span></p><p class="mt-x0_5 t3-regular text-fg-neutral-subtle">이익률 ${Ui.percent(dashboard.margin())}</p></div>
            <div class="px-x4 py-x3"><p class="stat-label">진행 중 프로젝트</p><p class="stat-value">${dashboard.inProgressCount()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">건</span></p><p class="mt-x0_5 truncate t3-regular text-fg-neutral-subtle">계약 ${dashboard.inProgressContractAmount().compact()}원 · 재직 ${dashboard.activeEmployees()}명</p></div>
        </div>

        <div class="grid grid-cols-1 gap-6 @5xl:grid-cols-[minmax(0,1fr)_320px]">
            <section class="min-w-0">
                <h2 class="section-title">월별 매출 · 비용 · 이익 <span class="section-count">${dashboard.year()}년</span></h2>
                <div class="list-box p-x4">
                    <div class="h-72"><canvas data-chart="${dashboard.chartJson()}" aria-label="월별 손익 차트"></canvas></div>
                </div>
            </section>
            <section>
                <h2 class="section-title">프로젝트 상태</h2>
                <div class="list-box">
                    @for(ProjectStatus status : ProjectStatus.values())
                        !{long count = dashboard.projectStatusCounts().getOrDefault(status, 0L);}
                        <a href="/projects?status=${status.name()}" class="list-row">
                            @template.components.statusIcon(status = status)
                            <span class="flex-1 t4-medium">${status.label()}</span>
                            <span class="tabular-nums ${count == 0 ? "text-fg-neutral-subtle" : "t4-bold"}">${count}</span>
                            ${Icons.of("chevron-right", "size-4 text-fg-neutral-subtle")}
                        </a>
                    @endfor
                </div>
                @if(dashboard.unallocatedCost() != null)
                    <a href="/summary" class="mt-x3 block rounded-r3 border border-stroke-warning-weak bg-bg-warning-weak px-x4 py-x3 transition-colors hover:border-stroke-warning-solid">
                        <p class="t3-medium text-fg-warning-contrast">미배분 정직원 비용 · ${dashboard.year()}년 누적</p>
                        <p class="mt-x1 t6-bold tabular-nums text-fg-warning-contrast">${Ui.won(dashboard.unallocatedCost())}원</p>
                        <p class="mt-x0_5 t3-regular text-fg-warning">프로젝트에 투입되지 않은 정직원 원가입니다.</p>
                    </a>
                @endif
            </section>
        </div>

        <div class="grid grid-cols-1 gap-6 @4xl:grid-cols-2">
            <section class="min-w-0">
                <h2 class="section-title">이익 상위 프로젝트</h2>
                <div class="list-box">
                    @if(dashboard.topProjects().isEmpty())
                        @template.components.empty(message = "집계된 손익이 없습니다.", icon = "chart")
                    @else
                        <div class="list-head"><span class="flex-1">프로젝트</span><span class="w-20 shrink-0 text-right">매출</span><span class="w-20 shrink-0 text-right">비용</span><span class="w-20 shrink-0 text-right">이익</span></div>
                        @for(ProfitRow row : dashboard.topProjects())
                            <a href="/projects/${row.id()}" class="list-row">
                                <span class="flex min-w-0 flex-1 items-baseline gap-x2"><span class="truncate t4-medium">${row.name()}</span><span class="hidden t3-regular whitespace-nowrap text-fg-neutral-subtle @xl:inline">${row.code()}</span></span>
                                <span class="w-20 shrink-0 text-right tabular-nums">${row.revenue().compact()}</span>
                                <span class="w-20 shrink-0 text-right tabular-nums text-fg-neutral-muted">${row.cost().compact()}</span>
                                <span class="w-20 shrink-0 text-right t4-bold tabular-nums ${Ui.signClass(row.profit())}">${row.profit().compact()}</span>
                            </a>
                        @endfor
                    @endif
                </div>
            </section>
            <section class="min-w-0">
                <h2 class="section-title">부서별 손익 <span class="section-count">주관 부서 기준</span></h2>
                <div class="list-box">
                    @if(dashboard.departments().isEmpty())
                        @template.components.empty(message = "집계된 손익이 없습니다.", icon = "building")
                    @else
                        <div class="list-head"><span class="flex-1">부서</span><span class="w-20 shrink-0 text-right">매출</span><span class="w-20 shrink-0 text-right">이익</span><span class="w-16 shrink-0 text-right">이익률</span></div>
                        @for(ProfitRow row : dashboard.departments())
                            <a href="/departments?selected=${row.id()}" class="list-row">
                                <span class="min-w-0 flex-1 truncate t4-medium">${row.name()}</span>
                                <span class="w-20 shrink-0 text-right tabular-nums">${row.revenue().compact()}</span>
                                <span class="w-20 shrink-0 text-right t4-bold tabular-nums ${Ui.signClass(row.profit())}">${row.profit().compact()}</span>
                                <span class="w-16 shrink-0 text-right tabular-nums text-fg-neutral-muted">${Ui.percent(row.margin())}</span>
                            </a>
                        @endfor
                    @endif
                </div>
            </section>
        </div>

        <section>
            <h2 class="section-title">청구 예정 · 미발행 매출 <span class="section-count">지난 30일 ~ 향후 60일</span></h2>
            <div class="list-box">
                @if(dashboard.upcomingRevenues().isEmpty())
                    @template.components.empty(message = "예정된 청구가 없습니다.", icon = "money")
                @else
                    @for(UpcomingRevenue item : dashboard.upcomingRevenues())
                        <a href="/projects/${item.project().id()}" class="list-row">
                            <span class="w-24 shrink-0 t3-regular tabular-nums ${item.overdue() ? "text-fg-critical" : "text-fg-neutral-subtle"}">${Ui.date(item.plan().getRevenueDate())}</span>
                            <span class="min-w-0 flex-1 truncate t4-medium">${item.project().getName()}</span>
                            <span class="list-cell-muted hidden w-28 @xl:block">${item.plan().getSequence()}차 ${item.plan().getType().label()}</span>
                            <span class="w-20 shrink-0">
                                @if(item.overdue())
                                    <span class="${Seed.badge("critical")}">기한 경과</span>
                                @else
                                    <span class="${Seed.badge("neutral", "outline")}">예정</span>
                                @endif
                            </span>
                            <span class="w-32 shrink-0 text-right t4-bold tabular-nums">${Ui.won(item.plan().getAmount())}원</span>
                        </a>
                    @endfor
                @endif
            </div>
        </section>
    </div>
`)
