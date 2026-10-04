@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@param PageView<?> page
@param String target
@if(page.totalPages() > 1 || page.totalElements() > 0)
    <div class="flex items-center justify-between border-t border-stroke-neutral-subtle px-x4 py-x3 t4-regular">
        <p class="text-fg-neutral-subtle">총 <span class="t4-bold text-fg-neutral">${page.totalElements()}</span>건 중 ${page.firstIndex()}–${page.lastIndex()}</p>
        @if(page.totalPages() > 1)
            <nav class="flex items-center gap-x1" hx-target="${target}" hx-push-url="true">
                @if(page.hasPrevious())
                    <a class="${Seed.button("ghost", "xsmall")}" href="${page.urlFor(page.page() - 1)}" hx-get="${page.urlFor(page.page() - 1)}">${Icons.of("chevron-left", "size-4")}</a>
                @endif
                @for(int number : page.pageNumbers())
                    <a class="${Seed.button(number == page.page() ? "neutralSolid" : "ghost", "xsmall")} min-w-x8" href="${page.urlFor(number)}" hx-get="${page.urlFor(number)}">${number + 1}</a>
                @endfor
                @if(page.hasNext())
                    <a class="${Seed.button("ghost", "xsmall")}" href="${page.urlFor(page.page() + 1)}" hx-get="${page.urlFor(page.page() + 1)}">${Icons.of("chevron-right", "size-4")}</a>
                @endif
            </nav>
        @endif
    </div>
@endif
