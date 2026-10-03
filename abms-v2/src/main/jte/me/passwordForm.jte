@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Seed
@param FormErrors errors
@param boolean done = false
<form class="grid grid-cols-1 gap-x3 sm:grid-cols-2" hx-post="/me/password" hx-target="#password-card" hx-disabled-elt="find button">
    <div class="sm:col-span-2">@template.components.globalError(errors = errors)</div>
    @if(done)
        <p role="status" class="rounded-r2 bg-bg-positive-weak px-x3 py-x2 t4-regular text-fg-positive sm:col-span-2">비밀번호를 변경했습니다.</p>
    @endif
    <div class="sm:col-span-2">
        <label for="currentPassword" class="label">현재 비밀번호</label>
        <input id="currentPassword" name="currentPassword" type="password" class="input sm:max-w-[calc(50%-6px)]" required autocomplete="current-password">
    </div>
    <div>
        <label for="newPassword" class="label">새 비밀번호</label>
        <input id="newPassword" name="newPassword" type="password" class="input" required minlength="8" autocomplete="new-password">
    </div>
    <div>
        <label for="confirmPassword" class="label">새 비밀번호 확인</label>
        <input id="confirmPassword" name="confirmPassword" type="password" class="input" required autocomplete="new-password">
    </div>
    <div class="sm:col-span-2">
        <button type="submit" class="${Seed.button("neutralOutline")}">비밀번호 변경</button>
    </div>
</form>
