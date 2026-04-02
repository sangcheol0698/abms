package kr.co.abacus.abms.adapter.api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("CSRF API")
class CsrfApiTest extends ApiIntegrationTestBase {

    @Test
    @DisplayName("CSRF 토큰 초기화 엔드포인트를 문서화한다")
    void csrfInit() throws Exception {
        mockMvc.perform(get("/api/csrf"))
                .andDo(document("auth/csrf"))
                .andExpect(status().isOk());
    }

}
