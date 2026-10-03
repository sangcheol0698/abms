@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.summary.CalculationResult
@import kr.co.abacus.abms.summary.MonthReport
@import kr.co.abacus.abms.summary.ProfitRow
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param MonthReport report
@param boolean canManage
@param boolean isPast
@param CalculationResult calculation = null
!{String month = report.month().toString();}
@template.layout.app(ctx = ctx, title = "손익 현황", content = @`
    @template.components.pageHeader(title = "손익 현황", subtitle = "프로젝트 주관 부서 기준 월 손익 · 청구(발행) 기준 매출, 투입 M/M 기준 비용", actions = @`
        <div class="flex items-center gap-1">
            <a href="/summary?month=${report.month().minusMonths(1).toString()}" class="${Seed.button("neutralOutline")} !px-2.5" aria-label="이전 달">${Icons.of("chevron-left", "size-4")}</a>
            <form action="/summary" method="get">
                <input type="month" name="month" value="${month}" class="input !w-auto" onchange="this.form.requestSubmit()" aria-label="조회 월">
            </form>
            <a href="/summary?month=${report.month().plusMonths(1).toString()}" class="${Seed.button("neutralOutline")} !px-2.5" aria-label="다음 달">${Icons.of("chevron-right", "size-4")}</a>
        </div>
        @if(canManage)
            @if(!report.closed())
                <form method="post" action="/summary/recalculate" hx-disabled-elt="find button">
                    <input type="hidden" name="month" value="${month}">
                    <button class="${Seed.button("brandSolid")}">${Icons.of("refresh", "size-4")} 재집계</button>
                </form>
                @if(isPast)
                    <form method="post" action="/summary/close" hx-confirm="${month} 손익을 최신 값으로 재집계한 뒤 마감할까요? 마감된 월은 자동 재집계되지 않습니다.">
                        <input type="hidden" name="month" value="${month}">
                        <button class="${Seed.button("neutralOutline")}">${Icons.of("lock", "size-4")} 마감</button>
                    </form>
                @endif
            @else
                <form method="post" action="/summary/reopen" hx-confirm="${month} 마감을 해제할까요?">
                    <input type="hidden" name="month" value="${month}">
                    <button class="${Seed.button("neutralOutline")}">마감 해제</button>
                </form>
            @endif
        @endif
    `)

    <div class="mb-4 flex flex-wrap items-center gap-2 text-sm text-fg-neutral-muted">
        <span class="font-semibold text-fg-neutral">${Ui.month(report.month())}</span>
        @if(report.closed())
            <span class="${Seed.badge("neutral")}">${Icons.of("lock", "size-3")} 마감</span>
        @else
            <span class="${Seed.badge("informative")}">집계 중</span>
        @endif
        <span class="text-xs text-fg-neutral-subtle">마지막 집계 ${Ui.dateTime(report.calculatedAt())}</span>
    </div>

    @if(calculation != null && !calculation.warnings().isEmpty())
        <div class="mb-4 rounded-xl bg-bg-warning-weak p-4 text-sm ring-1 ring-stroke-warning-weak">
            <p class="flex items-center gap-2 font-semibold text-fg-warning-contrast">${Icons.of("warning", "size-4")} 확인이 필요한 항목 ${calculation.warnings().size()}건</p>
            <ul class="mt-2 list-disc space-y-0.5 pl-6 text-fg-warning-contrast">
                @for(String warning : calculation.warnings())
                    <li>${warning}</li>
                @endfor
            </ul>
        </div>
    @endif

    @if(!report.calculated())
        <div class="card">
            @template.components.empty(message = "이 달은 아직 집계되지 않았습니다." + (canManage ? " [재집계] 버튼으로 집계할 수 있습니다." : " 매일 새벽 자동 집계됩니다."), icon = "chart")
        </div>
    @else
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
            @template.components.stat(label = "매출", value = Ui.won(report.revenue()) + "원")
            @template.components.stat(label = "비용", value = Ui.won(report.cost()) + "원")
            @template.components.stat(label = "이익", value = Ui.won(report.profit()) + "원", valueClass = Ui.signClass(report.profit()))
            @template.components.stat(label = "이익률", value = Ui.percent(report.margin()), sub = "프로젝트 " + report.projects().size() + "건")
        </div>

        @if(report.companyCost() != null)
            <section class="card mt-4">
                <div class="card-header"><h2 class="card-title">전사 정직원 비용 배분</h2></div>
                <div class="grid grid-cols-1 divide-y divide-stroke-neutral-subtle sm:grid-cols-3 sm:divide-x sm:divide-y-0">
                    <div class="px-5 py-4"><p class="stat-label">정직원 총 원가</p><p class="mt-1 text-lg font-semibold tabular-nums">${Ui.won(report.companyCost().getTotalFullTimeCost())}원</p></div>
                    <div class="px-5 py-4"><p class="stat-label">프로젝트 배분</p><p class="mt-1 text-lg font-semibold tabular-nums">${Ui.won(report.companyCost().getAllocatedFullTimeCost())}원</p></div>
                    <div class="px-5 py-4"><p class="stat-label">미배분 (유휴)</p><p class="mt-1 text-lg font-semibold tabular-nums text-fg-warning">${Ui.won(report.companyCost().getUnallocatedFullTimeCost())}원</p></div>
                </div>
            </section>
        @endif

        <div class="mt-4 grid grid-cols-1 gap-4 2xl:grid-cols-5">
            <section class="card overflow-hidden 2xl:col-span-3">
                <div class="card-header"><h2 class="card-title">프로젝트별 손익</h2></div>
                <div class="overflow-x-auto">
                    <table class="table">
                        <thead><tr><th>프로젝트</th><th class="num">매출</th><th class="num">비용</th><th class="num">이익</th><th class="num">이익률</th></tr></thead>
                        <tbody class="divide-y divide-stroke-neutral-subtle">
                        @for(ProfitRow row : report.projects())
                            <tr>
                                <td><a href="/projects/${row.id()}" class="link">${row.name()}</a><p class="text-xs text-fg-placeholder">${row.code()}</p></td>
                                <td class="num">${Ui.won(row.revenue())}</td>
                                <td class="num">${Ui.won(row.cost())}</td>
                                <td class="num font-semibold ${Ui.signClass(row.profit())}">${Ui.won(row.profit())}</td>
                                <td class="num">${row.revenue().amount().signum() == 0 ? "-" : Ui.percent(row.margin())}</td>
                            </tr>
                        @endfor
                        </tbody>
                    </table>
                </div>
            </section>
            <section class="card overflow-hidden 2xl:col-span-2">
                <div class="card-header"><h2 class="card-title">부서별 손익 (주관 부서)</h2></div>
                <div class="overflow-x-auto">
                    <table class="table">
                        <thead><tr><th>부서</th><th class="num">매출</th><th class="num">비용</th><th class="num">이익</th></tr></thead>
                        <tbody class="divide-y divide-stroke-neutral-subtle">
                        @for(ProfitRow row : report.departments())
                            <tr>
                                <td><a href="/departments?selected=${row.id()}" class="link">${row.name()}</a></td>
                                <td class="num">${Ui.won(row.revenue())}</td>
                                <td class="num">${Ui.won(row.cost())}</td>
                                <td class="num font-semibold ${Ui.signClass(row.profit())}">${Ui.won(row.profit())}</td>
                            </tr>
                        @endfor
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    @endif
`)
