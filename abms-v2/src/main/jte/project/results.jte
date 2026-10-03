@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
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
                    <a href="/projects/${p.id()}" class="list-row group sm:px-x6 lg:px-x8">
                        <span class="hidden w-24 shrink-0 t3-regular text-fg-neutral-subtle tabular-nums sm:block">${p.getCode()}</span>
                        @template.components.statusIcon(status = p.getStatus())
                        <span class="min-w-0 flex-1 truncate t4-medium">${p.getName()}</span>
                        <span class="list-cell-muted hidden w-36 md:block">${partyNames.getOrDefault(p.getPartyId(), "-")}</span>
                        <span class="list-cell-muted hidden w-40 lg:block">${tree.nameOf(p.getLeadDepartmentId())}</span>
                        <span class="list-cell-muted hidden w-52 tabular-nums xl:block">${Ui.period(p.getPeriod().startDate(), p.getPeriod().endDate())}</span>
                        <span class="w-28 shrink-0 text-right tabular-nums">${Ui.won(p.getContractAmount())}</span>
                    </a>
                @endfor
            </section>
        @endif
    @endfor
@endif
<div class="sm:px-x2 lg:px-x4">@template.components.pagination(page = page, target = "#project-results")</div>
