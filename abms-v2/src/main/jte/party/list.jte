@import java.util.Map
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.party.Party
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param PageView<Party> page
@param String q = null
@param Map<Long, Long> projectCounts
@template.layout.app(ctx = ctx, title = "협력사", content = @`
    @template.components.pageHeader(title = "협력사", subtitle = "프로젝트 고객사와 파트너 정보를 관리합니다.", actions = @`
        @if(ctx.can(PermissionCode.PARTY_WRITE))
            <a href="/parties/new" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}">${Icons.of("plus", "size-4")} 협력사 등록</a>
        @endif
    `)
    <div class="-mx-4 border-t border-stroke-neutral-subtle sm:-mx-6 lg:-mx-8">
        <form class="view-bar sm:px-x6 lg:px-x8" action="/parties" hx-get="/parties" hx-target="#party-results" hx-push-url="true"
              hx-trigger="input changed delay:300ms from:input[name=q], submit">
            <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-2.5 flex items-center text-fg-placeholder">${Icons.of("search", "size-3.5")}</span>
                <input type="search" name="q" value="${Ui.value(q)}" placeholder="협력사명 검색" class="input filter-input w-60 pl-x8" aria-label="협력사명 검색">
            </div>
            <span class="ml-auto t3-regular text-fg-neutral-subtle">총 ${page.totalElements()}곳</span>
        </form>
        <div id="party-results">@template.party.results(page = page, projectCounts = projectCounts)</div>
    </div>
`)
