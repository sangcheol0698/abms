@import java.time.LocalDateTime
@import java.util.Set
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.web.ViewContext
@import kr.co.abacus.abms.notice.Notice
@import kr.co.abacus.abms.notice.NoticeImportance
@import kr.co.abacus.abms.notice.NoticeSearch
@param ViewContext ctx
@param PageView<Notice> page
@param Set<Long> readIds
@param boolean canManage
@param LocalDateTime now
@param NoticeSearch search
@template.layout.app(ctx = ctx, title = "공지사항", fill = true, content = @`
<%-- 데스크톱: 페이지는 고정, 목록 본문만 스크롤 (fill 레이아웃). 태블릿·모바일은 문서 스크롤. --%>
<div class="flex flex-col lg:h-full">
    <div class="px-x4 pt-x5 sm:px-x6 lg:px-x8 lg:pt-x6">
    @template.components.pageHeader(title = "공지사항", subtitle = canManage ? "전체 사용자에게 알릴 내용을 게시합니다. 안내 팝업을 켜면 게시 기간 동안 화면을 열 때 팝업으로 보여줍니다." : "회사 공지와 안내를 확인합니다.", actions = @`
        @if(canManage)
            <a href="/notices/new" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}">${Icons.of("plus", "size-4")} 공지 등록</a>
        @endif
    `)
    </div>

    <div class="flex min-h-0 flex-1 flex-col border-t border-stroke-neutral-subtle">
        <form id="notice-filter" class="view-bar shrink-0 sm:px-x6 lg:px-x8" action="/notices" method="get"
              hx-get="/notices" hx-target="#notice-results" hx-select="#notice-results" hx-swap="outerHTML" hx-push-url="true"
              hx-trigger="input changed delay:300ms from:(#notice-filter input[name=q]), change from:(#notice-filter select), change from:(#notice-filter input[type=radio]), change from:(#notice-filter input[type=checkbox]), submit">
            <div class="flex flex-wrap items-center gap-x1">
                @if(canManage)
                    <div class="flex flex-wrap items-center gap-x1" role="radiogroup" aria-label="게시 상태">
                        <label class="chip"><input type="radio" name="status" value="" checked="${search.status() == null}">전체</label>
                        @for(NoticeSearch.Status s : NoticeSearch.Status.values())
                            <label class="chip"><input type="radio" name="status" value="${s.name()}" checked="${s == search.status()}">${s.label()}</label>
                        @endfor
                    </div>
                @endif
                <label class="chip"><input type="checkbox" name="unread" value="true" checked="${search.unreadOnly()}">안 읽은 공지만</label>
            </div>
            <div class="ml-auto flex flex-wrap items-center gap-x2">
                <div class="relative">
                    <span class="pointer-events-none absolute inset-y-0 left-2.5 flex items-center text-fg-placeholder">${Icons.of("search", "size-3.5")}</span>
                    <input type="search" name="q" value="${Ui.value(search.q())}" placeholder="제목·본문" class="input filter-input w-48 pl-x8" aria-label="검색어">
                </div>
                <select name="importance" class="select filter-select" aria-label="중요도">
                    <option value="">전체 중요도</option>
                    @for(NoticeImportance i : NoticeImportance.values())
                        <option value="${i.name()}" selected="${i == search.importance()}">${i.label()}</option>
                    @endfor
                </select>
            </div>
        </form>
        <div class="list-head shrink-0 sm:px-x6 lg:px-x8" aria-hidden="true">
            <span class="w-2 shrink-0"></span>
            <span class="min-w-0 flex-1">제목</span>
            @if(canManage)
                <span class="hidden w-16 @2xl:block">상태</span>
                <span class="hidden w-16 @2xl:block">팝업</span>
            @endif
            <span class="w-24 shrink-0 text-right">게시일</span>
        </div>
        <div id="notice-results" class="flex min-h-0 flex-1 flex-col">
        <div class="min-h-0 flex-1 lg:overflow-y-auto lg:overflow-x-hidden lg:overscroll-y-contain" data-fill-scroll>
            @if(page.isEmpty())
                @template.components.empty(message = search.isFiltered() ? "조건에 맞는 공지가 없습니다." : "등록된 공지가 없습니다.", icon = "megaphone")
            @else
                @for(Notice n : page.content())
                    !{boolean unread = !readIds.contains(n.id());}
                    <a href="/notices/${n.id()}" class="list-row sm:px-x6 lg:px-x8">
                        <span class="flex w-2 shrink-0 justify-center">@if(unread)<span class="size-1.5 rounded-full bg-bg-brand-solid" title="안 읽음"></span>@endif</span>
                        <span class="flex min-w-0 flex-1 items-center gap-x1_5">
                            @if(n.isPinned())${Icons.of("pin", "size-3.5 shrink-0 text-fg-brand")}@endif
                            @template.notice.importance(importance = n.getImportance())
                            <span class="truncate ${unread ? "t4-bold" : "t4-regular text-fg-neutral-muted"}">${n.getTitle()}</span>
                        </span>
                        @if(canManage)
                            !{String status = n.statusAt(now);}
                            <span class="hidden w-16 @2xl:block"><span class="${Seed.badge("게시 중".equals(status) ? "positive" : "neutral", "weak")}">${status}</span></span>
                            <span class="list-cell-muted hidden w-16 @2xl:block">${n.isPopup() ? "사용" : "-"}</span>
                        @endif
                        <span class="list-cell-muted w-24 shrink-0 text-right tabular-nums">${Ui.date(n.publishedAt() == null ? null : n.publishedAt().toLocalDate())}</span>
                    </a>
                @endfor
            @endif
        </div>
        <div class="shrink-0 bg-bg-layer-default sm:px-x2 lg:px-x4">@template.components.pagination(page = page, target = "body")</div>
        </div>
    </div>
</div>
`)
