@import java.util.Map
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.party.Party
@param ViewContext ctx
@param PageView<Party> page
@param String q = null
@param Map<Long, Long> projectCounts
@template.layout.app(ctx = ctx, title = "협력사", content = @`
    @template.components.pageHeader(title = "협력사", subtitle = "프로젝트 고객사와 파트너 정보를 관리합니다.", actions = @`
        @if(ctx.can(PermissionCode.PARTY_WRITE))
            <a href="/parties/new" class="btn btn-primary">${Icons.of("plus", "size-4")} 협력사 등록</a>
        @endif
    `)
    <div class="card">
        <form class="border-b border-slate-100 p-4" action="/parties" hx-get="/parties" hx-target="#party-results" hx-push-url="true"
              hx-trigger="input changed delay:300ms from:input[name=q], submit">
            <div class="relative max-w-sm">
                <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-slate-400">${Icons.of("search", "size-4")}</span>
                <input type="search" name="q" value="${Ui.value(q)}" placeholder="협력사명 검색" class="input pl-9" aria-label="협력사명 검색">
            </div>
        </form>
        <div id="party-results">@template.party.results(page = page, projectCounts = projectCounts)</div>
    </div>
`)
