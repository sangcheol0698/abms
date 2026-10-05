@import kr.co.abacus.abms.common.audit.AuditEntry
@import kr.co.abacus.abms.common.web.PageView
@param PageView<AuditEntry> page
<div class="min-h-0 flex-1 lg:overflow-y-auto lg:overflow-x-hidden lg:overscroll-y-contain" data-fill-scroll>
    @template.components.auditHistory(entries = page.content(), showTarget = true, collapsible = false, framed = false)
</div>
<div class="shrink-0 bg-bg-layer-default sm:px-x2 lg:px-x4">@template.components.pagination(page = page, target = "#audit-results")</div>
