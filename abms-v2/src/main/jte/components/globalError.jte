@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@param FormErrors errors
@if(errors.global() != null)
    <div role="alert" class="seed-callout__root seed-callout__root--tone_critical mb-x4">
        ${Icons.of("warning", "size-4 shrink-0 text-fg-critical")}
        <div class="seed-callout__content">
            <p class="seed-callout__description seed-callout__description--tone_critical">${errors.global()}</p>
        </div>
    </div>
@endif
