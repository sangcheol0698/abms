package kr.co.abacus.abms.common.domain;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import org.jspecify.annotations.Nullable;

/**
 * 위치(주소 + 좌표). 모든 값은 선택이며, 주소가 없으면 전체를 비운다.
 * 좌표는 위도·경도가 함께 있을 때만 저장한다.
 */
@Embeddable
public record Location(
        @Column(length = 10) @Nullable String zipCode,
        @Nullable String address,
        @Column(length = 100) @Nullable String addressDetail,
        @Column(precision = 10, scale = 7) @Nullable BigDecimal latitude,
        @Column(precision = 10, scale = 7) @Nullable BigDecimal longitude
) {

    public static final Location EMPTY = new Location(null, null, null, null, null);

    /** 입력값을 정리해 만든다. (빈 문자열 → null, 주소가 없으면 빈 위치, 좌표 범위 검증) */
    public static Location of(@Nullable String zipCode, @Nullable String address, @Nullable String addressDetail,
                              @Nullable BigDecimal latitude, @Nullable BigDecimal longitude) {
        String addr = trim(address);
        if (addr == null) {
            return EMPTY;
        }
        if (addr.length() > 255) {
            throw new BusinessException("주소는 255자 이하로 입력하세요.");
        }
        String detail = trim(addressDetail);
        if (detail != null && detail.length() > 100) {
            throw new BusinessException("상세 주소는 100자 이하로 입력하세요.");
        }
        String zip = trim(zipCode);
        if (zip != null && !zip.matches("^[0-9-]{5,7}$")) {
            throw new BusinessException("우편번호 형식이 올바르지 않습니다: " + zipCode);
        }
        if (latitude == null || longitude == null) {
            return new Location(zip, addr, detail, null, null);
        }
        if (latitude.abs().compareTo(BigDecimal.valueOf(90)) > 0 || longitude.abs().compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BusinessException("좌표 범위가 올바르지 않습니다.");
        }
        return new Location(zip, addr, detail, latitude, longitude);
    }

    public boolean isEmpty() {
        return address == null;
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }

    /** 두 위치 사이의 직선 거리(km, 하버사인). 어느 한쪽이라도 좌표가 없으면 null. */
    public @Nullable Double distanceKm(Location other) {
        if (!hasCoordinates() || !other.hasCoordinates()) {
            return null;
        }
        double lat1 = Math.toRadians(latitude.doubleValue());
        double lat2 = Math.toRadians(other.latitude.doubleValue());
        double dLat = lat2 - lat1;
        double dLng = Math.toRadians(other.longitude.doubleValue() - longitude.doubleValue());
        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dLng / 2), 2);
        return 6371.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** 주소 + 상세 주소 */
    public String fullAddress() {
        if (address == null) {
            return "";
        }
        return addressDetail == null ? address : address + " " + addressDetail;
    }

    /** 카카오맵 바로가기 (좌표가 있으면 해당 지점, 없으면 주소 검색). API 키 없이 동작한다. */
    public String kakaoMapLink(String label) {
        if (hasCoordinates()) {
            return "https://map.kakao.com/link/map/" + encode(label) + "," + latitude.toPlainString() + "," + longitude.toPlainString();
        }
        return "https://map.kakao.com/link/search/" + encode(fullAddress());
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static @Nullable String trim(@Nullable String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

}
