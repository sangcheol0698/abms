@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@param String title
@param String username
@param String password
@template.components.modal(title = title, context = "계정 관리", body = @`
    <div class="space-y-x4">
        <div class="flex items-start gap-x2 rounded-r2 bg-bg-warning-weak px-x3 py-x2_5 t3-regular text-fg-warning-contrast">
            ${Icons.of("warning", "mt-x0_5 size-4 shrink-0")}
            <span>임시 비밀번호는 지금 한 번만 표시됩니다. 사용자에게 안전하게 전달하고 첫 로그인 후 변경하도록 안내하세요.</span>
        </div>
        <div class="prop-form">
            <span class="prop-label">아이디</span>
            <span class="flex min-h-8 items-center t4-medium text-fg-neutral">${username}</span>
            <span class="prop-label">임시 비밀번호</span>
            <span class="flex min-h-8 items-center gap-x2">
                <code id="temp-password" class="rounded-r1_5 bg-bg-neutral-weak px-x2 py-x1 font-mono t5-medium text-fg-neutral">${password}</code>
                <button type="button" class="${Seed.button("neutralOutline", "xsmall")}" data-copy-from="temp-password" data-copy-message="복사했습니다.">${Icons.of("check", "size-3.5")} 복사</button>
            </span>
        </div>
    </div>
`, footer = @`
    <button type="button" class="${Seed.button("brandSolid")}" data-close-modal autofocus>확인</button>
`)
