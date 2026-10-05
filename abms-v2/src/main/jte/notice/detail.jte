@import java.time.LocalDateTime
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.notice.Notice
@param ViewContext ctx
@param Notice notice
@param boolean canManage
@param LocalDateTime now
@template.layout.app(ctx = ctx, title = notice.getTitle(), content = @`
    @template.components.pageHeader(title = notice.getTitle(), breadcrumb = @`
        <a href="/notices" class="hover:text-fg-neutral-muted">공지사항</a>${Icons.of("chevron-right", "size-3")}<span>${notice.getTitle()}</span>
    `, actions = @`
        @if(canManage)
            <a href="/notices/${notice.id()}/edit" class="${Seed.button("neutralOutline")}">${Icons.of("pencil", "size-4")} 수정</a>
            <form method="post" action="/notices/${notice.id()}/delete" hx-confirm="이 공지를 삭제할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                <button class="${Seed.button("neutralOutline")} text-fg-critical">${Icons.of("trash", "size-4")} 삭제</button>
            </form>
        @endif
    `)
    <div class="mx-auto max-w-3xl">
        <div class="-mt-x3 mb-x6 flex flex-wrap items-center gap-x2 t3-regular text-fg-neutral-subtle">
            @if(notice.isPinned())<span class="inline-flex items-center gap-x1 text-fg-brand">${Icons.of("pin", "size-3.5")} 상단 고정</span>@endif
            @template.notice.importance(importance = notice.getImportance())
            <span class="tabular-nums">${Ui.dateTime(notice.publishedAt())}</span>
            @if(notice.getEndsAt() != null)<span class="tabular-nums">· ${Ui.dateTime(notice.getEndsAt())}까지 게시</span>@endif
            @if(canManage)
                <span class="${Seed.badge("게시 중".equals(notice.statusAt(now)) ? "positive" : "neutral", "weak")}">${notice.statusAt(now)}</span>
                @if(notice.isPopup())<span class="${Seed.badge("informative", "weak")}">안내 팝업</span>@endif
            @endif
        </div>
        <div class="prose-md t5-regular" data-markdown>${notice.getBody()}</div>
    </div>
`)
