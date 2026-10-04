@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.project.ProjectRevenueController.RevenueForm
@import kr.co.abacus.abms.project.ProjectRevenuePlan
@import kr.co.abacus.abms.project.RevenueType
@import kr.co.abacus.abms.common.web.Seed
@param Project project
@param ProjectRevenuePlan plan = null
@param RevenueForm form
@param FormErrors errors
!{boolean editing = plan != null;}
<form hx-post="/projects/${project.id()}/revenues${editing ? "/" + plan.id() : ""}" hx-target="#modal-body">
    @template.components.modal(title = editing ? "매출 계획 수정" : "매출 계획 추가", description = project.getName() + " · 계약금액 " + project.getContractAmount().formatted() + "원", body = @`
        @template.components.globalError(errors = errors)
        <div class="grid grid-cols-2 gap-4">
            <div>
                <label for="sequence" class="label">차수</label>
                <input id="sequence" name="sequence" type="number" min="1" class="input" value="${Ui.value(form.sequence())}" required>
            </div>
            @template.components.select(name = "type", label = "유형", options = Ui.options(RevenueType.values()), value = Ui.value(form.type()), errors = errors)
            <div>
                <label for="revenueDate" class="label">청구일</label>
                <input id="revenueDate" name="revenueDate" type="date" class="input" value="${Ui.value(form.revenueDate())}" required>
            </div>
            <div>
                <label for="amount" class="label">금액 (원)</label>
                <input id="amount" name="amount" type="number" min="1" class="input" value="${Ui.value(form.amount())}" required>
            </div>
            <div class="col-span-2">
                <label for="memo" class="label">메모</label>
                <input id="memo" name="memo" class="input" value="${Ui.value(form.memo())}" maxlength="255">
            </div>
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("neutralWeak")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "추가"}</button>
    `)
</form>
