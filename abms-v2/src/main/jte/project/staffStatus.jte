@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.project.ProjectSections.StaffRow
@param StaffRow row
@param java.time.LocalDate today
@if(row.assignment().isActiveOn(today))
    <span class="${Seed.badge("informative")}">투입 중</span>
@elseif(row.assignment().getPeriod().startDate().isAfter(today))
    <span class="${Seed.badge("neutral", "outline")}">예정</span>
@else
    <span class="${Seed.badge("neutral")}">종료</span>
@endif
