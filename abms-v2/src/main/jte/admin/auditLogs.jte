@import java.util.Map
@import kr.co.abacus.abms.common.audit.AuditEntry
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.ViewContext
@param ViewContext ctx
@param PageView<AuditEntry> page
@param Map<String, String> types
@param String type = null
@template.layout.app(ctx = ctx, title = "변경 이력", fill = true, content = @`
<%-- 데스크톱: 페이지는 고정, 목록 본문만 스크롤 (fill 레이아웃). 태블릿·모바일은 문서 스크롤. --%>
<div class="flex flex-col lg:h-full">
    <div class="px-x4 pt-x5 sm:px-x6 lg:px-x8 lg:pt-x6">
    @template.components.pageHeader(title = "변경 이력", subtitle = "누가 언제 무엇을 바꿨는지 확인합니다. 계약·매출·연봉·권한 변경이 모두 기록됩니다.")
    </div>

    <div class="flex min-h-0 flex-1 flex-col border-t border-stroke-neutral-subtle">
    <form class="view-bar shrink-0 sm:px-x6 lg:px-x8" action="/admin/audit-logs" hx-get="/admin/audit-logs" hx-target="#audit-results" hx-push-url="true"
          hx-trigger="change from:select[name=type], submit">
        <label for="type" class="sr-only">대상</label>
        <select id="type" name="type" class="select filter-select w-44">
            <option value="">전체 대상</option>
            @for(var t : types.entrySet())
                <option value="${t.getKey()}" selected="${t.getKey().equals(type)}">${t.getValue()}</option>
            @endfor
        </select>
    </form>
    <div id="audit-results" class="flex min-h-0 flex-1 flex-col">@template.admin.auditLogResults(page = page)</div>
    </div>
</div>
`)
