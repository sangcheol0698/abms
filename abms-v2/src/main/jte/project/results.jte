@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.project.ProjectStatus
@param PageView<Project> page
@param DepartmentTree tree
@param Map<Long, String> partyNames
!{ProjectStatus[] order = {ProjectStatus.IN_PROGRESS, ProjectStatus.SCHEDULED, ProjectStatus.ON_HOLD, ProjectStatus.COMPLETED, ProjectStatus.CANCELLED};}
@if(page.isEmpty())
    @template.components.empty(message = "조회 가능한 프로젝트가 없습니다.", icon = "folder")
@else
    <%-- 상태별 그룹 리스트 (현재 페이지 기준) --%>
    @for(ProjectStatus status : order)
        !{List<Project> rows = page.content().stream().filter(p -> p.getStatus() == status).toList();}
        @if(!rows.isEmpty())
            <section aria-label="${status.label()}">
                <h3 class="list-group-header sm:px-x6 lg:px-x8">
                    @template.components.statusIcon(status = status)
                    <span class="text-fg-neutral">${status.label()}</span>
                    <span class="tabular-nums">${rows.size()}</span>
                </h3>
                @for(Project p : rows)
                    @template.components.projectRow(project = p, tree = tree, partyName = partyNames.getOrDefault(p.getPartyId(), "-"), padding = "sm:px-x6 lg:px-x8")
                @endfor
            </section>
        @endif
    @endfor
@endif
<div class="sm:px-x2 lg:px-x4">@template.components.pagination(page = page, target = "#project-results")</div>
