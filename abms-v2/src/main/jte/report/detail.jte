@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.report.WeeklyReport
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param WeeklyReport report
@param String author
@template.layout.app(ctx = ctx, aside = @`
            <dl class="property-list">
                <dt>시작일</dt><dd class="tabular-nums">${Ui.date(report.getWeekStart())}</dd>
                <dt>종료일</dt><dd class="tabular-nums">${Ui.date(report.getWeekEnd())}</dd>
                <dt>작성자</dt><dd>${author}</dd>
                <dt>생성 방식</dt>
                <dd>
                    @if(report.getGenerator() == WeeklyReport.Generator.AI)
                        <span class="${Seed.badge("neutral")} bg-bg-magic-weak">${Icons.of("sparkles", "size-3 text-fg-brand")} AI</span>
                    @else
                        <span class="${Seed.badge("neutral")}">템플릿</span>
                    @endif
                </dd>
                <dt>편집</dt><dd>${report.isEdited() ? "편집됨" : "원본"}</dd>
                <dt>생성</dt><dd class="tabular-nums">${Ui.dateTime(report.getCreatedAt())}</dd>
            </dl>
`, title = report.getTitle(), content = @`
    @template.components.pageHeader(title = report.getTitle(), subtitle = Ui.date(report.getWeekStart()) + " ~ " + Ui.date(report.getWeekEnd()) + " · 작성 " + author, breadcrumb = @`
        <a href="/reports" class="hover:text-fg-neutral-muted">주간 보고서</a>${Icons.of("chevron-right", "size-3")}<span>보고서</span>
    `, actions = @`
        <button type="button" class="${Seed.button("neutralOutline")}" title="마크다운 복사" data-copy-from="report-source" data-copy-message="마크다운을 복사했습니다.">복사</button>
        <button type="button" class="${Seed.button("neutralOutline")}" data-print>인쇄</button>
        <a href="/reports/${report.id()}/edit" class="${Seed.button("neutralOutline")}">${Icons.of("pencil", "size-4")} 편집</a>
        <form method="post" action="/reports/${report.id()}/delete" hx-confirm="보고서를 삭제할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
            <button class="${Seed.iconButton("neutralOutline", "small")} text-fg-critical" aria-label="보고서 삭제" title="삭제">${Icons.of("trash", "size-4")}</button>
        </form>
    `)
        <article class="min-w-0">
            @if(report.getGenerator() == WeeklyReport.Generator.AI)
                <div class="mb-x5 flex items-center gap-x2 rounded-r2 bg-bg-magic-weak px-x3 py-x2 t3-regular text-fg-neutral-muted">
                    ${Icons.of("sparkles", "size-4 text-fg-brand")} AI가 작성한 초안입니다. 수치를 검토한 뒤 공유하세요.
                </div>
            @endif
            <div class="prose-md mx-auto max-w-3xl t5-regular" data-markdown>${report.getContent()}</div>
            <template id="report-source">${report.getContent()}</template>
        </article>

`)
