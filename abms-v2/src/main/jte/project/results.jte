@import java.util.Map
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.project.Project
@param PageView<Project> page
@param DepartmentTree tree
@param Map<Long, String> partyNames
@if(page.isEmpty())
    @template.components.empty(message = "조회 가능한 프로젝트가 없습니다.", icon = "folder")
@else
    <div class="overflow-x-auto">
        <table class="table">
            <thead><tr><th>프로젝트</th><th>협력사</th><th>주관 부서</th><th>상태</th><th>기간</th><th class="num">계약금액</th></tr></thead>
            <tbody class="divide-y divide-stroke-neutral-subtle">
            @for(Project p : page.content())
                <tr>
                    <td><a href="/projects/${p.id()}" class="link">${p.getName()}</a><p class="text-xs text-fg-placeholder">${p.getCode()}</p></td>
                    <td>${partyNames.getOrDefault(p.getPartyId(), "-")}</td>
                    <td>${tree.nameOf(p.getLeadDepartmentId())}</td>
                    <td><span class="${Ui.badge(p.getStatus())}">${p.getStatus().label()}</span></td>
                    <td class="tabular-nums">${Ui.period(p.getPeriod().startDate(), p.getPeriod().endDate())}</td>
                    <td class="num">${Ui.won(p.getContractAmount())}</td>
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
@endif
@template.components.pagination(page = page, target = "#project-results")
