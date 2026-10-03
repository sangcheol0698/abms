@import java.time.LocalDate
@import java.util.Map
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.report.WeeklyReport
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param PageView<WeeklyReport> page
@param Map<Long, String> authors
@param LocalDate defaultDate
@template.layout.app(ctx = ctx, title = "주간 보고서", content = @`
    @template.components.pageHeader(title = "주간 보고서", subtitle = "프로젝트 현황·청구·인력 변화를 바탕으로 주간 운영 보고서 초안을 만듭니다.")
    <div class="grid gap-x6 xl:grid-cols-[minmax(0,1fr)_300px]">
        <section class="min-w-0">
            <h2 class="section-title">보고서 <span class="section-count">${page.totalElements()}</span></h2>
            <div class="list-box">
                @if(page.isEmpty())
                    @template.components.empty(message = "생성된 보고서가 없습니다.", icon = "document")
                @else
                    @for(WeeklyReport r : page.content())
                        <a href="/reports/${r.id()}" class="list-row">
                            ${Icons.of("document", "size-4 shrink-0 text-fg-neutral-subtle")}
                            <span class="min-w-0 flex-1 truncate t4-medium">${r.getTitle()}</span>
                            <span class="list-cell-muted hidden w-48 tabular-nums md:block">${Ui.date(r.getWeekStart())} ~ ${Ui.date(r.getWeekEnd())}</span>
                            <span class="list-cell-muted hidden w-20 lg:block">${authors.getOrDefault(r.getAuthorAccountId(), "-")}</span>
                            <span class="flex w-28 shrink-0 justify-end gap-x1">
                                @if(r.isEdited())<span class="${Seed.badge("informative")}">편집됨</span>@endif
                                @if(r.getGenerator() == WeeklyReport.Generator.AI)
                                    <span class="${Seed.badge("neutral")} bg-bg-magic-weak">${Icons.of("sparkles", "size-3 text-fg-brand")} AI</span>
                                @else
                                    <span class="${Seed.badge("neutral")}">템플릿</span>
                                @endif
                            </span>
                            <span class="list-cell-muted hidden w-16 text-right sm:block">${Ui.relative(r.getCreatedAt())}</span>
                        </a>
                    @endfor
                @endif
            </div>
            @template.components.pagination(page = page, target = "body")
        </section>

        <%-- 새 보고서 생성 패널 --%>
        <aside>
            <div class="xl:sticky xl:top-6">
                <h2 class="section-title">새 보고서</h2>
                <form method="post" action="/reports" class="list-box space-y-3 p-x4" hx-disabled-elt="find button">
                    <div>
                        <label for="date" class="label">보고 주간 (해당 주의 아무 날짜)</label>
                        <input id="date" name="date" type="date" class="input" value="${defaultDate.toString()}" max="${LocalDate.now().toString()}" required>
                        <p class="field-help">월요일~일요일 기준으로 집계합니다.</p>
                    </div>
                    <button type="submit" class="${Seed.button("brandSolid")} w-full">
                        <span class="htmx-indicator">
                            <svg class="size-4 animate-spin" viewBox="0 0 24 24" fill="none"><circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" class="opacity-25"/><path d="M4 12a8 8 0 0 1 8-8" stroke="currentColor" stroke-width="3" class="opacity-75"/></svg>
                        </span>
                        ${Icons.of("sparkles", "size-4 htmx-hide-on-request")} 보고서 생성
                    </button>
                    @if(!ctx.aiEnabled())
                        <p class="t3-regular text-fg-neutral-subtle">AI 모델이 설정되지 않아 템플릿 기반 보고서가 생성됩니다.</p>
                    @endif
                </form>
            </div>
        </aside>
    </div>
`)
