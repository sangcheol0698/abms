@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@param FormErrors errors
@if(errors.global() != null)
    <div class="mb-4 flex items-start gap-2 rounded-lg bg-rose-50 px-3.5 py-3 text-sm text-rose-700 ring-1 ring-rose-200">
        ${Icons.of("warning", "mt-0.5 size-4 shrink-0")}
        <span>${errors.global()}</span>
    </div>
@endif
