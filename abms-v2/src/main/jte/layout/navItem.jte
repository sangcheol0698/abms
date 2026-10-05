@import kr.co.abacus.abms.common.web.Icons
@param String href
@param String label
@param String icon
@param boolean current
@param long badge = 0
<a href="${href}" class="seed-side-navigation-menu-item__root hover:bg-bg-transparent-pressed" data-current="${current}" aria-current="${current ? "page" : null}">
    <span class="seed-side-navigation-menu-item__prefixIcon" data-current="${current}">${Icons.of(icon, "size-4")}</span>
    <span class="seed-side-navigation-menu-item__label" data-current="${current}">${label}</span>
    @if(badge > 0)<span class="ml-auto min-w-5 rounded-full bg-bg-brand-solid px-x1_5 text-center t2-bold text-white tabular-nums" aria-label="안 읽은 공지 ${badge}건">${badge > 99 ? "99+" : String.valueOf(badge)}</span>@endif
</a>
