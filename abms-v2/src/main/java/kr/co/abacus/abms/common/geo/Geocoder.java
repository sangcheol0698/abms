package kr.co.abacus.abms.common.geo;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.common.web.MapProperties;

/**
 * 주소 → 좌표 변환 (카카오 로컬 API). REST 키가 없거나 호출에 실패하면 위치를 그대로 돌려준다.
 * 좌표 없이 주소만 있는 위치를 저장할 때 서버에서 한 번 더 좌표를 채우는 용도다.
 */
@Component
public class Geocoder {

    private static final Logger log = LoggerFactory.getLogger(Geocoder.class);

    private final MapProperties properties;
    private final RestClient client;

    @Autowired
    public Geocoder(MapProperties properties, RestClient.Builder builder) {
        this(properties, builder.requestFactory(timeouts()).build());
    }

    Geocoder(MapProperties properties, RestClient client) {
        this.properties = properties;
        this.client = client;
    }

    private static SimpleClientHttpRequestFactory timeouts() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));
        return requestFactory;
    }

    /** 주소는 있는데 좌표가 없으면 좌표(와 비어 있는 우편번호)를 채운다. */
    public Location complete(@Nullable Location location) {
        if (location == null || location.isEmpty() || location.hasCoordinates() || !properties.geocodingEnabled()) {
            return location == null ? Location.EMPTY : location;
        }
        try {
            AddressResponse response = client.get()
                    .uri(uri -> uri.scheme("https").host("dapi.kakao.com").path("/v2/local/search/address.json").queryParam("query", location.address()).queryParam("size", 1).build())
                    .header("Authorization", "KakaoAK " + properties.kakaoRestKey())
                    .retrieve()
                    .body(AddressResponse.class);
            if (response == null || response.documents() == null || response.documents().isEmpty()) {
                return location;
            }
            Document first = response.documents().getFirst();
            String zipCode = location.zipCode() != null ? location.zipCode()
                    : first.road_address() == null ? null : blankToNull(first.road_address().zone_no());
            return Location.of(zipCode, location.address(), location.addressDetail(), new BigDecimal(first.y()), new BigDecimal(first.x()));
        } catch (RuntimeException e) {
            log.warn("주소 좌표 변환 실패: {} ({})", location.address(), e.getMessage());
            return location;
        }
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value;
    }

    record AddressResponse(@Nullable List<Document> documents) {
    }

    record Document(String x, String y, @Nullable RoadAddress road_address) {
    }

    record RoadAddress(@Nullable String zone_no) {
    }

}
