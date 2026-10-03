@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.project.ProjectRevenuePlan
@import kr.co.abacus.abms.project.ProjectSections.RevenueSection
@param RevenueSection section
!{Long projectId = section.project().id();}
<div class="card-header">
    <div>
        <h2 class="card-title">매출(청구) 계획</h2>
        <p class="mt-0.5 text-xs text-slate-500">세금계산서가 <b>발행</b>된 계획만 청구일이 속한 월의 매출로 집계됩니다.</p>
    </div>
    @if(section.canWrite())
        <button type="button" class="btn btn-secondary btn-sm" hx-get="/projects/${projectId}/revenues/new" hx-target="#modal-body">${Icons.of("plus", "size-4")} 매출 계획</button>
    @endif
</div>
<div class="grid grid-cols-1 gap-4 border-b border-slate-100 px-5 py-4 sm:grid-cols-3">
    <div>
        <p class="stat-label">발행 / 계약금액</p>
        <p class="mt-1 text-sm font-semibold tabular-nums">${Ui.won(section.issued())} / ${Ui.won(section.project().getContractAmount())}원</p>
        <div class="mt-2 h-1.5 overflow-hidden rounded-full bg-slate-100"><div class="h-full rounded-full bg-brand-500" style="width: ${Math.min(100, section.issuedPercent())}%"></div></div>
    </div>
    <div><p class="stat-label">계획 합계</p><p class="mt-1 text-sm font-semibold tabular-nums">${Ui.won(section.planned())}원</p></div>
    <div><p class="stat-label">미계획 잔액</p><p class="mt-1 text-sm font-semibold tabular-nums ${section.unplanned().isNegative() ? "negative" : ""}">${Ui.won(section.unplanned())}원</p></div>
</div>
@if(section.plans().isEmpty())
    @template.components.empty(message = "등록된 매출 계획이 없습니다.", icon = "money")
@else
    <div class="overflow-x-auto">
        <table class="table">
            <thead><tr><th>차수</th><th>유형</th><th>청구일</th><th class="num">금액</th><th>발행</th><th>메모</th>@if(section.canWrite())<th></th>@endif</tr></thead>
            <tbody class="divide-y divide-slate-100">
            @for(ProjectRevenuePlan plan : section.plans())
                <tr>
                    <td class="font-medium">${plan.getSequence()}차</td>
                    <td>${plan.getType().label()}</td>
                    <td class="tabular-nums">${Ui.date(plan.getRevenueDate())}</td>
                    <td class="num font-medium">${Ui.won(plan.getAmount())}</td>
                    <td>
                        @if(plan.isIssued())
                            <span class="badge badge-green">${Icons.of("check", "size-3")} 발행</span>
                        @elseif(plan.getRevenueDate().isBefore(java.time.LocalDate.now()))
                            <span class="badge badge-red">미발행 (기한 경과)</span>
                        @else
                            <span class="badge badge-gray">미발행</span>
                        @endif
                    </td>
                    <td class="max-w-56 truncate text-xs text-slate-500">${Ui.value(plan.getMemo())}</td>
                    @if(section.canWrite())
                        <td class="text-right">
                            <div class="flex justify-end gap-1" hx-target="#revenue-section">
                                @if(plan.isIssued())
                                    <button class="btn btn-ghost btn-sm" hx-post="/projects/${projectId}/revenues/${plan.id()}/cancel-issue" hx-confirm="${plan.getSequence()}차 매출 발행을 취소할까요?">발행 취소</button>
                                @else
                                    <button class="btn btn-ghost btn-sm text-brand-700" hx-post="/projects/${projectId}/revenues/${plan.id()}/issue" hx-confirm="${plan.getSequence()}차 매출을 발행 처리할까요?">발행</button>
                                    <button class="btn btn-ghost btn-sm" hx-get="/projects/${projectId}/revenues/${plan.id()}/edit" hx-target="#modal-body" aria-label="수정">${Icons.of("pencil", "size-4")}</button>
                                    <button class="btn btn-danger-ghost btn-sm" hx-post="/projects/${projectId}/revenues/${plan.id()}/delete" hx-confirm="${plan.getSequence()}차 매출 계획을 삭제할까요?" aria-label="삭제">${Icons.of("trash", "size-4")}</button>
                                @endif
                            </div>
                        </td>
                    @endif
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
@endif
