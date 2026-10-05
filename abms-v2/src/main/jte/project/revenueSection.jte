@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.project.ProjectRevenuePlan
@import kr.co.abacus.abms.project.ProjectSections.RevenueSection
@import kr.co.abacus.abms.common.web.Seed
@param RevenueSection section
!{Long projectId = section.project().id();}
<div class="card-header">
    <div>
        <h2 class="card-title">매출(청구) 계획</h2>
        <p class="mt-0.5 text-xs text-fg-neutral-subtle">세금계산서가 <b>발행</b>된 계획만 청구일이 속한 월의 매출로 집계됩니다.</p>
    </div>
    @if(section.canWrite())
        <button type="button" class="${Seed.button("neutralWeak", "xsmall")}" hx-get="/projects/${projectId}/revenues/new" hx-target="#modal-body">${Icons.of("plus", "size-4")} 매출 계획</button>
    @endif
</div>
<div class="grid grid-cols-1 gap-4 border-b border-stroke-neutral-subtle px-5 py-4 @xl:grid-cols-3">
    <div>
        <p class="stat-label">발행 / 계약금액</p>
        <p class="mt-1 text-sm font-semibold tabular-nums">${Ui.won(section.issued())} / ${Ui.won(section.project().getContractAmount())}원</p>
        <div class="mt-2 h-1.5 overflow-hidden rounded-full bg-bg-neutral-weak"><div class="h-full rounded-full bg-bg-brand-solid" style="width: ${Math.min(100, section.issuedPercent())}%"></div></div>
    </div>
    <div><p class="stat-label">계획 합계</p><p class="mt-1 text-sm font-semibold tabular-nums">${Ui.won(section.planned())}원</p></div>
    <div><p class="stat-label">미계획 잔액</p><p class="mt-1 text-sm font-semibold tabular-nums ${section.unplanned().isNegative() ? "negative" : ""}">${Ui.won(section.unplanned())}원</p></div>
</div>
@if(section.plans().isEmpty())
    @template.components.empty(message = "등록된 매출 계획이 없습니다.", icon = "money")
@else
    <%-- 넓은 본문: 표 / 좁은 본문(모바일): 카드 목록 (발행 상태·버튼이 가로 스크롤 밖으로 밀리지 않게) --%>
    <div class="hidden overflow-x-auto @2xl:block">
        <table class="table">
            <thead><tr><th>차수</th><th>유형</th><th>청구일</th><th class="num">금액</th><th>발행</th><th>메모</th>@if(section.canWrite())<th></th>@endif</tr></thead>
            <tbody class="divide-y divide-stroke-neutral-subtle">
            @for(ProjectRevenuePlan plan : section.plans())
                <tr>
                    <td class="font-medium">${plan.getSequence()}차</td>
                    <td>${plan.getType().label()}</td>
                    <td class="tabular-nums">${Ui.date(plan.getRevenueDate())}</td>
                    <td class="num font-medium">${Ui.won(plan.getAmount())}</td>
                    <td>@template.project.revenueStatus(plan = plan)</td>
                    <td class="max-w-56 truncate text-xs text-fg-neutral-subtle">${Ui.value(plan.getMemo())}</td>
                    @if(section.canWrite())
                        <td class="text-right">@template.project.revenueActions(plan = plan, projectId = projectId)</td>
                    @endif
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
    <ul class="divide-y divide-stroke-neutral-subtle @2xl:hidden">
        @for(ProjectRevenuePlan plan : section.plans())
            <li class="px-x4 py-x3">
                <div class="flex items-center gap-x2">
                    <span class="t4-bold">${plan.getSequence()}차</span>
                    <span class="t3-regular text-fg-neutral-subtle">${plan.getType().label()}</span>
                    <span class="ml-auto t4-bold tabular-nums">${Ui.won(plan.getAmount())}</span>
                </div>
                <div class="mt-x1_5 flex flex-wrap items-center gap-x2">
                    <span class="t3-regular text-fg-neutral-subtle tabular-nums">청구일 ${Ui.date(plan.getRevenueDate())}</span>
                    @template.project.revenueStatus(plan = plan)
                    @if(section.canWrite())<div class="ml-auto">@template.project.revenueActions(plan = plan, projectId = projectId)</div>@endif
                </div>
                @if(plan.getMemo() != null)<p class="mt-x1 t3-regular text-fg-neutral-subtle">${plan.getMemo()}</p>@endif
            </li>
        @endfor
    </ul>
@endif
