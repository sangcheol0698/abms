@import gg.jte.Content
@param String label
@param Content control
@param String forId = null
@param boolean required = false
@param String error = null
@param String help = null
<%-- 모달 폼의 "라벨 | 값" 행 (prop-form 그리드 안에서 사용) --%>
<label for="${forId}" class="prop-label">${label}@if(required)<span class="required-mark">*</span>@endif</label>
<div class="min-w-0">
    ${control}
    @if(error != null)
        <p class="field-error">${error}</p>
    @elseif(help != null)
        <p class="field-help">${help}</p>
    @endif
</div>
