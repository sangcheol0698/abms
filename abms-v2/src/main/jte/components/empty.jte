@import kr.co.abacus.abms.common.web.Icons
@param String message
@param String icon = "folder"
<div class="flex flex-col items-center justify-center gap-x3 px-x6 py-x14 text-center">
    <span class="flex size-x12 items-center justify-center rounded-full bg-bg-neutral-weak text-fg-neutral-subtle">${Icons.of(icon, "size-6")}</span>
    <p class="t4-regular text-fg-neutral-subtle">${message}</p>
</div>
