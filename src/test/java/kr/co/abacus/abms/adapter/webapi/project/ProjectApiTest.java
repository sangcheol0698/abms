package kr.co.abacus.abms.adapter.webapi.project;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import kr.co.abacus.abms.adapter.webapi.project.dto.ProjectCreateApiRequest;
import kr.co.abacus.abms.adapter.webapi.project.dto.ProjectResponse;
import kr.co.abacus.abms.adapter.webapi.project.dto.ProjectUpdateApiRequest;
import kr.co.abacus.abms.application.project.required.ProjectRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectFixture;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

class ProjectApiTest extends ApiIntegrationTestBase {

    @Autowired
    private ProjectRepository projectRepository;

    private UUID partyId;

    @BeforeEach
    void setUp() {
        partyId = UUID.randomUUID();
    }

    @Test
    @DisplayName("프로젝트 생성")
    void create() throws Exception {
        // Given
        ProjectCreateApiRequest request = new ProjectCreateApiRequest(
            partyId,
            "PRJ-TEST-001",
            "테스트 프로젝트",
            "테스트 프로젝트 설명",
            ProjectStatus.SCHEDULED,
            100_000_000L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        ProjectResponse response = restTestClient.post()
            .uri("/api/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestJson)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProjectResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.projectId()).isNotNull();
        assertThat(response.code()).isEqualTo("PRJ-TEST-001");
        assertThat(response.name()).isEqualTo("테스트 프로젝트");
        assertThat(response.statusDescription()).isEqualTo("계획");

        flushAndClear();
        Project savedProject = projectRepository.findById(response.projectId()).orElseThrow();
        assertThat(savedProject.getCode()).isEqualTo("PRJ-TEST-001");
    }

    @Test
    @DisplayName("프로젝트 생성 - 중복 코드")
    void create_duplicateCode() throws Exception {
        // Given: 동일한 코드의 프로젝트가 이미 존재
        projectRepository.save(ProjectFixture.createProject("PRJ-DUP-001"));
        flushAndClear();

        ProjectCreateApiRequest request = new ProjectCreateApiRequest(
            partyId,
            "PRJ-DUP-001",
            "중복 프로젝트",
            null,
            ProjectStatus.SCHEDULED,
            50_000_000L,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 6, 30)
        );
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        restTestClient.post()
            .uri("/api/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestJson)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("프로젝트 목록 조회 - 페이징")
    void list_withPaging() throws Exception {
        // Given: 15개의 프로젝트 생성
        for (int i = 1; i <= 15; i++) {
            Project project = ProjectFixture.createProject("PRJ-LIST-" + String.format("%03d", i));
            projectRepository.save(project);
        }
        flushAndClear();

        // When & Then: 첫 번째 페이지 (size=10)
        restTestClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/api/projects")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .build())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.content.length()").isEqualTo(10)
            .jsonPath("$.totalElements").isEqualTo(15)
            .jsonPath("$.totalPages").isEqualTo(2)
            .jsonPath("$.last").isEqualTo(false);
    }

    @Test
    @DisplayName("프로젝트 상세 조회")
    void find() throws Exception {
        // Given
        Project project = ProjectFixture.createProject("PRJ-FIND-001");
        projectRepository.save(project);
        flushAndClear();

        // When & Then
        ProjectResponse response = restTestClient.get()
            .uri("/api/projects/{id}", project.getId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProjectResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.projectId()).isEqualTo(project.getId());
        assertThat(response.code()).isEqualTo("PRJ-FIND-001");
        assertThat(response.name()).isEqualTo("테스트 프로젝트");
    }

    @Test
    @DisplayName("프로젝트 상세 조회 - 존재하지 않는 프로젝트")
    void find_notFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        restTestClient.get()
            .uri("/api/projects/{id}", nonExistentId)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("프로젝트 수정")
    void update() throws Exception {
        // Given
        Project project = ProjectFixture.createProject("PRJ-UPDATE-001");
        projectRepository.save(project);
        flushAndClear();

        ProjectUpdateApiRequest request = new ProjectUpdateApiRequest(
            partyId,
            "수정된 프로젝트명",
            "수정된 설명",
            ProjectStatus.IN_PROGRESS.name(),
            150_000_000L,
            LocalDate.of(2024, 2, 1),
            LocalDate.of(2024, 12, 31)
        );
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        ProjectResponse response = restTestClient.put()
            .uri("/api/projects/{id}", project.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .body(requestJson)
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProjectResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("수정된 프로젝트명");
        assertThat(response.statusDescription()).isEqualTo("진행 중");

        flushAndClear();
        Project updatedProject = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(updatedProject.getName()).isEqualTo("수정된 프로젝트명");
    }

    @Test
    @DisplayName("프로젝트 완료 처리")
    void complete() throws Exception {
        // Given
        Project project = ProjectFixture.createProject("PRJ-COMPLETE-001");
        projectRepository.save(project);
        flushAndClear();

        // When & Then
        ProjectResponse response = restTestClient.patch()
            .uri("/api/projects/{id}/complete", project.getId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProjectResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.statusDescription()).isEqualTo("완료");

        flushAndClear();
        Project completedProject = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(completedProject.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    @DisplayName("프로젝트 취소 처리")
    void cancel() throws Exception {
        // Given
        Project project = ProjectFixture.createProject("PRJ-CANCEL-001");
        projectRepository.save(project);
        flushAndClear();

        // When & Then
        ProjectResponse response = restTestClient.patch()
            .uri("/api/projects/{id}/cancel", project.getId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(ProjectResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.statusDescription()).isEqualTo("취소");

        flushAndClear();
        Project cancelledProject = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(cancelledProject.getStatus()).isEqualTo(ProjectStatus.CANCELLED);
    }

    @Test
    @DisplayName("프로젝트 삭제 (soft delete)")
    void delete() throws Exception {
        // Given
        Project project = ProjectFixture.createProject("PRJ-DELETE-001");
        projectRepository.save(project);
        flushAndClear();

        // When & Then
        restTestClient.delete()
            .uri("/api/projects/{id}", project.getId())
            .exchange()
            .expectStatus().isNoContent();

        flushAndClear();
        assertThat(projectRepository.findByIdAndDeletedFalse(project.getId())).isEmpty();
    }

    @Test
    @DisplayName("프로젝트 삭제 - 존재하지 않는 프로젝트")
    void delete_notFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        restTestClient.delete()
            .uri("/api/projects/{id}", nonExistentId)
            .exchange()
            .expectStatus().isNotFound();
    }

}
