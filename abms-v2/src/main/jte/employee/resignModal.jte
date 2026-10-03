@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.common.web.Seed
@param Employee employee
@param FormErrors errors
<form hx-post="/employees/${employee.id()}/resign" hx-target="#modal-body">
    @template.components.modal(title = "퇴사 처리", description = employee.getName() + " · 입사일 " + employee.getJoinDate(), body = @`
        @template.components.globalError(errors = errors)
        <label for="resignationDate" class="label">퇴사일</label>
        <input id="resignationDate" name="resignationDate" type="date" class="input" required value="${java.time.LocalDate.now().toString()}">
        <p class="field-help">진행 중인 연봉/직급 이력은 퇴사일로 종료됩니다. 퇴사일 이후로 남은 투입 일정은 직접 정리하세요.</p>
    `, footer = @`
        <button type="button" class="${Seed.button("neutralWeak")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("criticalSolid")}">퇴사 처리</button>
    `)
</form>
