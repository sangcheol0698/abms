@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.employee.Employee
@param Employee employee
@param String returnTo = null
@param String buttonSize = "xsmall"
<%--
  프로필 사진 변경·삭제. 파일을 고르면 바로 올린다. (JPG·PNG·GIF, 5MB 이하, 서버에서 정사각형 320px 로 다시 저장)
--%>
<div class="flex flex-wrap items-center gap-x1_5">
    <form hx-post="/employees/${employee.id()}/photo" hx-encoding="multipart/form-data">
        <input type="hidden" name="returnTo" value="${returnTo}">
        <label class="${Seed.button("neutralOutline", buttonSize)} cursor-pointer focus-within:outline-2 focus-within:outline-stroke-focus-ring" title="JPG·PNG·GIF, 5MB 이하">
            ${Icons.of("pencil", "size-3.5")} ${employee.photoUrl() == null ? "사진 등록" : "사진 변경"}
            <input type="file" name="file" accept="image/jpeg,image/png,image/gif" class="sr-only" data-submit-on-change aria-label="프로필 사진 파일">
        </label>
    </form>
    @if(employee.photoUrl() != null)
        <form hx-post="/employees/${employee.id()}/photo/delete" hx-confirm="프로필 사진을 삭제할까요? 기본 아바타가 표시됩니다.">
            <input type="hidden" name="returnTo" value="${returnTo}">
            <button type="submit" class="${Seed.button("ghost", buttonSize)} text-fg-neutral-subtle">삭제</button>
        </form>
    @endif
</div>
