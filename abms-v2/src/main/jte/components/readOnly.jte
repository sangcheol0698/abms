@import kr.co.abacus.abms.common.web.Icons
@param String reason
<%-- 수정 권한이 없어 편집 버튼이 빠진 화면임을 알린다. 이유는 마우스를 올리거나 스크린리더로 읽힌다. --%>
<span class="inline-flex shrink-0 items-center gap-x1 rounded-full bg-bg-neutral-weak px-x2 py-x0_5 t3-medium text-fg-neutral-muted" title="${reason}" data-read-only>
    ${Icons.of("eye", "size-3.5")}보기 전용<span class="sr-only">: ${reason}</span>
</span>
