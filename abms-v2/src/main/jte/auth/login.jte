@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param String error = null
@param String info = null
@param boolean demo = false
@template.layout.auth(title = "로그인", content = @`
    <div class="w-full max-w-sm">
        <div class="mb-8 text-center">
            <div class="mx-auto flex size-12 items-center justify-center rounded-xl bg-bg-brand-solid text-lg font-bold text-white">A</div>
            <h1 class="mt-5 text-2xl font-semibold tracking-tight text-fg-neutral">ABMS</h1>
            <p class="mt-1 text-sm text-fg-neutral-subtle">프로젝트 손익 관리 시스템</p>
        </div>
        <div class="card p-6 sm:p-8">
            @if(error != null)
                <div class="mb-4 rounded-lg bg-bg-critical-weak px-3.5 py-3 text-sm text-fg-critical ring-1 ring-stroke-critical-weak" role="alert">${error}</div>
            @elseif(info != null)
                <div class="mb-4 rounded-lg bg-bg-positive-weak px-3.5 py-3 text-sm text-fg-positive ring-1 ring-stroke-positive-weak" role="status">${info}</div>
            @endif
            <form method="post" action="/login" class="space-y-4">
                <input type="hidden" name="${ctx.csrfParameter()}" value="${ctx.csrfToken()}">
                <div>
                    <label for="username" class="label">이메일</label>
                    <input id="username" name="username" type="email" class="input" required autofocus autocomplete="username" placeholder="name@company.com">
                </div>
                <div>
                    <label for="password" class="label">비밀번호</label>
                    <input id="password" name="password" type="password" class="input" required autocomplete="current-password">
                </div>
                <button type="submit" class="${Seed.button("brandSolid")} w-full py-2.5">로그인</button>
            </form>
        </div>
        @if(demo)
            <div class="mt-6 rounded-xl bg-bg-layer-default/70 p-4 text-xs text-fg-neutral-muted ring-1 ring-stroke-neutral-subtle">
                <p class="mb-2 font-semibold text-fg-neutral-muted">데모 계정 (비밀번호 <code class="rounded bg-bg-neutral-weak px-1">abms1234!</code>)</p>
                <ul class="space-y-1">
                    <li><code>admin@abms.co</code> — 최고 관리자</li>
                    <li><code>leader@abms.co</code> — 부서장 (AI/Data사업담당)</li>
                    <li><code>member@abms.co</code> — 일반 사용자</li>
                </ul>
            </div>
        @endif
        <p class="mt-6 text-center text-xs text-fg-placeholder">계정이 없다면 관리자에게 발급을 요청하세요.</p>
    </div>
`)
