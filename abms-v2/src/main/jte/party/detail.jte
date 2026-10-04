@import java.util.List
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.MapMarker
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.party.Party
@import kr.co.abacus.abms.party.PartyInsight
@import kr.co.abacus.abms.party.PartyType
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param Party party
@param List<Project> projects
@param long projectCount
@param DepartmentTree tree
@param PartyInsight insight
@param List<MapMarker> markers
@template.layout.app(ctx = ctx, aside = @`
    <dl class="property-list">
        <dt>구분</dt>
        <dd><span class="${Seed.badge(party.getPartyType() == PartyType.PARTNER ? "neutral" : "informative", party.getPartyType() == PartyType.BOTH ? "outline" : "weak")}">${party.getPartyType().label()}</span></dd>
        <dt>사업자번호</dt>
        <dd class="tabular-nums">${party.getBusinessNumber() == null ? "-" : party.getBusinessNumber()}</dd>
        <dt>대표자</dt>
        <dd>${party.getCeoName() == null ? "-" : party.getCeoName()}</dd>
        <dt>업종</dt>
        <dd>${party.getIndustry() == null ? "-" : party.getIndustry()}</dd>
        <dt>대표번호</dt>
        <dd class="tabular-nums">@if(party.getPhone() != null)<a class="link" href="tel:${party.getPhone()}">${party.getPhone()}</a>@else-@endif</dd>
        <dt>웹사이트</dt>
        <dd>@if(party.getWebsite() != null)<a class="link" href="${party.getWebsite()}" target="_blank" rel="noopener noreferrer" title="${party.getWebsite()}">${party.getWebsite().replaceFirst("^https?://", "")}</a>@else-@endif</dd>
        <dt>주소</dt>
        <dd class="whitespace-normal">${party.getLocation().isEmpty() ? "-" : party.getLocation().fullAddress()}</dd>
        <dt>첫 거래</dt>
        <dd class="tabular-nums">${insight.firstStartDate() == null ? "-" : Ui.date(insight.firstStartDate())}</dd>
    </dl>
    <div class="mt-x5 border-t border-stroke-neutral-subtle pt-x4">
        <h3 class="mb-x3 t3-medium text-fg-neutral-subtle">영업 담당자</h3>
        <dl class="property-list">
            <dt>이름</dt>
            <dd>${party.getSalesRepName() == null ? "-" : party.getSalesRepName()}</dd>
            <dt>연락처</dt>
            <dd class="tabular-nums">@if(party.getSalesRepPhone() != null)<a class="link" href="tel:${party.getSalesRepPhone()}">${party.getSalesRepPhone()}</a>@else-@endif</dd>
            <dt>이메일</dt>
            <dd>@if(party.getSalesRepEmail() != null)<a class="link" href="mailto:${party.getSalesRepEmail()}" title="${party.getSalesRepEmail()}">${party.getSalesRepEmail()}</a>@else-@endif</dd>
        </dl>
    </div>
`, title = party.getName(), content = @`
    @template.components.pageHeader(title = party.getName(), breadcrumb = @`
        <a href="/parties" class="hover:text-fg-neutral-muted">협력사</a>${Icons.of("chevron-right", "size-3")}<span>${party.getName()}</span>
    `, actions = @`
        @if(ctx.can(PermissionCode.PARTY_WRITE))
            <a href="/parties/${party.id()}/edit" class="${Seed.button("neutralOutline")}">${Icons.of("pencil", "size-4")} 수정</a>
            @if(projectCount == 0)
                <form method="post" action="/parties/${party.id()}/delete" hx-confirm="${party.getName()} 협력사를 삭제할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                    <button class="${Seed.button("neutralOutline")} text-fg-critical">${Icons.of("trash", "size-4")} 삭제</button>
                </form>
            @endif
        @endif
    `)

    <div class="space-y-6">
        <div class="-mt-x3 flex flex-wrap items-center gap-x1_5">
            <span class="${Seed.badge(party.getPartyType() == PartyType.PARTNER ? "neutral" : "informative", party.getPartyType() == PartyType.BOTH ? "outline" : "weak")}">${party.getPartyType().label()}</span>
            @if(party.getIndustry() != null)<span class="${Seed.badge("neutral", "outline")}">${party.getIndustry()}</span>@endif
            @if(party.getBusinessNumber() != null)<span class="t3-regular text-fg-neutral-subtle tabular-nums">사업자 ${party.getBusinessNumber()}</span>@endif
        </div>

        <%-- 거래 요약 (조회 가능한 프로젝트 기준) --%>
        <div class="list-box grid grid-cols-2 gap-px bg-stroke-neutral-subtle @3xl:grid-cols-4 [&>div]:bg-bg-layer-default">
            <div class="px-x4 py-x3">
                <p class="stat-label">프로젝트</p>
                <p class="stat-value">${insight.projectCount()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">건</span></p>
                <p class="mt-x0_5 t3-regular text-fg-neutral-subtle">진행 중 ${insight.inProgressCount()}건</p>
            </div>
            <div class="px-x4 py-x3">
                <p class="stat-label">누적 계약금액</p>
                <p class="stat-value">${insight.totalContract().compact()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">원</span></p>
                <p class="mt-x0_5 t3-regular text-fg-neutral-subtle tabular-nums">${Ui.won(insight.totalContract())}원</p>
            </div>
            <div class="px-x4 py-x3">
                <p class="stat-label">발행 매출 누적</p>
                <p class="stat-value">${insight.issuedTotal().compact()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">원</span></p>
                <p class="mt-x0_5 t3-regular text-fg-neutral-subtle">올해 ${insight.thisYearIssued().compact()}원</p>
            </div>
            <div class="px-x4 py-x3">
                <p class="stat-label">남은 청구</p>
                <p class="stat-value">${insight.unissuedPlanned().plus(insight.unplanned()).compact()}<span class="ml-x0_5 t4-regular text-fg-neutral-subtle">원</span></p>
                <p class="mt-x0_5 t3-regular text-fg-neutral-subtle">미발행 ${insight.unissuedPlanned().compact()} · 미계획 ${insight.unplanned().compact()}</p>
            </div>
        </div>

        <div class="grid grid-cols-1 gap-6 @4xl:grid-cols-2">
            <section class="min-w-0">
                <h2 class="section-title">연도별 발행 매출 <span class="section-count">최근 5년</span></h2>
                <div class="list-box p-x4">
                    <div class="space-y-x2_5">
                        @for(PartyInsight.YearAmount item : insight.yearly())
                            <div class="flex items-center gap-x3">
                                <span class="w-12 shrink-0 t3-regular text-fg-neutral-subtle tabular-nums">${item.year()}</span>
                                <div class="meter flex-1"><span class="h-full bg-bg-brand-solid" style="width: ${insight.yearPercent(item)}%"></span></div>
                                <span class="w-20 shrink-0 text-right t4-medium tabular-nums">${item.amount().compact()}</span>
                            </div>
                        @endfor
                    </div>
                </div>
            </section>
            <section class="min-w-0">
                <h2 class="section-title">청구 일정 <span class="section-count">기한 경과 · 90일 이내 미발행</span></h2>
                <div class="list-box">
                    @if(insight.upcoming().isEmpty())
                        @template.components.empty(message = "예정된 청구가 없습니다.", icon = "money")
                    @else
                        @for(PartyInsight.UpcomingPlan item : insight.upcoming())
                            <a href="/projects/${item.project().id()}" class="list-row">
                                <span class="w-20 shrink-0 t3-regular tabular-nums ${item.overdue() ? "text-fg-critical" : "text-fg-neutral-subtle"}">${Ui.date(item.plan().getRevenueDate())}</span>
                                <span class="min-w-0 flex-1 truncate t4-medium">${item.project().getName()}</span>
                                <span class="list-cell-muted hidden w-20 @md:block">${item.plan().getSequence()}차 ${item.plan().getType().label()}</span>
                                <span class="w-24 shrink-0 text-right t4-bold tabular-nums">${item.plan().getAmount().compact()}</span>
                            </a>
                        @endfor
                    @endif
                </div>
            </section>
        </div>

        <section class="min-w-0">
            <h2 class="section-title">프로젝트 <span class="section-count">${projects.size()}</span></h2>
            <div class="list-box">
                @if(projects.isEmpty())
                    @template.components.empty(message = ctx.can(PermissionCode.PROJECT_READ) ? "조회 가능한 프로젝트가 없습니다." : "프로젝트 조회 권한이 없습니다.", icon = "folder")
                @else
                    @for(Project p : projects)
                        @template.components.projectRow(project = p, tree = tree)
                    @endfor
                @endif
            </div>
        </section>

        @if(!party.getLocation().isEmpty())
            <section class="min-w-0">
                <h2 class="section-title">위치</h2>
                @template.components.map(markers = markers, mapEnabled = ctx.mapEnabled(), fallbackText = party.getLocation().fullAddress(),
                        fallbackHref = party.getLocation().kakaoMapLink(party.getName()))
                <p class="mt-x2 flex flex-wrap items-center gap-x-x3 gap-y-x1 t3-regular text-fg-neutral-subtle">
                    <span class="inline-flex items-center gap-x1">${Icons.of("map-pin", "size-3.5")}@if(party.getLocation().zipCode() != null)<span class="tabular-nums">(${party.getLocation().zipCode()})</span>@endif ${party.getLocation().fullAddress()}</span>
                    <a href="${party.getLocation().kakaoMapLink(party.getName())}" target="_blank" rel="noopener noreferrer" class="link inline-flex items-center gap-x1">카카오맵 ${Icons.of("external", "size-3")}</a>
                </p>
            </section>
        @endif

        @if(party.getMemo() != null)
            <section>
                <h2 class="section-title">메모</h2>
                <p class="rounded-r2 bg-bg-neutral-muted p-x3 t4-regular whitespace-pre-line text-fg-neutral-muted">${party.getMemo()}</p>
            </section>
        @endif
    </div>
`)
