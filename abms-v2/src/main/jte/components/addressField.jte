@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.Ui
@param String zipCode = null
@param String address = null
@param String addressDetail = null
@param Object latitude = null
@param Object longitude = null
@param FormErrors errors
@param boolean mapEnabled = false
<%--
  주소 입력 (선택). '주소 검색'은 Daum 우편번호 서비스(키 불필요)를 쓰고,
  카카오 지도 키가 있으면 고른 주소의 좌표를 찾아 hidden 필드(latitude/longitude)에 채운다.
--%>
<div class="grid grid-cols-1 gap-x3" data-address-field>
    <div>
        <label for="address" class="label">주소</label>
        <div class="flex gap-x2">
            <input id="zipCode" name="zipCode" value="${Ui.value(zipCode)}" placeholder="우편번호" aria-label="우편번호" maxlength="7"
                   class="input w-24 shrink-0 tabular-nums ${errors.has("zipCode") ? "input-error" : ""}" autocomplete="off" data-address-zip>
            <input id="address" name="address" value="${Ui.value(address)}" placeholder="도로명 또는 지번 주소" maxlength="255"
                   class="input min-w-0 flex-1 ${errors.has("address") ? "input-error" : ""}" autocomplete="off" data-address-main>
            <button type="button" class="${Seed.button("neutralOutline")} shrink-0" data-address-search>${Icons.of("search", "size-4")} 주소 검색</button>
        </div>
        @if(errors.has("zipCode"))<p class="field-error">${errors.get("zipCode")}</p>@endif
        @if(errors.has("address"))<p class="field-error">${errors.get("address")}</p>@endif
    </div>
    <div>
        <label for="addressDetail" class="sr-only">상세 주소</label>
        <input id="addressDetail" name="addressDetail" value="${Ui.value(addressDetail)}" placeholder="상세 주소 (동·층·호)" maxlength="100"
               class="input ${errors.has("addressDetail") ? "input-error" : ""}" autocomplete="off" data-address-detail>
        @if(errors.has("addressDetail"))<p class="field-error">${errors.get("addressDetail")}</p>@endif
    </div>
    <input type="hidden" name="latitude" value="${Ui.value(latitude)}" data-address-lat>
    <input type="hidden" name="longitude" value="${Ui.value(longitude)}" data-address-lng>
    <p class="field-help flex items-center gap-x1" data-address-status>
        ${Icons.of("map-pin", "size-3.5 shrink-0")}
        <span data-address-status-text>
            @if(latitude != null && longitude != null)
                지도 위치가 저장되어 있습니다.
            @elseif(mapEnabled)
                주소를 고르면 지도 위치를 자동으로 찾습니다.
            @else
                지도 키가 설정되지 않아 주소만 저장합니다.
            @endif
        </span>
    </p>
    @if(mapEnabled)
        <div class="map-canvas h-48 hidden" data-address-preview aria-label="위치 미리보기"></div>
    @endif
</div>
