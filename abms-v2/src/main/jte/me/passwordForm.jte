@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Seed
@param FormErrors errors
@param boolean done = false
<div class="card-header"><h2 class="card-title">비밀번호 변경</h2></div>
<form class="card-body space-y-3" hx-post="/me/password" hx-target="#password-card" hx-disabled-elt="find button">
    @template.components.globalError(errors = errors)
    @if(done)
        <p class="rounded-lg bg-bg-positive-weak px-3 py-2 text-sm text-fg-positive ring-1 ring-stroke-positive-weak">비밀번호를 변경했습니다.</p>
    @endif
    <div>
        <label for="currentPassword" class="label">현재 비밀번호</label>
        <input id="currentPassword" name="currentPassword" type="password" class="input" required autocomplete="current-password">
    </div>
    <div>
        <label for="newPassword" class="label">새 비밀번호</label>
        <input id="newPassword" name="newPassword" type="password" class="input" required minlength="8" autocomplete="new-password">
        <p class="field-help">8자 이상, 영문과 숫자를 포함하세요.</p>
    </div>
    <div>
        <label for="confirmPassword" class="label">새 비밀번호 확인</label>
        <input id="confirmPassword" name="confirmPassword" type="password" class="input" required autocomplete="new-password">
    </div>
    <button type="submit" class="${Seed.button("brandSolid")} w-full">변경</button>
</form>
