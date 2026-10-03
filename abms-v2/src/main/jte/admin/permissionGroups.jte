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
@template.layout.app(ctx = ctx, title = "권한 그룹", content = @`
    @template.components.pageHeader(title = "권한 그룹", subtitle = "권한(기능)과 범위(데이터)를 묶어 계정에 부여합니다.", actions = @`
        <button type="button" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}" hx-get="/admin/permission-groups/new" hx-target="#modal-body">${Icons.of("plus", "size-4")} 그룹 추가</button>
    `)
    <div class="-mx-4 border-t border-stroke-neutral-subtle sm:-mx-6 lg:-mx-8">
        @for(PermissionGroup g : groups)
            <a href="/admin/permission-groups/${g.id()}" class="list-row min-h-14 sm:px-x6 lg:px-x8">
                <span class="flex size-7 shrink-0 items-center justify-center rounded-r1_5 bg-bg-neutral-weak text-fg-neutral-muted">${Icons.of("shield", "size-4")}</span>
                <span class="min-w-0 flex-1">
                    <span class="flex items-center gap-x2"><span class="truncate t4-medium">${g.getName()}</span><span class="${Seed.badge(g.isSystem() ? "neutral" : "informative")}">${g.getGroupType().label()}</span></span>
                    <span class="block truncate t3-regular text-fg-neutral-subtle">${g.getDescription()}</span>
                </span>
                <span class="list-cell-muted hidden w-24 items-center gap-x1 sm:flex">${Icons.of("users", "size-4")} 계정 ${memberCounts.getOrDefault(g.id(), 0L)}</span>
                <span class="list-cell-muted hidden w-24 items-center gap-x1 sm:flex">${Icons.of("shield", "size-4")} 권한 ${grantCounts.getOrDefault(g.id(), 0)}</span>
                ${Icons.of("chevron-right", "size-4 shrink-0 text-fg-neutral-subtle")}
            </a>
        @endfor
    </div>
`)
