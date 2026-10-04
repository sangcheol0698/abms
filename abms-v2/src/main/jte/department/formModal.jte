@import java.util.List
@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.Ui.SelectOption
@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.department.DepartmentController.DepartmentForm
@import kr.co.abacus.abms.department.DepartmentType
@import kr.co.abacus.abms.common.web.Seed
@param Department department = null
@param DepartmentForm form
@param FormErrors errors
@param List<SelectOption> departmentOptions
!{boolean editing = department != null;}
<form hx-post="${editing ? "/departments/" + department.id() : "/departments"}" hx-target="#modal-body">
    @template.components.modal(title = editing ? "부서 수정" : "부서 추가", context = editing ? department.getName() : "부서", hint = editing ? "저장" : "추가", body = @`
        @template.components.globalError(errors = errors)
        <input name="name" value="${Ui.value(form.name())}" required autofocus placeholder="부서명" aria-label="부서명"
               class="mb-x4 w-full bg-transparent t7-bold text-fg-neutral outline-none placeholder:text-fg-placeholder">
        @if(errors.has("name"))<p class="field-error -mt-3 mb-x3">${errors.get("name")}</p>@endif
        <div class="prop-form">
            @template.components.propRow(label = "부서 코드", forId = "code", required = !editing, error = errors.get("code"), control = @`
                <input id="code" name="code" class="input" value="${editing ? department.getCode() : Ui.value(form.code())}" required disabled="${editing}" placeholder="ABC1234">
            `)
            @template.components.propRow(label = "유형", forId = "type", error = errors.get("type"), control = @`
                <select id="type" name="type" class="select">
                    @for(var o : Ui.options(DepartmentType.values()))
                        <option value="${o.value()}" selected="${o.value().equals(Ui.value(form.type()))}">${o.label()}</option>
                    @endfor
                </select>
            `)
            @template.components.propRow(label = "소개", forId = "description", error = errors.get("description"), control = @`
                <textarea id="description" name="description" rows="2" maxlength="500" class="textarea" placeholder="부서의 역할과 담당 업무">${Ui.value(form.description())}</textarea>
            `)
            @template.components.propRow(label = "상위 부서", forId = "parentId", error = errors.get("parentId"), control = @`
                <select id="parentId" name="parentId" class="select">
                    <option value="">(최상위)</option>
                    @for(SelectOption o : departmentOptions)
                        <option value="${o.value()}" selected="${o.value().equals(Ui.value(form.parentId()))}">${o.label()}</option>
                    @endfor
                </select>
            `)
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "추가"}</button>
    `)
</form>
