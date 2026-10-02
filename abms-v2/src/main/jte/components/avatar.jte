@import kr.co.abacus.abms.employee.EmployeeAvatar
@param EmployeeAvatar avatar
@param String initial
@param String size = "size-9 text-sm"
<span class="inline-flex ${size} shrink-0 items-center justify-center rounded-full font-semibold text-white shadow-sm"
      style="background: ${avatar.gradient()}">${initial}</span>
