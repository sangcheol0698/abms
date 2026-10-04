package kr.co.abacus.abms.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Location;

class LocationTest {

    private static final BigDecimal CITY_HALL_LAT = new BigDecimal("37.5662952");
    private static final BigDecimal CITY_HALL_LNG = new BigDecimal("126.9779451");

    @Test
    void 주소가_비어_있으면_나머지_값도_버리고_빈_위치가_된다() {
        Location location = Location.of("04524", "  ", "3층", CITY_HALL_LAT, CITY_HALL_LNG);

        assertThat(location).isEqualTo(Location.EMPTY);
        assertThat(location.isEmpty()).isTrue();
        assertThat(location.fullAddress()).isEmpty();
    }

    @Test
    void 값을_다듬고_상세_주소를_이어_붙인다() {
        Location location = Location.of(" 04524 ", " 서울 중구 세종대로 110 ", " 3층 ", null, null);

        assertThat(location.zipCode()).isEqualTo("04524");
        assertThat(location.fullAddress()).isEqualTo("서울 중구 세종대로 110 3층");
        assertThat(location.hasCoordinates()).isFalse();
    }

    @Test
    void 위도와_경도_중_하나만_있으면_좌표를_저장하지_않는다() {
        Location location = Location.of(null, "서울 중구 세종대로 110", null, CITY_HALL_LAT, null);

        assertThat(location.latitude()).isNull();
        assertThat(location.longitude()).isNull();
    }

    @Test
    void 좌표_범위와_우편번호_형식을_검증한다() {
        assertThatThrownBy(() -> Location.of(null, "어딘가", null, new BigDecimal("91"), CITY_HALL_LNG))
                .isInstanceOf(BusinessException.class).hasMessageContaining("좌표");
        assertThatThrownBy(() -> Location.of("abc", "어딘가", null, null, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("우편번호");
    }

    @Test
    void 두_지점의_직선_거리를_계산한다() {
        Location cityHall = Location.of(null, "서울시청", null, CITY_HALL_LAT, CITY_HALL_LNG);
        Location pangyo = Location.of(null, "판교역", null, new BigDecimal("37.3947611"), new BigDecimal("127.1111361"));

        assertThat(cityHall.distanceKm(pangyo)).isCloseTo(22.4, within(0.5));
        assertThat(cityHall.distanceKm(Location.of(null, "좌표 없음", null, null, null))).isNull();
    }

    @Test
    void 카카오맵_링크는_좌표가_있으면_지점을_없으면_주소_검색을_연다() {
        Location withCoords = Location.of(null, "서울 중구 세종대로 110", null, CITY_HALL_LAT, CITY_HALL_LNG);
        Location addressOnly = Location.of(null, "서울 중구 세종대로 110", null, null, null);

        assertThat(withCoords.kakaoMapLink("서울 시청")).isEqualTo("https://map.kakao.com/link/map/%EC%84%9C%EC%9A%B8%20%EC%8B%9C%EC%B2%AD,37.5662952,126.9779451");
        assertThat(addressOnly.kakaoMapLink("무시")).startsWith("https://map.kakao.com/link/search/").doesNotContain("+");
    }

}
