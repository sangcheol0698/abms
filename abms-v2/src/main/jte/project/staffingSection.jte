@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.project.ProjectSections.StaffRow
@import kr.co.abacus.abms.project.ProjectSections.StaffingSection
@param StaffingSection section
!{Long projectId = section.project().id();}
!{java.time.LocalDate today = java.time.LocalDate.now();}
<div class="card-header">
    <div>
        <h2 class="card-title">투입 인력 <span class="ml-1 text-xs font-normal text-slate-500">현재 ${section.activeCount()}명 / 전체 ${section.rows().size()}건</span></h2>
        <p class="mt-0.5 text-xs text-slate-500">비용 = 직원 월 원가 × 투입 M/M (월 총일수 대비 투입일수)</p>
    </div>
    @if(section.canWrite())
        <button type="button" class="btn btn-secondary btn-sm" hx-get="/projects/${projectId}/assignments/new" hx-target="#modal-body">${Icons.of("plus", "size-4")} 인력 투입</button>
    @endif
</div>
@if(section.rows().isEmpty())
    @template.components.empty(message = "투입된 인력이 없습니다.", icon = "users")
@else
    <div class="overflow-x-auto">
        <table class="table">
            <thead><tr><th>직원</th><th>소속</th><th>역할</th><th>투입 기간</th><th>상태</th>@if(section.canWrite())<th></th>@endif</tr></thead>
            <tbody class="divide-y divide-slate-100">
            @for(StaffRow row : section.rows())
                <tr>
                    <td>
                        @if(row.employee() != null)
                            <a href="/employees/${row.employee().id()}" class="flex items-center gap-2.5">
                                @template.components.avatar(avatar = row.employee().getAvatar(), initial = row.employee().initial(), size = "size-7 text-xs")
                                <span class="font-medium text-slate-900 hover:text-brand-600">${row.employee().getName()}</span>
                                <span class="text-xs text-slate-400">${row.employee().getPosition().label()}</span>
                            </a>
                        @else
                            <span class="text-slate-400">(삭제된 직원)</span>
                        @endif
                    </td>
                    <td>
                        ${row.departmentName()}
                        @if(row.supporting())<span class="badge badge-purple ml-1">지원</span>@endif
                    </td>
                    <td>${row.assignment().getRole() == null ? "-" : row.assignment().getRole().label()}</td>
                    <td class="tabular-nums">${Ui.period(row.assignment().getPeriod().startDate(), row.assignment().getPeriod().endDate())}</td>
                    <td>
                        @if(row.assignment().isActiveOn(today))
                            <span class="badge badge-blue">투입 중</span>
                        @elseif(row.assignment().getPeriod().startDate().isAfter(today))
                            <span class="badge badge-purple">예정</span>
                        @else
                            <span class="badge badge-gray">종료</span>
                        @endif
                    </td>
                    @if(section.canWrite())
                        <td class="text-right">
                            <div class="flex justify-end gap-1">
                                <button class="btn btn-ghost btn-sm" hx-get="/projects/${projectId}/assignments/${row.assignment().id()}/edit" hx-target="#modal-body" aria-label="수정">${Icons.of("pencil", "size-4")}</button>
                                <button class="btn btn-danger-ghost btn-sm" hx-post="/projects/${projectId}/assignments/${row.assignment().id()}/delete" hx-target="#staffing-section" hx-confirm="투입 정보를 삭제할까요?" aria-label="삭제">${Icons.of("trash", "size-4")}</button>
                            </div>
                        </td>
                    @endif
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
@endif
