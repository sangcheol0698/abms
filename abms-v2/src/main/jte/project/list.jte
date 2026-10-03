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
@param ViewContext ctx
@param PageView<Project> page
@param DepartmentTree tree
@param Map<Long, String> partyNames
@param ProjectSearchForm search
@param List<SelectOption> departmentOptions
@param List<SelectOption> partyOptions
@template.layout.app(ctx = ctx, title = "프로젝트", content = @`
    @template.components.pageHeader(title = "프로젝트", subtitle = "계약·매출 계획·투입 인력을 관리합니다. 손익은 주관 부서에 귀속됩니다.", actions = @`
        @if(ctx.can(PermissionCode.PROJECT_EXPORT))
            <a href="/projects/export${search.exportQuery()}" hx-boost="false" class="btn btn-secondary">${Icons.of("download", "size-4")} CSV 내보내기</a>
        @endif
        @if(ctx.can(PermissionCode.PROJECT_WRITE))
            <a href="/projects/new" class="btn btn-primary">${Icons.of("plus", "size-4")} 프로젝트 등록</a>
        @endif
    `)
    <div class="card">
        <form class="grid grid-cols-1 gap-3 border-b border-slate-100 p-4 sm:grid-cols-2 lg:grid-cols-5" action="/projects"
              hx-get="/projects" hx-target="#project-results" hx-push-url="true"
              hx-trigger="input changed delay:300ms from:input[name=q], change from:select, submit">
            <div class="relative lg:col-span-2">
                <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-slate-400">${Icons.of("search", "size-4")}</span>
                <input type="search" name="q" value="${Ui.value(search.q())}" placeholder="프로젝트명 또는 코드" class="input pl-9" aria-label="검색어">
            </div>
            <select name="status" class="select" aria-label="상태">
                <option value="">전체 상태</option>
                @for(ProjectStatus s : ProjectStatus.values())
                    <option value="${s.name()}" selected="${s == search.status()}">${s.label()}</option>
                @endfor
            </select>
            <select name="partyId" class="select" aria-label="협력사">
                <option value="">전체 협력사</option>
                @for(SelectOption o : partyOptions)
                    <option value="${o.value()}" selected="${o.value().equals(Ui.value(search.partyId()))}">${o.label()}</option>
                @endfor
            </select>
            <select name="leadDepartmentId" class="select" aria-label="주관 부서">
                <option value="">전체 주관 부서</option>
                @for(SelectOption o : departmentOptions)
                    <option value="${o.value()}" selected="${o.value().equals(Ui.value(search.leadDepartmentId()))}">${o.label()}</option>
                @endfor
            </select>
        </form>
        <div id="project-results">@template.project.results(page = page, tree = tree, partyNames = partyNames)</div>
    </div>
`)
