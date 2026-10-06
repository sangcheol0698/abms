@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.project.ProjectExpense
@param ProjectExpense expense
@param Long projectId
<div class="flex justify-end gap-1" hx-target="#expense-section">
    <button class="${Seed.button("ghost", "xsmall")}" hx-get="/projects/${projectId}/expenses/${expense.id()}/edit" hx-target="#modal-body" aria-label="수정">${Icons.of("pencil", "size-4")}</button>
    <button class="${Seed.button("ghost", "xsmall")} text-fg-critical" hx-post="/projects/${projectId}/expenses/${expense.id()}/delete" hx-confirm="직접비 '${expense.getDescription()}'을(를) 삭제할까요?" aria-label="삭제">${Icons.of("trash", "size-4")}</button>
</div>
