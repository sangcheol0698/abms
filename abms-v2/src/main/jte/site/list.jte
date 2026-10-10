@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.MapMarker
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.web.ViewContext
@import kr.co.abacus.abms.site.Site
@import kr.co.abacus.abms.site.SiteType
@param ViewContext ctx
@param List<Site> sites
@param Map<Long, Integer> departmentCounts
@param Map<Long, Integer> headcounts
@param List<MapMarker> markers
@template.layout.app(ctx = ctx, title = "사업장", fill = true, content = @`
<%-- 데스크톱: 페이지는 고정, 목록 본문만 스크롤 (fill 레이아웃). 태블릿·모바일은 문서 스크롤. --%>
<div class="flex flex-col lg:h-full">
    <div class="px-x4 pt-x5 sm:px-x6 lg:px-x8 lg:pt-x6">
    @template.components.pageHeader(title = "사업장", subtitle = "본사·지사·연구소 등 근무지를 관리합니다.", actions = @`
        @if(ctx.can(PermissionCode.DEPARTMENT_WRITE))
            <a href="/sites/new" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}">${Icons.of("plus", "size-4")} 사업장 등록</a>
        @endif
    `)
    </div>

    <div class="flex min-h-0 flex-1 flex-col border-t border-stroke-neutral-subtle">
        <div class="list-head shrink-0 sm:px-x6 lg:px-x8" aria-hidden="true">
            <span class="w-6 shrink-0"></span>
            <span class="min-w-0 flex-1">사업장 <span class="tabular-nums">${sites.size()}</span></span>
            <span class="hidden min-w-0 flex-1 @3xl:block">주소</span>
            <span class="hidden w-20 text-right @xl:block">부서</span>
            <span class="w-16 shrink-0 text-right">인원</span>
            @if(ctx.mapEnabled())<span class="w-7 shrink-0"></span>@endif
        </div>
        <div class="min-h-0 flex-1 lg:overflow-y-auto lg:overflow-x-hidden lg:overscroll-y-contain" data-fill-scroll>
            @if(sites.isEmpty())
                @template.components.empty(message = "등록된 사업장이 없습니다.", icon = "map-pin")
            @else
                @for(Site s : sites)
                    <div class="list-row sm:px-x6 lg:px-x8">
                        <span class="flex size-6 shrink-0 items-center justify-center rounded-r1_5 bg-bg-neutral-weak text-fg-neutral-muted">${Icons.of(s.getSiteType() == SiteType.HEADQUARTERS ? "building" : "map-pin", "size-3.5")}</span>
                        <a href="/sites/${s.id()}" class="flex min-w-0 flex-1 items-center gap-x2">
                            <span class="truncate t4-medium">${s.getName()}</span>
                            <span class="${Seed.badge(s.getSiteType() == SiteType.HEADQUARTERS ? "informative" : "neutral", "weak")}">${s.getSiteType().label()}</span>
                        </a>
                        <span class="list-cell-muted hidden min-w-0 flex-1 truncate @3xl:block">${s.getLocation().isEmpty() ? "주소 미등록" : s.getLocation().fullAddress()}</span>
                        <span class="list-cell-muted hidden w-20 text-right @xl:block">${departmentCounts.getOrDefault(s.id(), 0)}곳</span>
                        <span class="w-16 shrink-0 text-right t3-regular tabular-nums">${headcounts.getOrDefault(s.id(), 0)}명</span>
                        @if(ctx.mapEnabled())
                            @if(s.getLocation().hasCoordinates())
                                <button type="button" class="${Seed.iconButton("ghost", "xsmall")} w-7 shrink-0" aria-label="${s.getName()} 지도에서 보기" title="지도에서 보기"
                                        data-map-focus="${s.getLocation().latitude().toPlainString()},${s.getLocation().longitude().toPlainString()}">${Icons.of("map", "size-4")}</button>
                            @else
                                <span class="w-7 shrink-0"></span>
                            @endif
                        @endif
                    </div>
                @endfor
                <div class="px-x4 py-x5 sm:px-x6 lg:px-x8">
                    @template.components.map(markers = markers, mapEnabled = ctx.mapEnabled(), height = "h-72 @3xl:h-96",
                            fallbackText = markers.isEmpty() ? null : "사업장 " + markers.size() + "곳")
                </div>
            @endif
        </div>
    </div>
</div>
`)
