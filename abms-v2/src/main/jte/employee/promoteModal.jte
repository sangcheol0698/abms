@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.employee.EmployeeGrade
@import kr.co.abacus.abms.employee.EmployeePosition
@import kr.co.abacus.abms.common.web.Seed
@param Employee employee
@param FormErrors errors
<form hx-post="/employees/${employee.id()}/promote" hx-target="#modal-body">
    @template.components.modal(title = "승진 / 등급 변경", context = employee.getName(), hint = "변경", body = @`
        @template.components.globalError(errors = errors)
        <div class="prop-form">
            <span class="prop-label">현재</span>
            <span class="flex min-h-8 items-center t4-regular text-fg-neutral">${employee.getPosition().label()} · ${employee.getGrade().label()}</span>
            @template.components.propRow(label = "직급", forId = "position", control = @`
                <select id="position" name="position" class="select" autofocus>
                    @for(EmployeePosition p : EmployeePosition.values())
                        @if(p.level() >= employee.getPosition().level())
                            <option value="${p.name()}" selected="${p == employee.getPosition()}">${p.label()}</option>
                        @endif
                    @endfor
                </select>
            `)
            @template.components.propRow(label = "등급", forId = "grade", control = @`
                <select id="grade" name="grade" class="select">
                    @for(EmployeeGrade g : EmployeeGrade.values())
                        <option value="${g.name()}" selected="${g == employee.getGrade()}">${g.label()}</option>
                    @endfor
                </select>
            `)
            @template.components.propRow(label = "적용일", forId = "effectiveDate", control = @`
                <input id="effectiveDate" name="effectiveDate" type="date" class="input" value="${java.time.LocalDate.now().toString()}">
            `)
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">변경</button>
    `)
</form>
