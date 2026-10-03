@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.report.WeeklyReport
@param ViewContext ctx
@param WeeklyReport report
@param String author
@template.layout.app(ctx = ctx, title = report.getTitle(), content = @`
    @template.components.pageHeader(title = report.getTitle(), subtitle = Ui.date(report.getWeekStart()) + " ~ " + Ui.date(report.getWeekEnd()) + " · 작성 " + author, breadcrumb = @`
        <a href="/reports" class="hover:text-slate-700">주간 보고서</a>${Icons.of("chevron-right", "size-3")}<span>보고서</span>
    `, actions = @`
        <button type="button" class="btn btn-secondary" onclick="navigator.clipboard.writeText(document.getElementById('report-source').textContent).then(() => abmsToast('success', '마크다운을 복사했습니다.'))">복사</button>
        <button type="button" class="btn btn-secondary" onclick="window.print()">인쇄</button>
        <a href="/reports/${report.id()}/edit" class="btn btn-secondary">${Icons.of("pencil", "size-4")} 편집</a>
        <form method="post" action="/reports/${report.id()}/delete" hx-confirm="보고서를 삭제할까요?">
            <button class="btn btn-danger-ghost">${Icons.of("trash", "size-4")}</button>
        </form>
    `)
    <article class="card mx-auto max-w-4xl">
        <div class="flex items-center gap-2 border-b border-slate-100 px-6 py-3 text-xs text-slate-500">
            @if(report.getGenerator() == WeeklyReport.Generator.AI)
                <span class="badge badge-purple">${Icons.of("sparkles", "size-3")} AI 생성</span> AI가 작성한 초안입니다. 수치를 검토한 뒤 공유하세요.
            @else
                <span class="badge badge-gray">템플릿</span> 시스템 데이터로 자동 작성된 보고서입니다.
            @endif
        </div>
        <div class="prose-md px-6 py-6 sm:px-8" data-markdown>${report.getContent()}</div>
        <template id="report-source">${report.getContent()}</template>
    </article>
`)
