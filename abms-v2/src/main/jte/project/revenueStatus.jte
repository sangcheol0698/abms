@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.project.ProjectRevenuePlan
@param ProjectRevenuePlan plan
@if(plan.isIssued())
    <span class="${Seed.badge("positive")}">${Icons.of("check", "size-3")} 발행</span>
@elseif(plan.getRevenueDate().isBefore(java.time.LocalDate.now()))
    <span class="${Seed.badge("critical")}">미발행 (기한 경과)</span>
@else
    <span class="${Seed.badge("neutral")}">미발행</span>
@endif
