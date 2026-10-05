@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.notice.Notice
@param Notice notice
<%-- 안내 팝업 (모달). 닫기 선택은 서버에 기록하고, 다음 팝업이 있으면 이어서 보여준다. --%>
<div data-notice-popup="${notice.id()}">
    <div class="flex h-12 items-center gap-x2 border-b border-stroke-neutral-subtle pr-x2 pl-x5">
        ${Icons.of("megaphone", "size-4 shrink-0 text-fg-brand")}
        <span class="t4-regular text-fg-neutral-subtle">공지</span>
        @template.notice.importance(importance = notice.getImportance())
        <span class="ml-auto t3-regular text-fg-neutral-subtle tabular-nums">${Ui.date(notice.publishedAt() == null ? null : notice.publishedAt().toLocalDate())}</span>
        <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" aria-label="닫기" title="닫기"
                hx-post="/notices/${notice.id()}/popup/dismiss?mode=close" hx-target="#modal-body">${Icons.of("x", "size-4")}</button>
    </div>
    <div class="px-x5 py-x4">
        <h2 class="t6-bold text-fg-neutral">${notice.getTitle()}</h2>
        <div class="prose-md mt-x3 max-h-[50vh] overflow-y-auto t4-regular" data-markdown>${notice.getBody()}</div>
    </div>
    <div class="flex flex-wrap items-center gap-x2 border-t border-stroke-neutral-subtle px-x5 py-x3">
        <button type="button" class="${Seed.button("ghost", "small")} text-fg-neutral-subtle" hx-post="/notices/${notice.id()}/popup/dismiss?mode=forever" hx-target="#modal-body">다시 보지 않기</button>
        <button type="button" class="${Seed.button("ghost", "small")} text-fg-neutral-subtle" hx-post="/notices/${notice.id()}/popup/dismiss?mode=today" hx-target="#modal-body">오늘 하루 보지 않기</button>
        <a href="/notices/${notice.id()}" class="${Seed.button("neutralOutline", "small")} ml-auto">자세히</a>
        <button type="button" class="${Seed.button("brandSolid", "small")}" hx-post="/notices/${notice.id()}/popup/dismiss?mode=close" hx-target="#modal-body" autofocus>닫기</button>
    </div>
</div>
