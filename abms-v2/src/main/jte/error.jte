@import kr.co.abacus.abms.web.ViewContext
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param Integer status = 500
@param String message = null
@param String error = null
!{String title = kr.co.abacus.abms.common.web.Ui.errorTitle(status);}
!{String detail = kr.co.abacus.abms.common.web.Ui.errorDetail(status, message);}
@if(ctx != null && ctx.loggedIn())
    @template.layout.app(ctx = ctx, title = title, content = @`
        <div class="mx-auto max-w-lg py-16 text-center">
            <span class="mx-auto flex size-10 items-center justify-center rounded-r3 bg-bg-neutral-weak text-fg-neutral-muted">${Icons.of("warning", "size-5")}</span>
            <p class="mt-x5 t3-medium text-fg-neutral-subtle tabular-nums">오류 ${status}</p>
            <h1 class="mt-x1 t7-bold text-fg-neutral">${title}</h1>
            <p class="mt-x2 t4-regular text-fg-neutral-subtle">${detail}</p>
            @if(status != null && status == 403)
                <p class="mt-x1 t3-regular text-fg-neutral-subtle">권한이 필요하면 권한 그룹 관리자에게 요청하세요. 권한이 바뀌면 다시 로그인해야 반영됩니다.</p>
            @endif
            <div class="mt-x6 flex justify-center gap-x2">
                <button type="button" class="${Seed.button("neutralOutline")}" data-history-back>이전으로</button>
                @if(status != null && status == 403)
                    <a href="/me#my-permissions" class="${Seed.button("neutralOutline")}">내 권한 확인</a>
                @endif
                <a href="/" class="${Seed.button("brandSolid")}">홈으로</a>
            </div>
        </div>
    `)
@else
    @template.layout.auth(title = title, content = @`
        <div class="text-center">
            <p class="t3-medium text-fg-neutral-subtle tabular-nums">오류 ${status}</p>
            <h1 class="mt-x1 t7-bold text-fg-neutral">${title}</h1>
            <p class="mt-x2 t4-regular text-fg-neutral-subtle">${detail}</p>
            <a href="/" class="${Seed.button("brandSolid")} mt-x6">홈으로</a>
        </div>
    `)
@endif
