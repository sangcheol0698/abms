@import kr.co.abacus.abms.common.web.Icons
@param String photoUrl = null
@param String name = ""
@param String size = "size-9"
<%--
  아바타: 프로필 사진이 있으면 사진, 없으면 기본 아바타(사람 아이콘). 기본 아바타는 이미지 요청을 하지 않는다.
--%>
@if(photoUrl != null)
    <img src="${photoUrl}" alt="${name}" loading="lazy" decoding="async" class="${size} shrink-0 rounded-full bg-bg-neutral-weak object-cover">
@else
    <span class="inline-flex ${size} shrink-0 items-center justify-center overflow-hidden rounded-full bg-bg-neutral-weak text-fg-neutral-subtle" role="img" aria-label="${name}">
        ${Icons.of("user", "size-[62%]")}
    </span>
@endif
