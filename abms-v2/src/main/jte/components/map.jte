@import java.util.List
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.MapMarker
@param List<MapMarker> markers
@param boolean mapEnabled
@param String height = "h-64"
@param String fallbackHref = null
@param String fallbackText = null
<%--
  지도. 카카오 지도 키가 있으면 JS(map.js)가 data-map 의 마커를 그린다.
  키가 없거나 SDK 를 불러오지 못하면 안쪽의 대체 내용(주소·카카오맵 바로가기)이 그대로 남는다.
--%>
<div class="map-canvas ${mapEnabled && !markers.isEmpty() ? height : "h-36"}" data-map="${mapEnabled && !markers.isEmpty() ? MapMarker.json(markers) : null}" aria-label="지도">
    <div class="flex h-full flex-col items-center justify-center gap-x2 p-x4 text-center">
        <span class="flex size-x10 items-center justify-center rounded-full bg-bg-layer-default text-fg-neutral-subtle shadow-s1">${Icons.of("map", "size-5")}</span>
        @if(fallbackText != null)<p class="t4-medium text-fg-neutral">${fallbackText}</p>@endif
        <p class="t3-regular text-fg-neutral-subtle">
            @if(!mapEnabled)
                지도 키(KAKAO_JS_KEY)가 설정되지 않았습니다.
            @elseif(markers.isEmpty())
                지도 위치가 등록되지 않았습니다.
            @else
                지도를 불러오는 중…
            @endif
        </p>
        @if(fallbackHref != null)
            <a href="${fallbackHref}" target="_blank" rel="noopener noreferrer" class="link inline-flex items-center gap-x1 t3-medium">카카오맵에서 보기 ${Icons.of("external", "size-3.5")}</a>
        @endif
    </div>
</div>
