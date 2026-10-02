@import kr.co.abacus.abms.common.web.Icons
@param String message
@param String icon = "folder"
<div class="flex flex-col items-center justify-center gap-2 px-6 py-14 text-center">
    <span class="flex size-11 items-center justify-center rounded-full bg-slate-100 text-slate-400">${Icons.of(icon, "size-6")}</span>
    <p class="text-sm text-slate-500">${message}</p>
</div>
