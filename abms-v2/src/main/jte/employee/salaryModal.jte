@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.common.web.Seed
@param Employee employee
@param FormErrors errors
<form hx-post="/employees/${employee.id()}/salary" hx-target="#modal-body">
    @template.components.modal(title = "연봉 변경", description = employee.getName() + " · 새 연봉은 적용일부터 반영되고 이전 연봉은 전날로 종료됩니다.", body = @`
        @template.components.globalError(errors = errors)
        <div class="grid grid-cols-2 gap-4">
            <div>
                <label for="annualSalary" class="label">연봉 (원)</label>
                <input id="annualSalary" name="annualSalary" type="number" min="1" step="100000" class="input" required placeholder="60000000">
            </div>
            <div>
                <label for="startDate" class="label">적용일</label>
                <input id="startDate" name="startDate" type="date" class="input" required value="${java.time.LocalDate.now().withDayOfMonth(1).toString()}">
            </div>
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("neutralWeak")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">등록</button>
    `)
</form>
