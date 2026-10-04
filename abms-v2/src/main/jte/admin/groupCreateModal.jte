@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Seed
@param FormErrors errors
<form hx-post="/admin/permission-groups" hx-target="#modal-body">
    @template.components.modal(title = "권한 그룹 추가", context = "권한 그룹", hint = "추가", body = @`
        @template.components.globalError(errors = errors)
        <input name="name" required maxlength="50" autofocus placeholder="그룹명 (예: 경영지원팀)" aria-label="그룹명"
               class="w-full bg-transparent t7-bold text-fg-neutral outline-none placeholder:text-fg-placeholder">
        @if(errors.has("name"))<p class="field-error">${errors.get("name")}</p>@endif
        <textarea name="description" maxlength="255" rows="2" placeholder="설명 추가…" aria-label="설명"
                  class="mt-x2 w-full resize-none bg-transparent t4-regular text-fg-neutral-muted outline-none placeholder:text-fg-placeholder"></textarea>
        <p class="t3-regular text-fg-neutral-subtle">추가한 뒤 그룹 화면에서 권한과 범위를 지정합니다.</p>
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">추가</button>
    `)
</form>
