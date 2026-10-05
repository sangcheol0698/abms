@param String photoUrl = null
@param String name = ""
@param String size = "size-9"
<%--
  아바타: 프로필 사진이 있으면 사진, 없으면 기본 아바타(사람 아이콘). 기본 아바타는 이미지 요청을 하지 않는다.
--%>
@if(photoUrl != null)
    <img src="${photoUrl}" alt="${name}" loading="lazy" decoding="async" class="${size} shrink-0 rounded-full bg-bg-neutral-weak object-cover">
@else
    <span class="inline-flex ${size} shrink-0 overflow-hidden rounded-full bg-palette-gray-300 text-palette-gray-500" role="img" aria-label="${name}">
        <svg viewBox="0 0 24 24" class="size-full" aria-hidden="true" fill="currentColor"><circle cx="12" cy="9.6" r="4.3"/><path d="M3.6 24c0-5 3.8-8.6 8.4-8.6s8.4 3.6 8.4 8.6z"/></svg>
    </span>
@endif
