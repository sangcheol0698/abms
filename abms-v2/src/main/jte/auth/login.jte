@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param String error = null
@param String info = null
@param boolean demo = false
@template.layout.auth(title = "로그인", content = @`
    <div class="w-full max-w-xs">
        <div class="mb-x8 text-center">
            <div class="mx-auto flex size-10 items-center justify-center rounded-r3 bg-bg-brand-solid t6-bold text-fg-on-brand-solid">A</div>
            <h1 class="mt-x5 t7-bold text-fg-neutral">ABMS에 로그인</h1>
            <p class="mt-x1 t4-regular text-fg-neutral-subtle">프로젝트 손익 관리 시스템</p>
        </div>
        @if(error != null)
            <div class="mb-x4 rounded-r2 bg-bg-critical-weak px-x3 py-x2_5 t4-regular text-fg-critical" role="alert">${error}</div>
        @elseif(info != null)
            <div class="mb-x4 rounded-r2 bg-bg-positive-weak px-x3 py-x2_5 t4-regular text-fg-positive" role="status">${info}</div>
        @endif
        <form method="post" action="/login" class="space-y-3">
            <input type="hidden" name="${ctx.csrfParameter()}" value="${ctx.csrfToken()}">
            <div>
                <label for="username" class="label">이메일</label>
                <input id="username" name="username" type="email" class="input min-h-10" required autofocus autocomplete="username" placeholder="name@company.com">
            </div>
            <div>
                <label for="password" class="label">비밀번호</label>
                <input id="password" name="password" type="password" class="input min-h-10" required autocomplete="current-password">
            </div>
            <button type="submit" class="${Seed.button("brandSolid", "medium")} w-full">로그인</button>
        </form>
        @if(demo)
            <div class="mt-x8">
                <p class="mb-x2 t3-medium text-fg-neutral-subtle">데모 계정 · 비밀번호 <code class="rounded-r1 bg-bg-neutral-weak px-x1">abms1234!</code></p>
                <div class="list-box">
                    @for(String[] demoAccount : new String[][]{{"admin@abms.co", "최고 관리자"}, {"leader@abms.co", "부서장"}, {"member@abms.co", "일반 사용자"}})
                        <button type="button" class="list-row min-h-9 w-full cursor-pointer text-left"
                                onclick="document.getElementById('username').value = '${demoAccount[0]}'; document.getElementById('password').value = 'abms1234!'; document.getElementById('password').focus();">
                            <span class="flex-1 truncate t3-regular">${demoAccount[0]}</span>
                            <span class="t3-regular text-fg-neutral-subtle">${demoAccount[1]}</span>
                        </button>
                    @endfor
                </div>
            </div>
        @endif
        <p class="mt-x6 text-center t3-regular text-fg-neutral-subtle">계정이 없다면 관리자에게 발급을 요청하세요.</p>
    </div>
`)
