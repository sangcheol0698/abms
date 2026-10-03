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
            <form action="/" method="get" class="flex items-center gap-2">
                <select name="year" class="select !w-auto" onchange="this.form.requestSubmit()" aria-label="연도">
                    @for(Integer y : years)
                        <option value="${y}" selected="${y == dashboard.year()}">${y}년</option>
                    @endfor
                </select>
            </form>
            @if(ctx.can(kr.co.abacus.abms.access.PermissionCode.DASHBOARD_READ))
                <a href="/summary" class="${Seed.button("neutralWeak")}">${Icons.of("chart", "size-4")} 월별 손익</a>
            @endif
        `)

    <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        @template.components.stat(label = "누적 매출", value = dashboard.revenue().compact() + "원", sub = Ui.won(dashboard.revenue()) + "원")
        @template.components.stat(label = "누적 비용", value = dashboard.cost().compact() + "원", sub = Ui.won(dashboard.cost()) + "원")
        @template.components.stat(label = "누적 이익", value = dashboard.profit().compact() + "원",
            sub = "이익률 " + Ui.percent(dashboard.margin()), valueClass = Ui.signClass(dashboard.profit()))
        @template.components.stat(label = "진행 중 프로젝트", value = dashboard.inProgressCount() + "건",
            sub = "계약 총액 " + dashboard.inProgressContractAmount().compact() + "원 · 재직 " + dashboard.activeEmployees() + "명")
    </div>

    <div class="mt-4 grid grid-cols-1 gap-4 xl:grid-cols-3">
        <section class="card xl:col-span-2">
            <div class="card-header"><h2 class="card-title">월별 매출 · 비용 · 이익</h2></div>
            <div class="card-body">
                <div class="h-72"><canvas data-chart="${dashboard.chartJson()}" aria-label="월별 손익 차트"></canvas></div>
            </div>
        </section>
        <section class="card">
            <div class="card-header"><h2 class="card-title">프로젝트 상태</h2></div>
            <div class="card-body space-y-3">
                @for(ProjectStatus status : ProjectStatus.values())
                    !{long count = dashboard.projectStatusCounts().getOrDefault(status, 0L);}
                    <a href="/projects?status=${status.name()}" class="flex items-center justify-between rounded-lg px-2 py-1.5 hover:bg-bg-neutral-muted">
                        <span class="${Ui.badge(status)}">${status.label()}</span>
                        <span class="text-sm font-semibold text-fg-neutral tabular-nums">${count}</span>
                    </a>
                @endfor
                @if(dashboard.unallocatedCost() != null)
                    <div class="mt-4 rounded-lg bg-bg-warning-weak p-3 ring-1 ring-stroke-warning-weak">
                        <p class="text-xs font-medium text-fg-warning-contrast">미배분 정직원 비용 (${dashboard.year()}년 누적)</p>
                        <p class="mt-1 text-lg font-semibold text-fg-warning-contrast tabular-nums">${Ui.won(dashboard.unallocatedCost())}원</p>
                        <p class="mt-0.5 text-xs text-fg-warning">프로젝트에 투입되지 않은 정직원 원가입니다.</p>
                    </div>
                @endif
            </div>
        </section>
    </div>

    <div class="mt-4 grid grid-cols-1 gap-4 xl:grid-cols-2">
        <section class="card overflow-hidden">
            <div class="card-header"><h2 class="card-title">이익 상위 프로젝트</h2></div>
            @if(dashboard.topProjects().isEmpty())
                @template.components.empty(message = "집계된 손익이 없습니다.", icon = "chart")
            @else
                <div class="overflow-x-auto">
                    <table class="table">
                        <thead><tr><th>프로젝트</th><th class="num">매출</th><th class="num">비용</th><th class="num">이익</th></tr></thead>
                        <tbody class="divide-y divide-stroke-neutral-subtle">
                        @for(ProfitRow row : dashboard.topProjects())
                            <tr>
                                <td><a class="link" href="/projects/${row.id()}">${row.name()}</a><p class="text-xs text-fg-placeholder">${row.code()}</p></td>
                                <td class="num">${row.revenue().compact()}</td>
                                <td class="num">${row.cost().compact()}</td>
                                <td class="num font-semibold ${Ui.signClass(row.profit())}">${row.profit().compact()}</td>
                            </tr>
                        @endfor
                        </tbody>
                    </table>
                </div>
            @endif
        </section>
        <section class="card overflow-hidden">
            <div class="card-header"><h2 class="card-title">부서별 손익 (주관 부서 기준)</h2></div>
            @if(dashboard.departments().isEmpty())
                @template.components.empty(message = "집계된 손익이 없습니다.", icon = "building")
            @else
                <div class="overflow-x-auto">
                    <table class="table">
                        <thead><tr><th>부서</th><th class="num">매출</th><th class="num">비용</th><th class="num">이익</th><th class="num">이익률</th></tr></thead>
                        <tbody class="divide-y divide-stroke-neutral-subtle">
                        @for(ProfitRow row : dashboard.departments())
                            <tr>
                                <td><a class="link" href="/departments?selected=${row.id()}">${row.name()}</a></td>
                                <td class="num">${row.revenue().compact()}</td>
                                <td class="num">${row.cost().compact()}</td>
                                <td class="num font-semibold ${Ui.signClass(row.profit())}">${row.profit().compact()}</td>
                                <td class="num">${Ui.percent(row.margin())}</td>
                            </tr>
                        @endfor
                        </tbody>
                    </table>
                </div>
            @endif
        </section>
    </div>

    <section class="card mt-4 overflow-hidden">
        <div class="card-header">
            <h2 class="card-title">청구 예정 · 미발행 매출</h2>
            <span class="text-xs text-fg-neutral-subtle">지난 30일 ~ 향후 60일</span>
        </div>
        @if(dashboard.upcomingRevenues().isEmpty())
            @template.components.empty(message = "예정된 청구가 없습니다.", icon = "money")
        @else
            <ul class="divide-y divide-stroke-neutral-subtle">
                @for(UpcomingRevenue item : dashboard.upcomingRevenues())
                    <li class="flex flex-wrap items-center justify-between gap-3 px-5 py-3">
                        <div class="min-w-0">
                            <a href="/projects/${item.project().id()}" class="link">${item.project().getName()}</a>
                            <p class="text-xs text-fg-neutral-subtle">${item.plan().getSequence()}차 ${item.plan().getType().label()} · 청구일 ${Ui.date(item.plan().getRevenueDate())}</p>
                        </div>
                        <div class="flex items-center gap-3">
                            @if(item.overdue())
                                <span class="${Seed.badge("critical")}">기한 경과</span>
                            @else
                                <span class="${Seed.badge("neutral")}">예정</span>
                            @endif
                            <span class="text-sm font-semibold tabular-nums">${Ui.won(item.plan().getAmount())}원</span>
                        </div>
                    </li>
                @endfor
            </ul>
        @endif
    </section>
`)
