@import java.util.List
@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.Ui.SelectOption
@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.department.DepartmentController.DepartmentForm
@import kr.co.abacus.abms.department.DepartmentType
@param Department department = null
@param DepartmentForm form
@param FormErrors errors
@param List<SelectOption> departmentOptions
!{boolean editing = department != null;}
<form hx-post="${editing ? "/departments/" + department.id() : "/departments"}" hx-target="#modal-body">
    @template.components.modal(title = editing ? "부서 수정" : "부서 추가", body = @`
        @template.components.globalError(errors = errors)
        <div class="grid grid-cols-2 gap-4">
            <div>
                <label for="code" class="label">부서 코드</label>
                <input id="code" name="code" class="input" value="${editing ? department.getCode() : Ui.value(form.code())}" required disabled="${editing}" placeholder="ABC1234">
            </div>
            @template.components.select(name = "type", label = "유형", options = Ui.options(DepartmentType.values()), value = Ui.value(form.type()), errors = errors)
            <div class="col-span-2">
                <label for="name" class="label">부서명</label>
                <input id="name" name="name" class="input" value="${Ui.value(form.name())}" required>
            </div>
            <div class="col-span-2">
                @template.components.select(name = "parentId", label = "상위 부서", options = departmentOptions, value = Ui.value(form.parentId()), errors = errors, emptyLabel = "(최상위)")
            </div>
        </div>
    `, footer = @`
        <button type="button" class="btn btn-secondary" data-close-modal>취소</button>
        <button type="submit" class="btn btn-primary">${editing ? "저장" : "추가"}</button>
    `)
</form>
