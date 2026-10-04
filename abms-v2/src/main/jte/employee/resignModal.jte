@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.common.web.Seed
@param Employee employee
@param FormErrors errors
<form hx-post="/employees/${employee.id()}/resign" hx-target="#modal-body">
    @template.components.modal(title = "퇴사 처리", context = employee.getName(), hint = "퇴사 처리", body = @`
        @template.components.globalError(errors = errors)
        <div class="prop-form">
            <span class="prop-label">입사일</span>
            <span class="flex min-h-8 items-center t4-regular tabular-nums text-fg-neutral">${employee.getJoinDate().toString()}</span>
            @template.components.propRow(label = "퇴사일", forId = "resignationDate", required = true, error = errors.get("resignationDate"), control = @`
                <input id="resignationDate" name="resignationDate" type="date" class="input" required autofocus value="${java.time.LocalDate.now().toString()}">
            `)
        </div>
        <div class="mt-x4 rounded-r2 bg-bg-critical-weak px-x3 py-x2_5 t3-regular text-fg-critical">
            진행 중인 연봉·직급 이력은 퇴사일로 종료됩니다. 퇴사일 이후로 남은 투입 일정은 직접 정리하세요.
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("criticalSolid")}">퇴사 처리</button>
    `)
</form>
