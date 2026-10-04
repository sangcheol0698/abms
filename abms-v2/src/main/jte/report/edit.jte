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
    <form method="post" action="/reports/${report.id()}" class="mx-auto max-w-3xl space-y-3">
        <label for="title" class="sr-only">제목</label>
        <input id="title" name="title" value="${report.getTitle()}" required maxlength="200" placeholder="제목"
               class="w-full rounded-r2 bg-transparent px-x1 py-x1 t7-bold text-fg-neutral outline-none placeholder:text-fg-placeholder focus:bg-bg-neutral-muted">
        <label for="content" class="sr-only">내용 (마크다운)</label>
        <textarea id="content" name="content" rows="28" required
                  class="textarea min-h-[60vh] font-mono t4-regular leading-relaxed">${report.getContent()}</textarea>
        <p class="t3-regular text-fg-neutral-subtle">마크다운 문법을 사용할 수 있습니다. 저장하면 '편집됨'으로 표시됩니다.</p>
        <div class="sticky bottom-4 flex items-center justify-end gap-x2 rounded-r3 border border-stroke-neutral-subtle bg-bg-layer-floating px-x4 py-x2_5 shadow-s2">
            <a href="/reports/${report.id()}" class="${Seed.button("neutralOutline")}">취소</a>
            <button type="submit" class="${Seed.button("brandSolid")}">저장</button>
        </div>
    </form>
`)
