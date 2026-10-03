@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param Integer status = 500
@param String message = null
@param String error = null
!{String title = kr.co.abacus.abms.common.web.Ui.errorTitle(status);}
!{String detail = kr.co.abacus.abms.common.web.Ui.errorDetail(message);}
@if(ctx != null && ctx.loggedIn())
    @template.layout.app(ctx = ctx, title = title, content = @`
        <div class="mx-auto max-w-lg py-16 text-center">
            <span class="mx-auto flex size-14 items-center justify-center rounded-full bg-bg-critical-weak text-fg-critical">${Icons.of("warning", "size-7")}</span>
            <p class="mt-5 text-sm font-semibold text-fg-brand">${status}</p>
            <h1 class="mt-1 text-2xl font-semibold text-fg-neutral">${title}</h1>
            <p class="mt-3 text-sm text-fg-neutral-subtle">${detail}</p>
            <div class="mt-8 flex justify-center gap-2">
                <button type="button" class="${Seed.button("neutralWeak")}" onclick="history.back()">이전으로</button>
                <a href="/" class="${Seed.button("brandSolid")}">홈으로</a>
            </div>
        </div>
    `)
@else
    @template.layout.auth(title = title, content = @`
        <div class="text-center">
            <p class="text-sm font-semibold text-fg-brand">${status}</p>
            <h1 class="mt-1 text-2xl font-semibold text-fg-neutral">${title}</h1>
            <p class="mt-3 text-sm text-fg-neutral-subtle">${detail}</p>
            <a href="/" class="${Seed.button("brandSolid")} mt-8">홈으로</a>
        </div>
    `)
@endif
