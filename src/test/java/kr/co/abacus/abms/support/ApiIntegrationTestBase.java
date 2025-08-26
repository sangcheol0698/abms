package kr.co.abacus.abms.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * API 통합 테스트를 위한 베이스 클래스
 * 
 * 기본 IntegrationTestBase 기능에 추가로:
 * - MockMvc 테스트 지원
 * - JSON 직렬화/역직렬화 지원
 * - Web API 테스트에 최적화된 환경 제공
 */
@AutoConfigureMockMvc
public abstract class ApiIntegrationTestBase extends IntegrationTestBase {

    @Autowired
    protected MockMvcTester mvcTester;

    @Autowired
    protected ObjectMapper objectMapper;

}