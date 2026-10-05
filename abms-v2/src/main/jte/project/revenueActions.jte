@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.project.ProjectRevenuePlan
@param ProjectRevenuePlan plan
@param Long projectId
<div class="flex justify-end gap-1" hx-target="#revenue-section">
    @if(plan.isIssued())
        <button class="${Seed.button("ghost", "xsmall")}" hx-post="/projects/${projectId}/revenues/${plan.id()}/cancel-issue" hx-confirm="${plan.getSequence()}차 매출 발행을 취소할까요?">발행 취소</button>
    @else
        <button class="${Seed.button("ghost", "xsmall")} text-fg-brand" hx-post="/projects/${projectId}/revenues/${plan.id()}/issue" hx-confirm="${plan.getSequence()}차 매출을 발행 처리할까요?">발행</button>
        <button class="${Seed.button("ghost", "xsmall")}" hx-get="/projects/${projectId}/revenues/${plan.id()}/edit" hx-target="#modal-body" aria-label="수정">${Icons.of("pencil", "size-4")}</button>
        <button class="${Seed.button("ghost", "xsmall")} text-fg-critical" hx-post="/projects/${projectId}/revenues/${plan.id()}/delete" hx-confirm="${plan.getSequence()}차 매출 계획을 삭제할까요?" aria-label="삭제">${Icons.of("trash", "size-4")}</button>
    @endif
</div>
