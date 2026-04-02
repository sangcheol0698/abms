package kr.co.abacus.abms.support;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.snippet.Snippet;
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
@ExtendWith(RestDocumentationExtension.class)
public abstract class ApiIntegrationTestBase extends IntegrationTestBase {

    @Autowired
    protected ObjectMapper objectMapper;

    protected RestTestClient restTestClient;
    protected MockMvc mockMvc;

    @BeforeEach
    void setUpRestTestClient(
            WebApplicationContext applicationContext,
            RestDocumentationContextProvider restDocumentation
    ) {
        restTestClient = RestTestClient.bindToApplicationContext(applicationContext).build();
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(documentationConfiguration(restDocumentation)
                        .operationPreprocessors()
                        .withRequestDefaults(prettyPrint())
                        .withResponseDefaults(prettyPrint()))
                .apply(springSecurity())
                .defaultRequest(get("/").with(csrf().asHeader()))
                .build();
    }

    protected RestDocumentationResultHandler document(String identifier, Snippet... snippets) {
        return org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document(
                identifier,
                preprocessRequest(prettyPrint()),
                preprocessResponse(prettyPrint()),
                snippets
        );
    }

}
