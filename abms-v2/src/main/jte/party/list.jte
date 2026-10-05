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
@param Map<Long, kr.co.abacus.abms.party.PartyContact> primaryContacts
@template.layout.app(ctx = ctx, title = "협력사", fill = true, content = @`
<%-- 데스크톱: 페이지는 고정, 목록 본문만 스크롤하고 페이징은 아래에 고정 (fill 레이아웃). 태블릿·모바일은 문서 스크롤. --%>
<div class="flex flex-col lg:h-full">
    <div class="px-x4 pt-x5 sm:px-x6 lg:px-x8 lg:pt-x6">
    @template.components.pageHeader(title = "협력사", subtitle = "프로젝트 고객사와 파트너 정보를 관리합니다.", actions = @`
        @if(ctx.can(PermissionCode.PARTY_WRITE))
            <a href="/parties/new" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}">${Icons.of("plus", "size-4")} 협력사 등록</a>
        @endif
    `)
    </div>

    <div class="flex min-h-0 flex-1 flex-col border-t border-stroke-neutral-subtle">
        <%-- 필터 바와 열 제목: 데스크톱은 목록 위에 고정, 태블릿은 문서 스크롤 중 툴바 아래에 고정, 모바일은 함께 스크롤 --%>
        <div class="z-10 shrink-0 bg-bg-layer-default md:sticky md:top-(--sticky-top)">
        <form class="view-bar sm:px-x6 lg:px-x8" action="/parties" hx-get="/parties" hx-target="#party-results" hx-push-url="true"
              hx-trigger="input changed delay:300ms from:input[name=q], submit">
            <div class="relative">
                <span class="pointer-events-none absolute inset-y-0 left-2.5 flex items-center text-fg-placeholder">${Icons.of("search", "size-3.5")}</span>
                <input type="search" name="q" value="${Ui.value(q)}" placeholder="협력사명 검색" class="input filter-input w-60 pl-x8" aria-label="협력사명 검색">
            </div>
            <span class="ml-auto t3-regular text-fg-neutral-subtle">총 ${page.totalElements()}곳</span>
        </form>
        <div class="list-head sm:px-x6 lg:px-x8" aria-hidden="true">
            <span class="w-6 shrink-0"></span>
            <span class="min-w-0 flex-1">협력사</span>
            <span class="hidden w-28 @2xl:block">업종</span>
            <span class="hidden w-28 @2xl:block">대표자</span>
            <span class="hidden w-32 @3xl:block">대표 담당자</span>
            <span class="hidden w-56 @5xl:block">이메일</span>
            <span class="w-24 shrink-0 text-right">프로젝트</span>
        </div>
        </div>
        <div id="party-results" class="flex min-h-0 flex-1 flex-col">@template.party.results(page = page, projectCounts = projectCounts, primaryContacts = primaryContacts)</div>
    </div>
</div>
`)
