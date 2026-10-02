@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Icons
@param PageView<?> page
@param String target
@if(page.totalPages() > 1 || page.totalElements() > 0)
    <div class="flex items-center justify-between border-t border-slate-100 px-4 py-3 text-sm">
        <p class="text-slate-500">총 <span class="font-medium text-slate-700">${page.totalElements()}</span>건 중 ${page.firstIndex()}–${page.lastIndex()}</p>
        @if(page.totalPages() > 1)
            <nav class="flex items-center gap-1" hx-target="${target}" hx-push-url="true">
                @if(page.hasPrevious())
                    <a class="btn btn-ghost btn-sm" href="${page.urlFor(page.page() - 1)}" hx-get="${page.urlFor(page.page() - 1)}">${Icons.of("chevron-left", "size-4")}</a>
                @endif
                @for(int number : page.pageNumbers())
                    <a class="btn btn-sm ${number == page.page() ? "btn-primary" : "btn-ghost"}" href="${page.urlFor(number)}" hx-get="${page.urlFor(number)}">${number + 1}</a>
                @endfor
                @if(page.hasNext())
                    <a class="btn btn-ghost btn-sm" href="${page.urlFor(page.page() + 1)}" hx-get="${page.urlFor(page.page() + 1)}">${Icons.of("chevron-right", "size-4")}</a>
                @endif
            </nav>
        @endif
    </div>
@endif
