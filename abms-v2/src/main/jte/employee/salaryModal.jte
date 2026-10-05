@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.common.web.Seed
@param Employee employee
@param FormErrors errors
<form hx-post="/employees/${employee.id()}/salary" hx-target="#modal-body">
    @template.components.modal(title = "연봉 변경", context = employee.getName(), hint = "등록",
        description = "새 연봉은 적용일부터 반영되고 이전 연봉은 전날로 종료됩니다.", body = @`
        @template.components.globalError(errors = errors)
        <div class="prop-form">
            @template.components.propRow(label = "연봉 (원)", forId = "annualSalary", required = true, error = errors.get("annualSalary"), control = @`
                <input id="annualSalary" name="annualSalary" type="number" min="1" step="1" class="input tabular-nums" required autofocus placeholder="60000000">
            `)
            @template.components.propRow(label = "적용일", forId = "startDate", required = true, error = errors.get("startDate"), control = @`
                <input id="startDate" name="startDate" type="date" class="input" required value="${java.time.LocalDate.now().withDayOfMonth(1).toString()}">
            `)
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">등록</button>
    `)
</form>
