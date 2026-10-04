@import kr.co.abacus.abms.employee.EmployeeAvatar
@param EmployeeAvatar selected
@param String initial
<div class="flex flex-wrap gap-2">
    @for(EmployeeAvatar avatar : EmployeeAvatar.values())
        <label class="cursor-pointer" title="${avatar.label()}">
            <input type="radio" name="avatar" value="${avatar.name()}" class="peer sr-only" checked="${avatar == selected}">
            <span class="flex size-10 items-center justify-center rounded-full text-sm font-semibold text-white ring-2 ring-transparent ring-offset-2 transition peer-checked:ring-stroke-brand-solid peer-focus-visible:ring-stroke-brand-weak"
                  style="background: ${avatar.gradient()}">${initial}</span>
        </label>
    @endfor
</div>
