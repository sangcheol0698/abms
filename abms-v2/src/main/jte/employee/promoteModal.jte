@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.employee.EmployeeGrade
@import kr.co.abacus.abms.employee.EmployeePosition
@import kr.co.abacus.abms.common.web.Seed
@param Employee employee
@param FormErrors errors
<form hx-post="/employees/${employee.id()}/promote" hx-target="#modal-body">
    @template.components.modal(title = "승진 / 등급 변경", description = "현재 " + employee.getPosition().label() + " · " + employee.getGrade().label(), body = @`
        @template.components.globalError(errors = errors)
        <div class="grid grid-cols-2 gap-4">
            <div>
                <label for="position" class="label">직급</label>
                <select id="position" name="position" class="select">
                    @for(EmployeePosition p : EmployeePosition.values())
                        @if(p.level() >= employee.getPosition().level())
                            <option value="${p.name()}" selected="${p == employee.getPosition()}">${p.label()}</option>
                        @endif
                    @endfor
                </select>
            </div>
            <div>
                <label for="grade" class="label">등급</label>
                <select id="grade" name="grade" class="select">
                    @for(EmployeeGrade g : EmployeeGrade.values())
                        <option value="${g.name()}" selected="${g == employee.getGrade()}">${g.label()}</option>
                    @endfor
                </select>
            </div>
            <div class="col-span-2">
                <label for="effectiveDate" class="label">적용일</label>
                <input id="effectiveDate" name="effectiveDate" type="date" class="input" value="${java.time.LocalDate.now().toString()}">
            </div>
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("neutralWeak")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">변경</button>
    `)
</form>
