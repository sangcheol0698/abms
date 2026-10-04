@import java.util.List
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.summary.ProfitQueryService.MonthPoint
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param DepartmentTree tree
@param Long selectedId = null
@param Department department = null
@param List<Employee> members = null
@param int subtreeMemberCount = 0
@param Employee leader = null
@param List<Project> projects = null
@param List<MonthPoint> trend = null
@param boolean canWrite = false
@param kr.co.abacus.abms.department.DepartmentInsight insight = null
@param String trendChart = null
@param kr.co.abacus.abms.site.Site site = null
@template.layout.app(ctx = ctx, title = "부서", fill = true, aside = department == null ? null : @`
    @template.department.properties(department = department, tree = tree, members = members, subtreeMemberCount = subtreeMemberCount, leader = leader, site = site)
`, content = @`
<div class="flex flex-col lg:h-full">
    <div class="px-x4 pt-x5 sm:px-x6 lg:px-x8 lg:pt-x6">
    @template.components.pageHeader(title = "부서", subtitle = "조직도와 부서별 구성원·주관 프로젝트를 확인합니다.", actions = @`
        @if(ctx.can(PermissionCode.DEPARTMENT_WRITE))
            <button type="button" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}" hx-get="/departments/new${selectedId == null ? "" : "?parentId=" + selectedId}" hx-target="#modal-body">${Icons.of("plus", "size-4")} 부서 추가</button>
        @endif
    `)
    </div>
    <div class="grid min-h-0 flex-1 border-t border-stroke-neutral-subtle @3xl:grid-cols-[260px_minmax(0,1fr)]">
        <nav class="scroll-pane max-h-64 border-b border-stroke-neutral-subtle p-x2 @3xl:max-h-none @3xl:border-r @3xl:border-b-0" aria-label="조직도">
            <div>
                <ul class="space-y-px">
                    @for(DepartmentTree.Node node : tree.flatten())
                        <li>
                            <a href="/departments?selected=${node.department().id()}"
                               data-dept-link class="tree-item" aria-current="${node.department().id().equals(selectedId) ? "page" : null}"
                               style="padding-left: ${8 + node.depth() * 14}px">
                                @if(tree.children(node.department().id()).isEmpty())
                                    <span class="flex size-3 shrink-0 items-center justify-center"><span class="size-1 rounded-full bg-palette-gray-500"></span></span>
                                @else
                                    ${Icons.of("chevron-down", "size-3 shrink-0 text-fg-neutral-subtle")}
                                @endif
                                <span class="truncate">${node.department().getName()}</span>
                            </a>
                        </li>
                    @endfor
                </ul>
            </div>
        </nav>
        <div id="department-detail" class="scroll-pane @container min-w-0 px-x4 py-x5 sm:px-x6 lg:px-x8">
            @if(department != null)
                @template.department.detail(ctx = ctx, department = department, tree = tree, members = members,
                    subtreeMemberCount = subtreeMemberCount, leader = leader, projects = projects, trend = trend, canWrite = canWrite,
                    insight = insight, trendChart = trendChart)
            @else
                @template.components.empty(message = "부서가 없습니다.", icon = "building")
            @endif
        </div>
    </div>
</div>
`)
