@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.Ui.SelectOption
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.project.ProjectSearchForm
@import kr.co.abacus.abms.project.ProjectStatus
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param PageView<Project> page
@param DepartmentTree tree
@param Map<Long, String> partyNames
@param ProjectSearchForm search
@param List<SelectOption> departmentOptions
@param List<SelectOption> partyOptions
@template.layout.app(ctx = ctx, title = "프로젝트", fill = true, content = @`
<%-- 데스크톱: 페이지는 고정, 목록 본문만 스크롤하고 페이징은 아래에 고정 (fill 레이아웃). 태블릿·모바일은 문서 스크롤. --%>
<div class="flex flex-col lg:h-full">
    <div class="px-x4 pt-x5 sm:px-x6 lg:px-x8 lg:pt-x6">
    @template.components.pageHeader(title = "프로젝트", subtitle = "계약·매출 계획·투입 인력을 관리합니다. 손익은 주관 부서에 귀속됩니다.", actions = @`
        @if(ctx.can(PermissionCode.PROJECT_EXPORT))
            <a href="/projects/export${search.exportQuery()}" hx-boost="false" class="${Seed.button("neutralOutline")}">${Icons.of("download", "size-4")} CSV 내보내기</a>
        @endif
        @if(ctx.can(PermissionCode.PROJECT_WRITE))
            <a href="/projects/new" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}">${Icons.of("plus", "size-4")} 프로젝트 등록</a>
        @endif
    `)
    </div>

    <div class="flex min-h-0 flex-1 flex-col border-t border-stroke-neutral-subtle">
        <%-- 필터 바와 열 제목: 데스크톱은 목록 위에 고정. 태블릿·모바일은 상태별 그룹 머리가 툴바 아래에 고정되므로 함께 스크롤 --%>
        <div class="z-10 shrink-0 bg-bg-layer-default">
        <form id="project-filter" class="view-bar sm:px-x6 lg:px-x8" action="/projects"
              hx-get="/projects" hx-target="#project-results" hx-push-url="true"
              hx-trigger="input changed delay:300ms from:(#project-filter input[name=q]), change from:(#project-filter select), change from:(#project-filter input[type=radio]), submit">
            <div class="flex flex-wrap items-center gap-x1" role="radiogroup" aria-label="상태">
                <label class="chip"><input type="radio" name="status" value="" checked="${search.status() == null}">전체</label>
                @for(ProjectStatus s : ProjectStatus.values())
                    <label class="chip"><input type="radio" name="status" value="${s.name()}" checked="${s == search.status()}">@template.components.statusIcon(status = s)${s.label()}</label>
                @endfor
            </div>
            <div class="ml-auto flex flex-wrap items-center gap-x2">
                <div class="relative">
                    <span class="pointer-events-none absolute inset-y-0 left-2.5 flex items-center text-fg-placeholder">${Icons.of("search", "size-3.5")}</span>
                    <input type="search" name="q" value="${Ui.value(search.q())}" placeholder="프로젝트명 또는 코드" class="input filter-input w-52 pl-x8" aria-label="검색어">
                </div>
                <select name="partyId" class="select filter-select" aria-label="협력사">
                    <option value="">전체 협력사</option>
                    @for(SelectOption o : partyOptions)
                        <option value="${o.value()}" selected="${o.value().equals(Ui.value(search.partyId()))}">${o.label()}</option>
                    @endfor
                </select>
                <select name="leadDepartmentId" class="select filter-select" aria-label="주관 부서">
                    <option value="">전체 주관 부서</option>
                    @for(SelectOption o : departmentOptions)
                        <option value="${o.value()}" selected="${o.value().equals(Ui.value(search.leadDepartmentId()))}">${o.label()}</option>
                    @endfor
                </select>
            </div>
        </form>
        <div class="list-head sm:px-x6 lg:px-x8" aria-hidden="true">
            <span class="hidden w-24 shrink-0 @xl:block">코드</span>
            <span class="w-3.5 shrink-0"></span>
            <span class="min-w-0 flex-1">프로젝트</span>
            <span class="hidden w-36 @2xl:block">협력사</span>
            <span class="hidden w-40 @3xl:block">주관 부서</span>
            <span class="hidden w-52 @5xl:block">기간</span>
            <span class="w-28 shrink-0 text-right">계약금액</span>
        </div>
        </div>
        <div id="project-results" class="flex min-h-0 flex-1 flex-col">@template.project.results(page = page, tree = tree, partyNames = partyNames)</div>
    </div>
</div>
`)
