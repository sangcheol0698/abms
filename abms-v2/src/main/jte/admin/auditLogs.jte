@import java.util.Map
@import kr.co.abacus.abms.common.audit.AuditEntry
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.ViewContext
@param ViewContext ctx
@param PageView<AuditEntry> page
@param Map<String, String> types
@param String type = null
@template.layout.app(ctx = ctx, title = "변경 이력", content = @`
    @template.components.pageHeader(title = "변경 이력", subtitle = "누가 언제 무엇을 바꿨는지 확인합니다. 계약·매출·연봉·권한 변경이 모두 기록됩니다.")
    <form class="mb-x4 flex items-center gap-x2" action="/admin/audit-logs" hx-get="/admin/audit-logs" hx-target="#audit-results" hx-push-url="true"
          hx-trigger="change from:select[name=type], submit">
        <label for="type" class="sr-only">대상</label>
        <select id="type" name="type" class="select filter-select w-44">
            <option value="">전체 대상</option>
            @for(var t : types.entrySet())
                <option value="${t.getKey()}" selected="${t.getKey().equals(type)}">${t.getValue()}</option>
            @endfor
        </select>
    </form>
    <div id="audit-results">@template.admin.auditLogResults(page = page)</div>
`)
