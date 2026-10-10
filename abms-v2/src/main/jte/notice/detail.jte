@import java.time.LocalDateTime
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.web.ViewContext
@import kr.co.abacus.abms.notice.Notice
@param ViewContext ctx
@param Notice notice
@param boolean canManage
@param LocalDateTime now
@template.layout.app(ctx = ctx, title = notice.getTitle(), content = @`
    @template.components.pageHeader(title = notice.getTitle(), showTitle = false, breadcrumb = @`
        <a href="/notices" class="hover:text-fg-neutral-muted">공지사항</a>${Icons.of("chevron-right", "size-3")}<span>${notice.getTitle()}</span>
    `, actions = @`
        @if(canManage)
            <a href="/notices/${notice.id()}/edit" class="${Seed.button("neutralOutline")}">${Icons.of("pencil", "size-4")} 수정</a>
            <form method="post" action="/notices/${notice.id()}/delete" hx-confirm="이 공지를 삭제할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                <button class="${Seed.button("neutralOutline")} text-fg-critical">${Icons.of("trash", "size-4")} 삭제</button>
            </form>
        @endif
    `)
    <%-- 제목·메타·본문을 한 칸(문서 폭)에 모아 읽기 흐름을 만든다 --%>
    <article class="mx-auto max-w-3xl pt-x4 pb-x10">
        <header class="mb-x6 border-b border-stroke-neutral-subtle pb-x5">
            @if(notice.isPinned() || notice.getImportance() != kr.co.abacus.abms.notice.NoticeImportance.NORMAL)
                <div class="mb-x2 flex flex-wrap items-center gap-x2">
                    @template.notice.importance(importance = notice.getImportance())
                    @if(notice.isPinned())<span class="inline-flex items-center gap-x1 t3-medium text-fg-brand">${Icons.of("pin", "size-3.5")} 상단 고정</span>@endif
                </div>
            @endif
            <h1 class="text-[22px] leading-snug font-bold text-fg-neutral">${notice.getTitle()}</h1>
            <p class="mt-x2 flex flex-wrap items-center gap-x-x2 gap-y-x1 t3-regular text-fg-neutral-subtle">
                <span class="tabular-nums">${Ui.dateTime(notice.publishedAt())}</span>
                @if(notice.getEndsAt() != null)<span class="tabular-nums">· ${Ui.dateTime(notice.getEndsAt())}까지 게시</span>@endif
                @if(canManage)
                    <span class="${Seed.badge("게시 중".equals(notice.statusAt(now)) ? "positive" : "neutral", "weak")}">${notice.statusAt(now)}</span>
                    @if(notice.isPopup())<span class="${Seed.badge("informative", "weak")}">안내 팝업</span>@endif
                @endif
            </p>
        </header>
        <div class="prose-md t5-regular" data-markdown>${notice.getBody()}</div>
    </article>
`)
