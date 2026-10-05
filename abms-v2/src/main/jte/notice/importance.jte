@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.notice.NoticeImportance
@param NoticeImportance importance
@if(importance == NoticeImportance.URGENT)
    <span class="${Seed.badge("critical")}">긴급</span>
@elseif(importance == NoticeImportance.IMPORTANT)
    <span class="${Seed.badge("warning")}">중요</span>
@endif
