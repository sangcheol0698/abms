@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.project.Project
@param Project project
@param DepartmentTree tree
@param String partyName = null
@param String padding = ""
<%-- Linear 풍 프로젝트 리스트 행 (프로젝트 목록·부서·협력사 상세 공용) --%>
<a href="/projects/${project.id()}" class="list-row ${padding}">
    <span class="hidden w-24 shrink-0 t3-regular text-fg-neutral-subtle tabular-nums @xl:block">${project.getCode()}</span>
    @template.components.statusIcon(status = project.getStatus())
    <span class="min-w-0 flex-1 truncate t4-medium">${project.getName()}</span>
    @if(partyName != null)
        <span class="list-cell-muted hidden w-36 @2xl:block">${partyName}</span>
    @endif
    <span class="list-cell-muted hidden w-40 @3xl:block">${tree.nameOf(project.getLeadDepartmentId())}</span>
    <span class="list-cell-muted hidden w-52 tabular-nums @5xl:block">${Ui.period(project.getPeriod().startDate(), project.getPeriod().endDate())}</span>
    <span class="w-28 shrink-0 text-right tabular-nums">${Ui.won(project.getContractAmount())}</span>
</a>
