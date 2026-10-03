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
@template.layout.app(ctx = ctx, title = "부서", content = @`
    @template.components.pageHeader(title = "부서", subtitle = "조직도와 부서별 구성원·주관 프로젝트를 확인합니다.", actions = @`
        @if(ctx.can(PermissionCode.DEPARTMENT_WRITE))
            <button type="button" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}" hx-get="/departments/new${selectedId == null ? "" : "?parentId=" + selectedId}" hx-target="#modal-body">${Icons.of("plus", "size-4")} 부서 추가</button>
        @endif
    `)
    <div class="grid grid-cols-1 gap-4 lg:grid-cols-[18rem_1fr]">
        <nav class="card h-fit p-2 lg:sticky lg:top-20" aria-label="조직도">
            <ul class="space-y-0.5">
                @for(DepartmentTree.Node node : tree.flatten())
                    <li>
                        <a href="/departments?selected=${node.department().id()}"
                           hx-get="/departments/${node.department().id()}" hx-target="#department-detail" hx-push-url="/departments?selected=${node.department().id()}"
                           onclick="document.querySelectorAll('[data-dept-link]').forEach(e => e.classList.remove('bg-bg-transparent-selected','t4-bold','text-fg-neutral')); this.classList.add('bg-bg-transparent-selected','t4-bold','text-fg-neutral')"
                           data-dept-link
                           class="flex items-center gap-1.5 rounded-r2 py-x1_5 pr-x2 t4-regular hover:bg-bg-transparent-pressed ${node.department().id().equals(selectedId) ? "bg-bg-transparent-selected t4-bold text-fg-neutral" : "text-fg-neutral-muted"}"
                           style="padding-left: ${8 + node.depth() * 14}px">
                            @if(tree.children(node.department().id()).isEmpty())
                                <span class="size-1.5 rounded-full bg-palette-gray-400"></span>
                            @else
                                ${Icons.of("chevron-down", "size-3 text-fg-placeholder")}
                            @endif
                            <span class="truncate ${node.depth() == 0 ? "font-semibold" : ""}">${node.department().getName()}</span>
                        </a>
                    </li>
                @endfor
            </ul>
        </nav>
        <div id="department-detail">
            @if(department != null)
                @template.department.detail(ctx = ctx, department = department, tree = tree, members = members,
                    subtreeMemberCount = subtreeMemberCount, leader = leader, projects = projects, trend = trend, canWrite = canWrite)
            @else
                <div class="card">@template.components.empty(message = "부서가 없습니다.", icon = "building")</div>
            @endif
        </div>
    </div>
`)
