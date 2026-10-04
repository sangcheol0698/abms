@import kr.co.abacus.abms.common.web.FormErrors
@param String name
@param String label
@param String value = ""
@param FormErrors errors
@param String type = "text"
@param boolean required = false
@param String placeholder = null
@param String help = null
@param String extra = null
<div>
    <label for="${name}" class="label">${label}@if(required)<span class="required-mark">*</span>@endif</label>
    <input id="${name}" name="${name}" type="${type}" value="${value}" placeholder="${placeholder}" required="${required}"
           class="input ${errors.has(name) ? "input-error" : ""}" step="${"number".equals(type) ? "any" : null}" autocomplete="off">
    @if(errors.has(name))
        <p class="field-error">${errors.get(name)}</p>
    @elseif(help != null)
        <p class="field-help">${help}</p>
    @endif
</div>
