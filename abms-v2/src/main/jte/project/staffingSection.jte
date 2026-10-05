@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.project.ProjectSections.StaffRow
@import kr.co.abacus.abms.project.ProjectSections.StaffingSection
@import kr.co.abacus.abms.common.web.Seed
@param StaffingSection section
!{Long projectId = section.project().id();}
!{java.time.LocalDate today = java.time.LocalDate.now();}
<div class="card-header">
    <div>
        <h2 class="card-title">투입 인력 <span class="ml-1 text-xs font-normal text-fg-neutral-subtle">현재 ${section.activeCount()}명 / 전체 ${section.rows().size()}건</span></h2>
        <p class="mt-0.5 text-xs text-fg-neutral-subtle">비용 = 직원 월 원가 × 투입 M/M (월 총일수 대비 투입일수)</p>
    </div>
    @if(section.canWrite())
        <button type="button" class="${Seed.button("neutralWeak", "xsmall")}" hx-get="/projects/${projectId}/assignments/new" hx-target="#modal-body">${Icons.of("plus", "size-4")} 인력 투입</button>
    @endif
</div>
@if(section.rows().isEmpty())
    @template.components.empty(message = "투입된 인력이 없습니다.", icon = "users")
@else
    <%-- 넓은 본문: 표 / 좁은 본문(모바일): 카드 목록 --%>
    <div class="hidden overflow-x-auto @2xl:block">
        <table class="table">
            <thead><tr><th>직원</th><th>소속</th><th>역할</th><th>투입 기간</th><th>상태</th>@if(section.canWrite())<th></th>@endif</tr></thead>
            <tbody class="divide-y divide-stroke-neutral-subtle">
            @for(StaffRow row : section.rows())
                <tr>
                    <td>
                        @if(row.employee() != null)
                            <a href="/employees/${row.employee().id()}" class="flex items-center gap-2.5">
                                @template.components.avatar(photoUrl = row.employee().photoUrl(), name = row.employee().getName(), size = "size-7")
                                <span class="font-medium text-fg-neutral hover:text-fg-brand">${row.employee().getName()}</span>
                                <span class="text-xs text-fg-placeholder">${row.employee().getPosition().label()}</span>
                            </a>
                        @else
                            <span class="text-fg-placeholder">(삭제된 직원)</span>
                        @endif
                    </td>
                    <td>
                        ${row.departmentName()}
                        @if(row.supporting())<span class="${Seed.badge("neutral", "outline")} ml-x1">지원</span>@endif
                    </td>
                    <td>${row.assignment().getRole() == null ? "-" : row.assignment().getRole().label()}</td>
                    <td class="tabular-nums">${Ui.period(row.assignment().getPeriod().startDate(), row.assignment().getPeriod().endDate())}</td>
                    <td>@template.project.staffStatus(row = row, today = today)</td>
                    @if(section.canWrite())
                        <td class="text-right">@template.project.staffActions(row = row, projectId = projectId)</td>
                    @endif
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
    <ul class="divide-y divide-stroke-neutral-subtle @2xl:hidden">
        @for(StaffRow row : section.rows())
            <li class="px-x4 py-x3">
                <div class="flex items-center gap-x2">
                    @if(row.employee() != null)
                        <a href="/employees/${row.employee().id()}" class="flex min-w-0 items-center gap-x2">
                            @template.components.avatar(photoUrl = row.employee().photoUrl(), name = row.employee().getName(), size = "size-7")
                            <span class="truncate t4-bold">${row.employee().getName()}</span>
                            <span class="t3-regular text-fg-placeholder">${row.employee().getPosition().label()}</span>
                        </a>
                    @else
                        <span class="text-fg-placeholder">(삭제된 직원)</span>
                    @endif
                    <span class="ml-auto shrink-0">@template.project.staffStatus(row = row, today = today)</span>
                </div>
                <div class="mt-x1_5 flex flex-wrap items-center gap-x2 t3-regular text-fg-neutral-subtle">
                    <span>${row.departmentName()}</span>
                    @if(row.supporting())<span class="${Seed.badge("neutral", "outline")}">지원</span>@endif
                    <span>· ${row.assignment().getRole() == null ? "-" : row.assignment().getRole().label()}</span>
                    <span class="tabular-nums">· ${Ui.period(row.assignment().getPeriod().startDate(), row.assignment().getPeriod().endDate())}</span>
                    @if(section.canWrite())<div class="ml-auto">@template.project.staffActions(row = row, projectId = projectId)</div>@endif
                </div>
            </li>
        @endfor
    </ul>
@endif
