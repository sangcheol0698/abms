package kr.co.abacus.abms.support;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import tools.jackson.databind.ObjectMapper;

/**
 * API 통합 테스트를 위한 베이스 클래스
 * <p>
 * 기본 IntegrationTestBase 기능에 추가로:
 * - MockMvc 테스트 지원
 * - JSON 직렬화/역직렬화 지원
 * - Web API 테스트에 최적화된 환경 제공
 */
public abstract class ApiIntegrationTestBase extends IntegrationTestBase {

    @Autowired
    protected ObjectMapper objectMapper;

    protected RestTestClient restTestClient;
    protected MockMvc mockMvc;

    @BeforeEach
    void setUpRestTestClient(WebApplicationContext applicationContext) {
        restTestClient = RestTestClient.bindToApplicationContext(applicationContext).build();
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

}