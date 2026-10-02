@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.access.PermissionGroup
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@param ViewContext ctx
@param List<PermissionGroup> groups
@param Map<Long, Long> memberCounts
@param Map<Long, Integer> grantCounts
@template.layout.app(ctx = ctx, title = "권한 그룹", content = @`
    @template.components.pageHeader(title = "권한 그룹", subtitle = "권한(기능)과 범위(데이터)를 묶어 계정에 부여합니다.", actions = @`
        <button type="button" class="btn btn-primary" hx-get="/admin/permission-groups/new" hx-target="#modal-body">${Icons.of("plus", "size-4")} 그룹 추가</button>
    `)
    <div class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
        @for(PermissionGroup g : groups)
            <a href="/admin/permission-groups/${g.id()}" class="card block p-5 transition hover:ring-brand-300">
                <div class="flex items-center justify-between gap-2">
                    <h2 class="font-semibold text-slate-900">${g.getName()}</h2>
                    <span class="badge ${g.isSystem() ? "badge-gray" : "badge-blue"}">${g.getGroupType().label()}</span>
                </div>
                <p class="mt-2 line-clamp-2 text-sm text-slate-500">${g.getDescription()}</p>
                <div class="mt-4 flex gap-4 text-xs text-slate-500">
                    <span>${Icons.of("users", "mr-1 inline size-4")} 계정 ${memberCounts.getOrDefault(g.id(), 0L)}개</span>
                    <span>${Icons.of("shield", "mr-1 inline size-4")} 권한 ${grantCounts.getOrDefault(g.id(), 0)}개</span>
                </div>
            </a>
        @endfor
    </div>
`)
