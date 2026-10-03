@import java.util.List
@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Ui.SelectOption
@param String name
@param String label
@param List<SelectOption> options
@param String value = ""
@param FormErrors errors
@param boolean required = false
@param String emptyLabel = null
<div>
    <label for="${name}" class="label">${label}@if(required)<span class="ml-0.5 text-rose-500">*</span>@endif</label>
    <select id="${name}" name="${name}" required="${required}" class="select ${errors.has(name) ? "input-error" : ""}">
        @if(emptyLabel != null)
            <option value="">${emptyLabel}</option>
        @endif
        @for(SelectOption option : options)
            <option value="${option.value()}" selected="${option.value().equals(value)}">${option.label()}</option>
        @endfor
    </select>
    @if(errors.has(name))
        <p class="field-error">${errors.get(name)}</p>
    @endif
</div>
