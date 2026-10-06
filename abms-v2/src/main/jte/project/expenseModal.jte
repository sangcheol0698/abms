@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.project.ExpenseCategory
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.project.ProjectExpense
@import kr.co.abacus.abms.project.ProjectExpenseController.ExpenseForm
@import kr.co.abacus.abms.common.web.Seed
@param Project project
@param ProjectExpense expense = null
@param ExpenseForm form
@param FormErrors errors
!{boolean editing = expense != null;}
<form hx-post="/projects/${project.id()}/expenses${editing ? "/" + expense.id() : ""}" hx-target="#modal-body">
    @template.components.modal(title = editing ? "직접비 수정" : "직접비 추가", context = project.getName(), hint = editing ? "저장" : "추가",
        description = "귀속일이 속한 월의 프로젝트 비용으로 집계됩니다. 금액은 부가세를 뺀 공급가액으로 입력하세요.", body = @`
        @template.components.globalError(errors = errors)
        <div class="prop-form">
            @template.components.propRow(label = "귀속일", forId = "expenseDate", required = true, error = errors.get("expenseDate"), control = @`
                <input id="expenseDate" name="expenseDate" type="date" class="input" value="${Ui.value(form.expenseDate())}" required>
            `)
            @template.components.propRow(label = "분류", forId = "category", error = errors.get("category"), control = @`
                <select id="category" name="category" class="select">
                    @for(var o : Ui.options(ExpenseCategory.values()))
                        <option value="${o.value()}" selected="${o.value().equals(Ui.value(form.category()))}">${o.label()}</option>
                    @endfor
                </select>
            `)
            @template.components.propRow(label = "내용", forId = "description", required = true, error = errors.get("description"), control = @`
                <input id="description" name="description" class="input" value="${Ui.value(form.description())}" maxlength="100" required placeholder="예: 디자인 외주 1차분">
            `)
            @template.components.propRow(label = "금액 (원)", forId = "amount", required = true, error = errors.get("amount"), control = @`
                <input id="amount" name="amount" type="number" min="1" class="input tabular-nums" data-money="직접비 금액" value="${Ui.value(form.amount())}" required>
            `)
            @template.components.propRow(label = "메모", forId = "memo", error = errors.get("memo"), control = @`
                <input id="memo" name="memo" class="input" value="${Ui.value(form.memo())}" maxlength="255" placeholder="선택 입력 (거래처, 계약 번호 등)">
            `)
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "추가"}</button>
    `)
</form>
