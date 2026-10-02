@import kr.co.abacus.abms.common.web.FormErrors
@param FormErrors errors
<form hx-post="/admin/permission-groups" hx-target="#modal-body">
    @template.components.modal(title = "권한 그룹 추가", body = @`
        @template.components.globalError(errors = errors)
        <div class="space-y-4">
            <div>
                <label for="name" class="label">그룹명</label>
                <input id="name" name="name" class="input" required maxlength="50" placeholder="예: 경영지원팀">
            </div>
            <div>
                <label for="description" class="label">설명</label>
                <input id="description" name="description" class="input" maxlength="255">
            </div>
        </div>
    `, footer = @`
        <button type="button" class="btn btn-secondary" data-close-modal>취소</button>
        <button type="submit" class="btn btn-primary">추가</button>
    `)
</form>
