@import kr.co.abacus.abms.common.web.Icons
@param String title
@param String username
@param String password
@template.components.modal(title = title, body = @`
    <div class="space-y-3">
        <div class="flex items-start gap-2 rounded-lg bg-amber-50 p-3 text-sm text-amber-800 ring-1 ring-amber-200">
            ${Icons.of("warning", "mt-0.5 size-4 shrink-0")}
            <span>임시 비밀번호는 지금 한 번만 표시됩니다. 사용자에게 안전하게 전달하고 첫 로그인 후 변경하도록 안내하세요.</span>
        </div>
        <dl class="divide-y divide-slate-100 rounded-lg ring-1 ring-slate-200">
            <div class="flex justify-between px-4 py-3 text-sm"><dt class="text-slate-500">아이디</dt><dd class="font-medium">${username}</dd></div>
            <div class="flex items-center justify-between px-4 py-3 text-sm">
                <dt class="text-slate-500">임시 비밀번호</dt>
                <dd class="flex items-center gap-2">
                    <code id="temp-password" class="rounded bg-slate-100 px-2 py-1 font-mono text-base">${password}</code>
                    <button type="button" class="btn btn-ghost btn-sm" onclick="navigator.clipboard.writeText(document.getElementById('temp-password').textContent).then(() => abmsToast('success', '복사했습니다.'))">복사</button>
                </dd>
            </div>
        </dl>
    </div>
`, footer = @`
    <button type="button" class="btn btn-primary" data-close-modal>확인</button>
`)
