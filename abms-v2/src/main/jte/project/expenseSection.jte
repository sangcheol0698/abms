@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.project.ProjectExpense
@import kr.co.abacus.abms.project.ProjectSections.ExpenseSection
@import kr.co.abacus.abms.common.web.Seed
@param ExpenseSection section
!{Long projectId = section.project().id();}
<div class="card-header">
    <div>
        <h2 class="card-title">직접비 <span class="ml-1 text-xs font-normal text-fg-neutral-subtle">${section.expenses().size()}건</span></h2>
        <p class="mt-0.5 text-xs text-fg-neutral-subtle">외주 용역비·장비·라이선스 등 인건비 외 비용 · 귀속일이 속한 월의 비용으로 집계됩니다. (공급가액 기준)</p>
    </div>
    @if(section.canWrite())
        <button type="button" class="${Seed.button("neutralWeak", "xsmall")}" hx-get="/projects/${projectId}/expenses/new" hx-target="#modal-body">${Icons.of("plus", "size-4")} 직접비</button>
    @endif
</div>
@if(section.expenses().isEmpty())
    @template.components.empty(message = "등록된 직접비가 없습니다.", icon = "calculator")
@else
    <div class="grid grid-cols-1 gap-4 border-b border-stroke-neutral-subtle px-5 py-4 @xl:grid-cols-3">
        <div>
            <p class="stat-label">직접비 합계</p>
            <p class="mt-1 text-sm font-semibold tabular-nums">${Ui.won(section.total())}원</p>
            <p class="mt-0.5 text-xs text-fg-neutral-subtle">계약금액의 ${section.contractPercent()}%</p>
        </div>
        <div class="@xl:col-span-2">
            <p class="stat-label">분류별</p>
            <ul class="mt-1 flex flex-wrap gap-x-x4 gap-y-x1 text-sm">
                @for(var entry : section.byCategory())
                    <li><span class="text-fg-neutral-subtle">${entry.getKey().label()}</span> <span class="font-semibold tabular-nums">${Ui.won(entry.getValue())}</span></li>
                @endfor
            </ul>
        </div>
    </div>
    <%-- 넓은 본문: 표 / 좁은 본문(모바일): 카드 목록 --%>
    <div class="hidden overflow-x-auto @2xl:block">
        <table class="table">
            <thead><tr><th>귀속일</th><th>분류</th><th>내용</th><th class="num">금액</th><th>메모</th>@if(section.canWrite())<th></th>@endif</tr></thead>
            <tbody class="divide-y divide-stroke-neutral-subtle">
            @for(ProjectExpense expense : section.expenses())
                <tr>
                    <td class="tabular-nums">${Ui.date(expense.getExpenseDate())}</td>
                    <td>${expense.getCategory().label()}</td>
                    <td class="font-medium">${expense.getDescription()}</td>
                    <td class="num font-medium">${Ui.won(expense.getAmount())}</td>
                    <td class="max-w-56 truncate text-xs text-fg-neutral-subtle">${Ui.value(expense.getMemo())}</td>
                    @if(section.canWrite())
                        <td class="text-right">@template.project.expenseActions(expense = expense, projectId = projectId)</td>
                    @endif
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
    <ul class="divide-y divide-stroke-neutral-subtle @2xl:hidden">
        @for(ProjectExpense expense : section.expenses())
            <li class="px-x4 py-x3">
                <div class="flex items-center gap-x2">
                    <span class="truncate t4-bold">${expense.getDescription()}</span>
                    <span class="ml-auto t4-bold tabular-nums">${Ui.won(expense.getAmount())}</span>
                </div>
                <div class="mt-x1_5 flex flex-wrap items-center gap-x2 t3-regular text-fg-neutral-subtle">
                    <span>${expense.getCategory().label()}</span>
                    <span class="tabular-nums">· ${Ui.date(expense.getExpenseDate())}</span>
                    @if(section.canWrite())<div class="ml-auto">@template.project.expenseActions(expense = expense, projectId = projectId)</div>@endif
                </div>
                @if(expense.getMemo() != null)<p class="mt-x1 t3-regular text-fg-neutral-subtle">${expense.getMemo()}</p>@endif
            </li>
        @endfor
    </ul>
@endif
