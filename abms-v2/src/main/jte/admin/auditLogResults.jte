@import kr.co.abacus.abms.common.audit.AuditEntry
@import kr.co.abacus.abms.common.web.PageView
@param PageView<AuditEntry> page
@template.components.auditHistory(entries = page.content(), showTarget = true, collapsible = false)
<div class="mt-x2">@template.components.pagination(page = page, target = "#audit-results")</div>
