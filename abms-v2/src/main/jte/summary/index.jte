@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.summary.CalculationResult
@import kr.co.abacus.abms.summary.MonthReport
@import kr.co.abacus.abms.summary.ProfitRow
@import kr.co.abacus.abms.summary.RevenueBasis
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param MonthReport report
@param boolean canManage
@param boolean isPast
@param CalculationResult calculation = null
!{String month = report.month().toString();}
!{boolean managed = report.basis() == RevenueBasis.MANAGED;}
!{String basisQuery = managed ? "&basis=managed" : "";}
@template.layout.app(ctx = ctx, title = "손익 현황", content = @`
    @template.components.pageHeader(title = "손익 현황", subtitle = "프로젝트 주관 부서 기준 월 손익 · " + (managed ? "진행 기준 매출(계약금액 기간 일할)" : "청구(발행) 기준 매출") + ", 비용 = 인건비(투입 M/M) + 직접비", actions = @`
        @if(canManage)
            @if(!report.closed())
                <form method="post" action="/summary/recalculate" hx-disabled-elt="find button">${kr.co.abacus.abms.common.web.Csrf.field()}
                    <input type="hidden" name="month" value="${month}">
                    <button class="${Seed.button("brandSolid")}">${Icons.of("refresh", "size-4")} 재집계</button>
                </form>
                @if(isPast)
                    <form method="post" action="/summary/close" hx-confirm="${month} 손익을 최신 값으로 재집계한 뒤 마감할까요? 마감된 월은 자동 재집계되지 않습니다.">${kr.co.abacus.abms.common.web.Csrf.field()}
                        <input type="hidden" name="month" value="${month}">
                        <button class="${Seed.button("neutralOutline")}">${Icons.of("lock", "size-4")} 마감</button>
                    </form>
                @endif
            @else
                <form method="post" action="/summary/reopen" hx-confirm="${month} 마감을 해제할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                    <input type="hidden" name="month" value="${month}">
                    <button class="${Seed.button("neutralOutline")}">마감 해제</button>
                </form>
            @endif
        @endif
    `)

    <%-- 월 이동 · 상태 --%>
    <div class="-mx-4 mb-x6 sm:-mx-6 lg:-mx-8">
        <div class="view-bar border-t sm:px-x6 lg:px-x8">
            <div class="flex items-center rounded-r2 border border-stroke-neutral-subtle">
                <a href="/summary?month=${report.month().minusMonths(1).toString()}${basisQuery}" class="${Seed.iconButton("ghost", "xsmall")} rounded-r-none" aria-label="이전 달" title="이전 달">${Icons.of("chevron-left", "size-4")}</a>
                <form action="/summary" method="get" class="border-x border-stroke-neutral-subtle">
                    <input type="month" name="month" value="${month}" required class="h-7 bg-transparent px-x2 t4-medium text-fg-neutral outline-none" data-submit-on-change aria-label="조회 월">
                    @if(managed)<input type="hidden" name="basis" value="managed">@endif
                </form>
                <a href="/summary?month=${report.month().plusMonths(1).toString()}${basisQuery}" class="${Seed.iconButton("ghost", "xsmall")} rounded-l-none" aria-label="다음 달" title="다음 달">${Icons.of("chevron-right", "size-4")}</a>
            </div>
            @if(report.closed())
                <span class="${Seed.badge("neutral")}">${Icons.of("lock", "size-3")} 마감</span>
            @else
                <span class="${Seed.badge("informative")}">집계 중</span>
            @endif
            <div class="flex items-center rounded-r2 border border-stroke-neutral-subtle" role="group" aria-label="매출 인식 기준">
                @for(RevenueBasis b : RevenueBasis.values())
                    !{boolean active = b == report.basis();}
                    <a href="/summary?month=${month}${b == RevenueBasis.MANAGED ? "&basis=managed" : ""}" class="${Seed.button(active ? "neutralWeak" : "ghost", "xsmall")}" aria-current="${active ? "true" : null}"
                       title="${b == RevenueBasis.MANAGED ? "계약금액을 프로젝트 기간에 일할 배분한 관리 매출" : "세금계산서가 발행된 청구 계획 기준 매출"}">${b.label()}</a>
                @endfor
            </div>
            <span class="ml-auto t3-regular text-fg-neutral-subtle">마지막 집계 ${Ui.dateTime(report.calculatedAt())}</span>
        </div>
    </div>

    @if(calculation != null && !calculation.warnings().isEmpty())
        <div role="alert" class="seed-callout__root seed-callout__root--tone_warning mb-x6 items-start!">
            ${Icons.of("warning", "size-4 shrink-0 text-fg-warning")}
            <div class="seed-callout__content">
                <p class="seed-callout__title seed-callout__title--tone_warning">확인이 필요한 항목 ${calculation.warnings().size()}건</p>
                <ul class="seed-callout__description seed-callout__description--tone_warning mt-x1 list-disc pl-x5">
                    @for(String warning : calculation.warnings())
                        <li>${warning}</li>
                    @endfor
                </ul>
            </div>
        </div>
    @endif

    @if(!report.calculated())
        <div class="list-box">
            @template.components.empty(message = "이 달은 아직 집계되지 않았습니다." + (canManage ? " [재집계] 버튼으로 집계할 수 있습니다." : " 매일 새벽 자동 집계됩니다."), icon = "chart")
        </div>
    @else
        <div class="space-y-6">
            <%-- 지표 줄 --%>
            <div class="list-box grid grid-cols-2 gap-px bg-stroke-neutral-subtle @3xl:grid-cols-4 [&>div]:bg-bg-layer-default">
                <div class="px-x4 py-x3"><p class="stat-label">매출 <span class="text-fg-placeholder">${report.basis().label()}</span></p><p class="stat-value">${Ui.won(report.revenue())}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">원</span></p></div>
                <div class="px-x4 py-x3"><p class="stat-label">비용</p><p class="stat-value">${Ui.won(report.cost())}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">원</span></p>
                    <p class="mt-x0_5 t3-regular text-fg-neutral-subtle tabular-nums">인건비 ${Ui.won(report.laborCost())} · 직접비 ${Ui.won(report.directCost())}</p></div>
                <div class="px-x4 py-x3"><p class="stat-label">이익</p><p class="stat-value ${Ui.signClass(report.profit())}">${Ui.won(report.profit())}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">원</span></p></div>
                <div class="px-x4 py-x3"><p class="stat-label">이익률</p><p class="stat-value">${Ui.percent(report.margin())}</p><p class="mt-x0_5 t3-regular text-fg-neutral-subtle">프로젝트 ${report.projects().size()}건</p></div>
            </div>

            @if(report.companyCost() != null)
                !{var company = report.companyCost();}
                !{int allocatedPct = company.getTotalFullTimeCost().amount().signum() <= 0 ? 0 : Math.max(0, Math.min(100, company.getAllocatedFullTimeCost().amount().multiply(java.math.BigDecimal.valueOf(100)).divide(company.getTotalFullTimeCost().amount(), 0, java.math.RoundingMode.HALF_UP).intValue()));}
                <section>
                    <h2 class="section-title">전사 정직원 비용 배분 <span class="section-count">배분 ${allocatedPct}%</span></h2>
                    <div class="list-box p-x4">
                        <div class="meter" role="img" aria-label="프로젝트 배분 ${allocatedPct}%">
                            <span class="h-full bg-bg-brand-solid" style="width: ${allocatedPct}%"></span>
                        </div>
                        <dl class="mt-x3 grid grid-cols-1 gap-x3 @xl:grid-cols-3">
                            <div><dt class="stat-label">정직원 총 원가</dt><dd class="mt-x0_5 t5-bold tabular-nums">${Ui.won(company.getTotalFullTimeCost())}원</dd></div>
                            <div><dt class="stat-label flex items-center gap-x1_5"><span class="size-2 rounded-full bg-bg-brand-solid"></span>프로젝트 배분</dt><dd class="mt-x0_5 t5-bold tabular-nums">${Ui.won(company.getAllocatedFullTimeCost())}원</dd></div>
                            <div><dt class="stat-label flex items-center gap-x1_5"><span class="size-2 rounded-full bg-bg-neutral-weak ring-1 ring-stroke-neutral-weak"></span>미배분 (유휴)</dt><dd class="mt-x0_5 t5-bold tabular-nums text-fg-warning">${Ui.won(company.getUnallocatedFullTimeCost())}원</dd></div>
                        </dl>
                    </div>
                </section>
            @endif

            <div class="grid grid-cols-1 gap-6 @7xl:grid-cols-5">
                <section class="min-w-0 @7xl:col-span-3">
                    <h2 class="section-title">프로젝트별 손익 <span class="section-count">${report.projects().size()}</span></h2>
                    <div class="list-box overflow-x-auto">
                        <div class="min-w-[560px]">
                            <div class="list-head"><span class="flex-1">프로젝트</span><span class="col-num">매출</span><span class="col-num">비용</span><span class="col-num">이익</span><span class="w-16 shrink-0 text-right">이익률</span></div>
                            @for(ProfitRow row : report.projects())
                                <a href="/projects/${row.id()}" class="list-row">
                                    <span class="flex min-w-0 flex-1 items-baseline gap-x2"><span class="truncate t4-medium">${row.name()}</span><span class="hidden t3-regular whitespace-nowrap text-fg-neutral-subtle @xl:inline">${row.code()}</span></span>
                                    <span class="col-num">${Ui.won(row.revenue())}</span>
                                    <span class="col-num text-fg-neutral-muted">${Ui.won(row.cost())}</span>
                                    <span class="col-num t4-bold ${Ui.signClass(row.profit())}">${Ui.won(row.profit())}</span>
                                    <span class="w-16 shrink-0 text-right tabular-nums text-fg-neutral-muted">${row.revenue().amount().signum() == 0 ? "-" : Ui.percent(row.margin())}</span>
                                </a>
                            @endfor
                        </div>
                    </div>
                </section>
                <section class="min-w-0 @7xl:col-span-2">
                    <h2 class="section-title">부서별 손익 <span class="section-count">주관 부서</span></h2>
                    <div class="list-box overflow-x-auto">
                        <div class="min-w-[440px]">
                            <div class="list-head"><span class="flex-1">부서</span><span class="col-num">매출</span><span class="col-num">비용</span><span class="col-num">이익</span></div>
                            @for(ProfitRow row : report.departments())
                                <a href="/departments?selected=${row.id()}" class="list-row">
                                    <span class="min-w-0 flex-1 truncate t4-medium">${row.name()}</span>
                                    <span class="col-num">${Ui.won(row.revenue())}</span>
                                    <span class="col-num text-fg-neutral-muted">${Ui.won(row.cost())}</span>
                                    <span class="col-num t4-bold ${Ui.signClass(row.profit())}">${Ui.won(row.profit())}</span>
                                </a>
                            @endfor
                        </div>
                    </div>
                </section>
            </div>
        </div>
    @endif
`)
