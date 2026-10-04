@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.report.WeeklyReport
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param WeeklyReport report
@template.layout.app(ctx = ctx, title = "보고서 편집", content = @`
    @template.components.pageHeader(title = "보고서 편집", breadcrumb = @`
        <a href="/reports" class="hover:text-fg-neutral-muted">주간 보고서</a>${Icons.of("chevron-right", "size-3")}
        <a href="/reports/${report.id()}" class="hover:text-fg-neutral-muted">보고서</a>${Icons.of("chevron-right", "size-3")}<span>편집</span>
    `)
    <form method="post" action="/reports/${report.id()}" class="card mx-auto max-w-4xl">
        <div class="card-body space-y-4">
            <div>
                <label for="title" class="label">제목</label>
                <input id="title" name="title" class="input" value="${report.getTitle()}" required maxlength="200">
            </div>
            <div>
                <label for="content" class="label">내용 (마크다운)</label>
                <textarea id="content" name="content" rows="28" class="textarea font-mono text-xs leading-relaxed" required>${report.getContent()}</textarea>
            </div>
        </div>
        <div class="flex justify-end gap-2 rounded-b-xl border-t border-stroke-neutral-subtle bg-bg-neutral-muted px-5 py-3.5">
            <a href="/reports/${report.id()}" class="${Seed.button("neutralWeak")}">취소</a>
            <button type="submit" class="${Seed.button("brandSolid")}">저장</button>
        </div>
    </form>
`)
