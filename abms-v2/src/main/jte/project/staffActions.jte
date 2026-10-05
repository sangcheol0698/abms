@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.project.ProjectSections.StaffRow
@param StaffRow row
@param Long projectId
<div class="flex justify-end gap-1">
    <button class="${Seed.button("ghost", "xsmall")}" hx-get="/projects/${projectId}/assignments/${row.assignment().id()}/edit" hx-target="#modal-body" aria-label="수정">${Icons.of("pencil", "size-4")}</button>
    <button class="${Seed.button("ghost", "xsmall")} text-fg-critical" hx-post="/projects/${projectId}/assignments/${row.assignment().id()}/delete" hx-target="#staffing-section" hx-confirm="투입 정보를 삭제할까요?" aria-label="삭제">${Icons.of("trash", "size-4")}</button>
</div>
