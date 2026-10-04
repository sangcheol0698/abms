package kr.co.abacus.abms.common.geo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.common.web.MapProperties;

class GeocoderTest {

    private MockRestServiceServer server;
    private Geocoder geocoder;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        geocoder = new Geocoder(new MapProperties(null, "rest-key"), builder.build());
    }

    @Test
    void 좌표가_없는_주소는_카카오_로컬_API로_좌표와_우편번호를_채운다() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://dapi.kakao.com/v2/local/search/address.json?query=")))
                .andExpect(header("Authorization", "KakaoAK rest-key"))
                .andRespond(withSuccess("""
                        {"documents":[{"x":"126.9779451","y":"37.5662952","road_address":{"zone_no":"04524"}}]}
                        """, MediaType.APPLICATION_JSON));

        Location result = geocoder.complete(Location.of(null, "서울 중구 세종대로 110", "3층", null, null));

        assertThat(result.latitude()).isEqualByComparingTo("37.5662952");
        assertThat(result.longitude()).isEqualByComparingTo("126.9779451");
        assertThat(result.zipCode()).isEqualTo("04524");
        assertThat(result.addressDetail()).isEqualTo("3층");
        server.verify();
    }

    @Test
    void 이미_좌표가_있거나_주소가_없으면_호출하지_않는다() {
        Location located = Location.of(null, "어딘가", null, BigDecimal.ONE, BigDecimal.ONE);

        assertThat(geocoder.complete(located)).isSameAs(located);
        assertThat(geocoder.complete(Location.EMPTY)).isSameAs(Location.EMPTY);
        server.verify();
    }

    @Test
    void 호출에_실패하면_주소만_그대로_둔다() {
        server.expect(requestTo(org.hamcrest.Matchers.startsWith("https://dapi.kakao.com/"))).andRespond(withServerError());
        Location addressOnly = Location.of(null, "서울 중구 세종대로 110", null, null, null);

        assertThat(geocoder.complete(addressOnly)).isSameAs(addressOnly);
    }

    @Test
    void REST_키가_없으면_변환하지_않는다() {
        Geocoder disabled = new Geocoder(new MapProperties(null, null), RestClient.create());
        Location addressOnly = Location.of(null, "서울 중구 세종대로 110", null, null, null);

        assertThat(disabled.complete(addressOnly)).isSameAs(addressOnly);
    }

}
