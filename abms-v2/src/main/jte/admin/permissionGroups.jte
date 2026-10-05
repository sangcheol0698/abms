@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.access.PermissionGroup
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param List<PermissionGroup> groups
@param Map<Long, Long> memberCounts
@param Map<Long, Integer> grantCounts
@template.layout.app(ctx = ctx, title = "권한 그룹", fill = true, content = @`
<%-- 데스크톱: 페이지는 고정, 목록 본문만 스크롤 (fill 레이아웃). 태블릿·모바일은 문서 스크롤. --%>
<div class="flex flex-col lg:h-full">
    <div class="px-x4 pt-x5 sm:px-x6 lg:px-x8 lg:pt-x6">
    @template.components.pageHeader(title = "권한 그룹", subtitle = "권한(기능)과 범위(데이터)를 묶어 계정에 부여합니다.", actions = @`
        <button type="button" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}" hx-get="/admin/permission-groups/new" hx-target="#modal-body">${Icons.of("plus", "size-4")} 그룹 추가</button>
    `)
    </div>

    <div class="flex min-h-0 flex-1 flex-col border-t border-stroke-neutral-subtle">
        <div class="list-head shrink-0 sm:px-x6 lg:px-x8" aria-hidden="true">
            <span class="w-7 shrink-0"></span>
            <span class="min-w-0 flex-1">그룹</span>
            <span class="hidden w-24 @xl:block">계정</span>
            <span class="hidden w-24 @xl:block">권한</span>
            <span class="w-4 shrink-0"></span>
        </div>
        <div class="min-h-0 flex-1 lg:overflow-y-auto lg:overscroll-contain" data-fill-scroll>
        @for(PermissionGroup g : groups)
            <a href="/admin/permission-groups/${g.id()}" class="list-row min-h-14 sm:px-x6 lg:px-x8">
                <span class="flex size-7 shrink-0 items-center justify-center rounded-r1_5 bg-bg-neutral-weak text-fg-neutral-muted">${Icons.of("shield", "size-4")}</span>
                <span class="min-w-0 flex-1">
                    <span class="flex items-center gap-x2"><span class="truncate t4-medium">${g.getName()}</span><span class="${Seed.badge(g.isSystem() ? "neutral" : "informative")}">${g.getGroupType().label()}</span></span>
                    <span class="block truncate t3-regular text-fg-neutral-subtle">${g.getDescription()}</span>
                </span>
                <span class="list-cell-muted hidden w-24 items-center gap-x1 @xl:flex">${Icons.of("users", "size-4")} 계정 ${memberCounts.getOrDefault(g.id(), 0L)}</span>
                <span class="list-cell-muted hidden w-24 items-center gap-x1 @xl:flex">${Icons.of("shield", "size-4")} 권한 ${grantCounts.getOrDefault(g.id(), 0)}</span>
                ${Icons.of("chevron-right", "size-4 shrink-0 text-fg-neutral-subtle")}
            </a>
        @endfor
        </div>
    </div>
</div>
`)
