@import kr.co.abacus.abms.common.web.Icons
@param String href
@param String label
@param String icon
@param boolean current
<a href="${href}" class="seed-side-navigation-menu-item__root hover:bg-bg-transparent-pressed" data-current="${current}" aria-current="${current ? "page" : null}">
    <span class="seed-side-navigation-menu-item__prefixIcon" data-current="${current}">${Icons.of(icon, "size-5")}</span>
    <span class="seed-side-navigation-menu-item__label" data-current="${current}">${label}</span>
</a>
