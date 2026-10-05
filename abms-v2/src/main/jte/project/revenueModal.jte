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
    @template.components.modal(title = editing ? "매출 계획 수정" : "매출 계획 추가", context = project.getName(), hint = editing ? "저장" : "추가",
        description = "계약금액 " + project.getContractAmount().formatted() + "원 · 계획 합계는 계약금액을 넘을 수 없습니다.", body = @`
        @template.components.globalError(errors = errors)
        <div class="prop-form">
            @template.components.propRow(label = "차수", forId = "sequence", required = true, error = errors.get("sequence"), control = @`
                <input id="sequence" name="sequence" type="number" min="1" class="input w-24" value="${Ui.value(form.sequence())}" required>
            `)
            @template.components.propRow(label = "유형", forId = "type", error = errors.get("type"), control = @`
                <select id="type" name="type" class="select">
                    @for(var o : Ui.options(RevenueType.values()))
                        <option value="${o.value()}" selected="${o.value().equals(Ui.value(form.type()))}">${o.label()}</option>
                    @endfor
                </select>
            `)
            @template.components.propRow(label = "청구일", forId = "revenueDate", required = true, error = errors.get("revenueDate"), control = @`
                <input id="revenueDate" name="revenueDate" type="date" class="input" value="${Ui.value(form.revenueDate())}" required>
            `)
            @template.components.propRow(label = "금액 (원)", forId = "amount", required = true, error = errors.get("amount"), control = @`
                <input id="amount" name="amount" type="number" min="1" class="input tabular-nums" data-money="매출 금액" value="${Ui.value(form.amount())}" required>
            `)
            @template.components.propRow(label = "메모", forId = "memo", error = errors.get("memo"), control = @`
                <input id="memo" name="memo" class="input" value="${Ui.value(form.memo())}" maxlength="255" placeholder="선택 입력">
            `)
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "추가"}</button>
    `)
</form>
