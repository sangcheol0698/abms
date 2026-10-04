@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.MapMarker
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.site.Site
@import kr.co.abacus.abms.site.SiteController.NearbyParty
@import kr.co.abacus.abms.site.SiteType
@param ViewContext ctx
@param Site site
@param List<Department> departments
@param Map<Long, Integer> memberCounts
@param int headcount
@param List<NearbyParty> nearby
@param List<MapMarker> markers
@param java.util.List<kr.co.abacus.abms.common.audit.AuditEntry> auditHistory = null
@template.layout.app(ctx = ctx, aside = @`
    <dl class="property-list">
        <dt>유형</dt>
        <dd><span class="${Seed.badge(site.getSiteType() == SiteType.HEADQUARTERS ? "informative" : "neutral", "weak")}">${site.getSiteType().label()}</span></dd>
        <dt>대표번호</dt>
        <dd class="tabular-nums">@if(site.getPhone() != null)<a class="link" href="tel:${site.getPhone()}">${site.getPhone()}</a>@else-@endif</dd>
        <dt>우편번호</dt>
        <dd class="tabular-nums">${site.getLocation().zipCode() == null ? "-" : site.getLocation().zipCode()}</dd>
        <dt>주소</dt>
        <dd class="whitespace-normal">${site.getLocation().isEmpty() ? "-" : site.getLocation().fullAddress()}</dd>
        <dt>부서</dt>
        <dd class="tabular-nums">${departments.size()}곳</dd>
        <dt>근무 인원</dt>
        <dd class="tabular-nums">${headcount}명</dd>
    </dl>
`, title = site.getName(), content = @`
    @template.components.pageHeader(title = site.getName(), breadcrumb = @`
        <a href="/sites" class="hover:text-fg-neutral-muted">사업장</a>${Icons.of("chevron-right", "size-3")}<span>${site.getName()}</span>
    `, actions = @`
        @if(ctx.can(PermissionCode.DEPARTMENT_WRITE))
            <a href="/sites/${site.id()}/edit" class="${Seed.button("neutralOutline")}">${Icons.of("pencil", "size-4")} 수정</a>
            @if(departments.isEmpty())
                <form method="post" action="/sites/${site.id()}/delete" hx-confirm="${site.getName()} 사업장을 삭제할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                    <button class="${Seed.button("neutralOutline")} text-fg-critical">${Icons.of("trash", "size-4")} 삭제</button>
                </form>
            @endif
        @endif
    `)

    <div class="space-y-6">
        <div class="-mt-x3 flex flex-wrap items-center gap-x1_5">
            <span class="${Seed.badge(site.getSiteType() == SiteType.HEADQUARTERS ? "informative" : "neutral", "weak")}">${site.getSiteType().label()}</span>
            @if(!site.getLocation().isEmpty())
                <span class="inline-flex items-center gap-x1 t3-regular text-fg-neutral-subtle">${Icons.of("map-pin", "size-3.5")}${site.getLocation().fullAddress()}</span>
            @endif
        </div>

        <section class="min-w-0">
            <h2 class="section-title">위치</h2>
            @if(site.getLocation().isEmpty())
                <div class="list-box">@template.components.empty(message = "주소가 등록되지 않았습니다.", icon = "map-pin")</div>
            @else
                @template.components.map(markers = markers, mapEnabled = ctx.mapEnabled(), height = "h-72", fallbackText = site.getLocation().fullAddress(),
                        fallbackHref = site.getLocation().kakaoMapLink(site.getName()))
                <p class="mt-x2 flex flex-wrap items-center gap-x3 t3-regular text-fg-neutral-subtle">
                    <a href="${site.getLocation().kakaoMapLink(site.getName())}" target="_blank" rel="noopener noreferrer" class="link inline-flex items-center gap-x1">카카오맵에서 길찾기 ${Icons.of("external", "size-3")}</a>
                </p>
            @endif
        </section>

        <div class="grid grid-cols-1 gap-6 @4xl:grid-cols-2">
            <section class="min-w-0">
                <h2 class="section-title">근무 부서 <span class="section-count">${departments.size()}</span></h2>
                <div class="list-box">
                    @if(departments.isEmpty())
                        @template.components.empty(message = "이 사업장에서 근무하는 부서가 없습니다. 부서 수정에서 사업장을 지정하세요.", icon = "building")
                    @else
                        @for(Department d : departments)
                            <a href="/departments?selected=${d.id()}" class="list-row">
                                ${Icons.of("building", "size-4 shrink-0 text-fg-neutral-subtle")}
                                <span class="min-w-0 flex-1 truncate t4-medium">${d.getName()}</span>
                                <span class="list-cell-muted w-14">${d.getType().label()}</span>
                                <span class="w-12 shrink-0 text-right t3-regular tabular-nums">${memberCounts.getOrDefault(d.id(), 0)}명</span>
                            </a>
                        @endfor
                    @endif
                </div>
            </section>
            @if(ctx.can(PermissionCode.PARTY_READ))
                <section class="min-w-0">
                    <h2 class="section-title">가까운 협력사 <span class="section-count">직선 거리</span></h2>
                    <div class="list-box">
                        @if(nearby.isEmpty())
                            @template.components.empty(message = site.getLocation().hasCoordinates() ? "지도 위치가 등록된 협력사가 없습니다." : "사업장의 지도 위치가 없어 거리를 계산할 수 없습니다.", icon = "briefcase")
                        @else
                            @for(NearbyParty n : nearby)
                                <a href="/parties/${n.party().id()}" class="list-row">
                                    <span class="flex size-6 shrink-0 items-center justify-center rounded-r1_5 bg-bg-neutral-weak t3-bold text-fg-neutral-muted">${n.party().getName().substring(0, 1)}</span>
                                    <span class="min-w-0 flex-1 truncate t4-medium">${n.party().getName()}</span>
                                    <span class="list-cell-muted hidden min-w-0 max-w-48 truncate @xl:block">${n.party().getLocation().address()}</span>
                                    <span class="w-16 shrink-0 text-right t3-medium tabular-nums">${n.distanceLabel()}</span>
                                </a>
                            @endfor
                        @endif
                    </div>
                </section>
            @endif
        </div>

        @if(site.getMemo() != null)
            <section>
                <h2 class="section-title">메모</h2>
                <p class="rounded-r2 bg-bg-neutral-muted p-x3 t4-regular whitespace-pre-line text-fg-neutral-muted">${site.getMemo()}</p>
            </section>
        @endif
        @if(auditHistory != null)
            <div class="mt-6">@template.components.auditHistory(entries = auditHistory, showTarget = true)</div>
        @endif
    </div>
`)
