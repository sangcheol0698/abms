@import java.util.List
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.domain.Money
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.party.Party
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.project.ProjectSections.ExpenseSection
@import kr.co.abacus.abms.project.ProjectSections.RevenueSection
@import kr.co.abacus.abms.project.ProjectSections.StaffingSection
@import kr.co.abacus.abms.project.ProjectStatus
@import kr.co.abacus.abms.summary.MonthlyRevenueSummary
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param Project project
@param Party party
@param DepartmentTree tree
@param boolean canWrite
@param RevenueSection revenue
@param ExpenseSection expense
@param StaffingSection staffing
@param List<MonthlyRevenueSummary> history
@param boolean showHistory
@param kr.co.abacus.abms.project.ProjectPlaceService.ResolvedPlace place
@param kr.co.abacus.abms.project.ProjectPlaceService.NearestSite nearestSite = null
@param java.util.List<kr.co.abacus.abms.common.audit.AuditEntry> auditHistory = null
@param kr.co.abacus.abms.attachment.AttachmentSection attachments = null
@template.layout.app(ctx = ctx, aside = @`
            <dl class="property-list">
            <dt>상태</dt>
            <dd class="flex items-center gap-x2">@template.components.statusIcon(status = project.getStatus())${project.getStatus().label()}</dd>
            <dt>코드</dt>
            <dd class="tabular-nums">${project.getCode()}</dd>
            <dt>협력사</dt>
            <dd>@if(ctx.can(PermissionCode.PARTY_READ))<a class="link" href="/parties/${party.id()}">${party.getName()}</a>@else${party.getName()}@endif</dd>
            <dt>주관 부서</dt>
            <dd><a class="link" href="/departments?selected=${project.getLeadDepartmentId()}">${tree.nameOf(project.getLeadDepartmentId())}</a></dd>
            <dt>시작일</dt>
            <dd class="tabular-nums">${Ui.date(project.getPeriod().startDate())}</dd>
            <dt>종료일</dt>
            <dd class="tabular-nums">${Ui.date(project.getPeriod().endDate())}</dd>
            <dt>계약금액</dt>
            <dd class="t4-bold tabular-nums">${Ui.won(project.getContractAmount())}원</dd>
            <dt>수행 장소</dt>
            <dd>${place.label()}</dd>
            </dl>
            @if(showHistory && !history.isEmpty())
            !{Money sumRevenue = history.stream().map(MonthlyRevenueSummary::getRevenueAmount).reduce(Money.ZERO, Money::plus);}
            !{Money sumManaged = history.stream().map(MonthlyRevenueSummary::getManagedRevenueAmount).reduce(Money.ZERO, Money::plus);}
            !{Money sumCost = history.stream().map(MonthlyRevenueSummary::getCostAmount).reduce(Money.ZERO, Money::plus);}
            <div class="mt-x5 border-t border-stroke-neutral-subtle pt-x4">
                <h2 class="mb-x3 t3-medium text-fg-neutral-subtle">누적 손익</h2>
                <dl class="property-list">
                    <dt>매출 (청구)</dt><dd class="tabular-nums">${Ui.won(sumRevenue)}</dd>
                    <dt>매출 (진행)</dt><dd class="tabular-nums">${Ui.won(sumManaged)}</dd>
                    <dt>비용</dt><dd class="tabular-nums">${Ui.won(sumCost)}</dd>
                    <dt>이익 (청구)</dt><dd class="t4-bold tabular-nums ${Ui.signClass(sumRevenue.minus(sumCost))}">${Ui.won(sumRevenue.minus(sumCost))}</dd>
                    <dt>이익 (진행)</dt><dd class="t4-bold tabular-nums ${Ui.signClass(sumManaged.minus(sumCost))}">${Ui.won(sumManaged.minus(sumCost))}</dd>
                </dl>
            </div>
            @endif
`, title = project.getName(), content = @`
    @template.components.pageHeader(readOnly = !canWrite ? "이 프로젝트를 수정할 권한이 없습니다. 주관 부서·PM 범위 안에서만 수정할 수 있습니다." : null, title = project.getName(), subtitle = project.getCode(), breadcrumb = @`
        <a href="/projects" class="hover:text-fg-neutral-muted">프로젝트</a>${Icons.of("chevron-right", "size-3")}<span>${project.getName()}</span>
    `, actions = @`
        @if(canWrite)
            <a href="/projects/${project.id()}/edit" class="${Seed.button("neutralOutline")}">${Icons.of("pencil", "size-4")} 수정</a>
            <details class="relative" data-dropdown>
                <summary class="${Seed.button("neutralOutline")}">더보기 ${Icons.of("chevron-down", "size-4")}</summary>
                <div class="dropdown-panel w-44 p-x1">
                    @if(!project.getStatus().isClosed())
                        <form method="post" action="/projects/${project.id()}/complete" hx-confirm="프로젝트를 완료 처리할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                            <button class="menu-item">완료 처리</button>
                        </form>
                        <form method="post" action="/projects/${project.id()}/cancel" hx-confirm="프로젝트를 취소 처리할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                            <button class="menu-item">취소 처리</button>
                        </form>
                    @endif
                    <form method="post" action="/projects/${project.id()}/delete" hx-confirm="프로젝트와 매출 계획, 투입 정보, 직접비를 모두 삭제할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                        <button class="menu-item text-fg-critical">삭제</button>
                    </form>
                </div>
            </details>
        @endif
    `)

    <div class="min-w-0 space-y-4">
    @if(project.getDescription() != null)
        <p class="t5-regular text-fg-neutral-muted">${project.getDescription()}</p>
    @endif

    <section class="card" id="revenue-section">
        @template.project.revenueSection(section = revenue)
    </section>

    <section class="card" id="staffing-section">
        @template.project.staffingSection(section = staffing)
    </section>

    <section class="card" id="expense-section">
        @template.project.expenseSection(section = expense)
    </section>

    @if(showHistory)
        <section class="card overflow-hidden">
            <div class="card-header">
                <h2 class="card-title">월별 손익 집계</h2>
                <span class="text-xs text-fg-neutral-subtle">청구 = 발행된 매출 · 진행 = 계약금액 기간 일할 · 매일 새벽 자동 집계</span>
            </div>
            @if(history.isEmpty())
                @template.components.empty(message = "아직 집계된 손익이 없습니다.", icon = "chart")
            @else
                !{Money totalRevenue = history.stream().map(MonthlyRevenueSummary::getRevenueAmount).reduce(Money.ZERO, Money::plus);}
                !{Money totalManaged = history.stream().map(MonthlyRevenueSummary::getManagedRevenueAmount).reduce(Money.ZERO, Money::plus);}
                !{Money totalLabor = history.stream().map(MonthlyRevenueSummary::getLaborCostAmount).reduce(Money.ZERO, Money::plus);}
                !{Money totalDirect = history.stream().map(MonthlyRevenueSummary::getDirectCostAmount).reduce(Money.ZERO, Money::plus);}
                !{Money totalCost = totalLabor.plus(totalDirect);}
                <div class="overflow-x-auto">
                    <table class="table">
                        <thead>
                            <tr><th>월</th><th class="num">매출 (청구)</th><th class="num">매출 (진행)</th><th class="num">인건비</th><th class="num">직접비</th><th class="num">이익 (청구)</th><th class="num">이익 (진행)</th><th>집계 시각</th></tr>
                        </thead>
                        <tbody class="divide-y divide-stroke-neutral-subtle">
                        @for(MonthlyRevenueSummary s : history)
                            <tr>
                                <td><a class="link" href="/summary?month=${java.time.YearMonth.from(s.getTargetMonth()).toString()}">${Ui.month(java.time.YearMonth.from(s.getTargetMonth()))}</a></td>
                                <td class="num">${Ui.won(s.getRevenueAmount())}</td>
                                <td class="num text-fg-neutral-muted">${Ui.won(s.getManagedRevenueAmount())}</td>
                                <td class="num">${Ui.won(s.getLaborCostAmount())}</td>
                                <td class="num">${Ui.won(s.getDirectCostAmount())}</td>
                                <td class="num font-semibold ${Ui.signClass(s.getProfitAmount())}">${Ui.won(s.getProfitAmount())}</td>
                                <td class="num font-semibold ${Ui.signClass(s.getManagedProfitAmount())}">${Ui.won(s.getManagedProfitAmount())}</td>
                                <td class="text-xs text-fg-neutral-subtle">${Ui.dateTime(s.getCalculatedAt())}</td>
                            </tr>
                        @endfor
                        </tbody>
                        <tfoot class="bg-bg-neutral-muted text-sm font-semibold">
                            <tr>
                                <td class="px-4 py-3">합계</td>
                                <td class="px-4 py-3 text-right tabular-nums">${Ui.won(totalRevenue)}</td>
                                <td class="px-4 py-3 text-right tabular-nums">${Ui.won(totalManaged)}</td>
                                <td class="px-4 py-3 text-right tabular-nums">${Ui.won(totalLabor)}</td>
                                <td class="px-4 py-3 text-right tabular-nums">${Ui.won(totalDirect)}</td>
                                <td class="px-4 py-3 text-right tabular-nums ${Ui.signClass(totalRevenue.minus(totalCost))}">${Ui.won(totalRevenue.minus(totalCost))}</td>
                                <td class="px-4 py-3 text-right tabular-nums ${Ui.signClass(totalManaged.minus(totalCost))}">${Ui.won(totalManaged.minus(totalCost))}</td>
                                <td></td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            @endif
        </section>
    @endif
        @if(attachments != null)
            <div class="mt-6">@template.attachment.section(section = attachments)</div>
        @endif
        @if(auditHistory != null)
            <div class="mt-6">@template.components.auditHistory(entries = auditHistory, showTarget = true)</div>
        @endif
    @if(place.workPlace() != null)
        !{var markers = new java.util.ArrayList<kr.co.abacus.abms.common.web.MapMarker>();}
        !{var placeMarker = kr.co.abacus.abms.common.web.MapMarker.of(project.getName(), place.location().fullAddress(), place.location(), null);}
        !{if (placeMarker != null) markers.add(placeMarker);}
        !{var siteMarker = nearestSite == null || place.workPlace() == kr.co.abacus.abms.project.WorkPlace.OFFICE ? null : kr.co.abacus.abms.common.web.MapMarker.of(nearestSite.site().getName(), nearestSite.site().getLocation().fullAddress(), nearestSite.site().getLocation(), "/sites/" + nearestSite.site().id());}
        !{if (siteMarker != null) markers.add(siteMarker);}
        <section>
            <h2 class="section-title">수행 장소 <span class="section-count">${place.label()}@if(place.source() != null) · ${place.source()}@endif</span></h2>
            @if(place.location().isEmpty())
                <div class="list-box">@template.components.empty(message = place.workPlace() == kr.co.abacus.abms.project.WorkPlace.REMOTE ? "원격으로 수행하는 프로젝트입니다." : "수행 장소 주소가 없습니다. 협력사·사업장 주소를 등록하거나 프로젝트에서 직접 입력하세요.", icon = "map-pin")</div>
            @else
                @template.components.map(markers = markers, mapEnabled = ctx.mapEnabled(), height = "h-64", fallbackText = place.location().fullAddress(),
                        fallbackHref = place.location().kakaoMapLink(project.getName()))
                <p class="mt-x2 flex flex-wrap items-center gap-x-x3 gap-y-x1 t3-regular text-fg-neutral-subtle">
                    <span class="inline-flex items-center gap-x1">${Icons.of("map-pin", "size-3.5")}${place.location().fullAddress()}</span>
                    @if(siteMarker != null)
                        <span>가장 가까운 사업장 <a class="link" href="/sites/${nearestSite.site().id()}">${nearestSite.site().getName()}</a> ${nearestSite.distanceLabel()}</span>
                    @endif
                    <a href="${place.location().kakaoMapLink(project.getName())}" target="_blank" rel="noopener noreferrer" class="link inline-flex items-center gap-x1">카카오맵 ${Icons.of("external", "size-3")}</a>
                </p>
            @endif
        </section>
    @endif

    </div>

`)
