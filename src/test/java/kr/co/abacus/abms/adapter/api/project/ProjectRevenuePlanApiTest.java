package kr.co.abacus.abms.adapter.api.project;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectCreateApiRequest;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectRevenuePlanResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectUpdateApiRequest;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectFixture;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.project.RevenueType;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("프로젝트 매출 계획 API (ProjectRevenuePlanApi)")
class ProjectRevenuePlanApiTest extends ApiIntegrationTestBase {

    @Autowired
    private ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Test
    @DisplayName("프로젝트 매출 계획 생성")
    void create() {
        // Given
        ProjectRevenuePlanCreateRequest request = new ProjectRevenuePlanCreateRequest(
                1L,
                1,
                LocalDate.of(2024, 1, 15),
                RevenueType.DOWN_PAYMENT,
                180000000L,
                "메모");
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        ProjectRevenuePlanResponse response = restTestClient.post()
                .uri("/api/projectRevenuePlans")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestJson)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProjectRevenuePlanResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.projectId()).isEqualTo(1L);
        assertThat(response.sequence()).isEqualTo(1);

        flushAndClear();

        // ProjectRevenuePlan savedProjectRevenuePlan = projectRevenuePlanRepository.findByProjectIdAndSequence(response.projectId(), response.sequence());
        // assertThat(savedProjectRevenuePlan.getProjectId()).isEqualTo(1L);
    }

}
