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
@param boolean money = false
@param Long moneyMax = null
<div>
    <label for="${name}" class="label">${label}@if(required)<span class="required-mark">*</span>@endif</label>
    <input id="${name}" name="${name}" type="${type}" value="${value}" placeholder="${placeholder}" required="${required}"
           class="input ${errors.has(name) ? "input-error" : ""} ${money ? "tabular-nums" : ""}" step="${"number".equals(type) ? "any" : null}" autocomplete="off"
           data-money="${money ? label.replaceAll(" \\(원\\)$", "") : null}" data-money-max="${moneyMax == null ? null : String.valueOf(moneyMax)}">
    @if(errors.has(name))
        <p class="field-error">${errors.get(name)}</p>
    @elseif(help != null)
        <p class="field-help">${help}</p>
    @endif
</div>
