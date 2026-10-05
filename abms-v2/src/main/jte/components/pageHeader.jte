@import gg.jte.Content
@param String title
@param String subtitle = null
@param Content actions = null
@param Content breadcrumb = null
@param Content leading = null
@param boolean showTitle = true
@param String readOnly = null
<%--
  본문 패널 맨 위에 붙는 고정 툴바. 본문을 스크롤해도 경로(또는 제목)와 주요 동작이 남는다.
  - breadcrumb 이 있으면(상세 화면) 툴바에는 경로, 큰 제목은 그 아래 본문에 둔다.
  - 없으면(목록 화면) 툴바에 제목을 둔다.
  - readOnly: 수정 권한이 없어 편집 버튼이 없는 화면이면 그 이유. '보기 전용' 표시를 띄운다.
--%>
<div data-page-toolbar class="sticky top-12 z-20 -mx-4 -mt-5 flex min-h-13 flex-wrap items-center gap-x-x3 gap-y-x2 border-b border-transparent bg-bg-layer-default px-4 py-x2 sm:-mx-6 sm:px-6 lg:top-0 lg:-mx-8 lg:-mt-6 lg:px-8">
    <div class="min-w-0 flex-1">
        @if(breadcrumb != null)
            <nav aria-label="경로" class="flex min-w-0 items-center gap-x1 t4-regular whitespace-nowrap text-fg-neutral-subtle [&>a]:hover:text-fg-neutral [&>span:last-child]:truncate [&>span:last-child]:text-fg-neutral">${breadcrumb}</nav>
        @else
            <h1 class="truncate t5-bold text-fg-neutral">${title}</h1>
        @endif
    </div>
    @if(readOnly != null)
        @template.components.readOnly(reason = readOnly)
    @endif
    @if(actions != null)
        <div class="flex shrink-0 flex-wrap items-center gap-x2">${actions}</div>
    @endif
</div>
@if(showTitle)
<div class="mb-x5 mt-x1">
    @if(breadcrumb != null)
        @if(leading != null)
            <div class="flex items-center gap-x3">${leading}<h1 class="min-w-0 truncate t7-bold text-fg-neutral">${title}</h1></div>
        @else
            <h1 class="t7-bold text-fg-neutral">${title}</h1>
        @endif
    @endif
    @if(subtitle != null)
        <p class="mt-x1 t4-regular text-fg-neutral-subtle">${subtitle}</p>
    @endif
</div>
@endif
