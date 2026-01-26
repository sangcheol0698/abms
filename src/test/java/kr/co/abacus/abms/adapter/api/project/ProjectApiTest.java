package kr.co.abacus.abms.adapter.api.project;

import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectCreateApiRequest;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectUpdateApiRequest;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectFixture;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;
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

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("프로젝트 API (ProjectApi)")
class ProjectApiTest extends ApiIntegrationTestBase {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Test
    @DisplayName("프로젝트 생성")
    void create() {
        // Given
        ProjectCreateApiRequest request = new ProjectCreateApiRequest(
                1L,
                "PRJ-TEST-001",
                "테스트 프로젝트",
                "테스트 프로젝트 설명",
                ProjectStatus.SCHEDULED,
                100_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
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
        assertThat(response.statusDescription()).isEqualTo("예약");

        flushAndClear();
        Project savedProject = projectRepository.findById(response.projectId()).orElseThrow();
        assertThat(savedProject.getCode()).isEqualTo("PRJ-TEST-001");
    }

    @Test
    @DisplayName("프로젝트 생성 - 중복 코드")
    void create_duplicateCode() {
        // Given: 동일한 코드의 프로젝트가 이미 존재
        projectRepository.save(ProjectFixture.createProject("PRJ-DUP-001"));
        flushAndClear();

        ProjectCreateApiRequest request = new ProjectCreateApiRequest(
                99L,
                "PRJ-DUP-001",
                "중복 프로젝트",
                null,
                ProjectStatus.SCHEDULED,
                50_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30));
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
    @DisplayName("프로젝트 검색 - 코드/이름, 상태, 기간 조건")
    void search() {
        projectRepository.save(createProject("PRJ-ALPHA-001", "알파 프로젝트", 1L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2024, 1, 10)));
        projectRepository.save(createProject("PRJ-ALPHA-002", "알파 보조", 1L, ProjectStatus.COMPLETED,
                LocalDate.of(2024, 2, 5)));
        projectRepository.save(createProject("PRJ-BETA-001", "베타 프로젝트", 2L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2023, 5, 1)));
        flushAndClear();

        PageResponse<ProjectResponse> response = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/projects")
                        .queryParam("name", "알파")
                        .queryParam("statuses", ProjectStatus.IN_PROGRESS.name())
                        .queryParam("startDate", "2024-01-01")
                        .queryParam("endDate", "2024-12-31")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<ProjectResponse>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.content())
                .extracting(ProjectResponse::code)
                .containsExactly("PRJ-ALPHA-001");
    }

    @Test
    @DisplayName("프로젝트 상세 조회")
    void find() {
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
    void find_notFound() {
        // Given
        Long nonExistentId = 9999L;

        // When & Then
        restTestClient.get()
                .uri("/api/projects/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("프로젝트 수정")
    void update() {
        // Given
        Project project = ProjectFixture.createProject("PRJ-UPDATE-001");
        projectRepository.save(project);
        flushAndClear();

        ProjectUpdateApiRequest request = new ProjectUpdateApiRequest(
                99L,
                "수정된 프로젝트명",
                "수정된 설명",
                ProjectStatus.IN_PROGRESS.name(),
                150_000_000L,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 12, 31));
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
    void complete() {
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
    void cancel() {
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
    void delete() {
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
    void delete_notFound() {
        // Given
        Long nonExistentId = 9999L;

        // When & Then
        restTestClient.delete()
                .uri("/api/projects/{id}", nonExistentId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("프로젝트 엑셀 다운로드")
    void downloadExcel() {
        restTestClient.get()
                .uri("/api/projects/excel/download")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .expectHeader().valueMatches("Content-Disposition", "attachment; filename=\"projects_\\d{8}_\\d{6}\\.xlsx\"");
    }

    @Test
    @DisplayName("프로젝트 엑셀 샘플 다운로드")
    void downloadExcelSample() {
        restTestClient.get()
                .uri("/api/projects/excel/sample")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .expectHeader().valueMatches("Content-Disposition", "attachment; filename=\"projects_sample_\\d{8}_\\d{6}\\.xlsx\"");
    }

    @Test
    @DisplayName("프로젝트 엑셀 업로드")
    void uploadExcel() throws Exception {
        // Given: 협력사 생성
        String partyName = "네이버클라우드";
        kr.co.abacus.abms.domain.party.Party party = kr.co.abacus.abms.domain.party.Party.create(
                new kr.co.abacus.abms.domain.party.PartyCreateRequest(partyName, null, null, null, null));
        partyRepository.save(party);
        flushAndClear();

        byte[] excelBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Projects");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("협력사 이름");
            header.createCell(1).setCellValue("프로젝트 코드");
            header.createCell(2).setCellValue("프로젝트명");
            header.createCell(3).setCellValue("상태");
            header.createCell(4).setCellValue("계약금액");
            header.createCell(5).setCellValue("시작일");
            header.createCell(6).setCellValue("종료일");
            header.createCell(7).setCellValue("설명");

            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue(partyName);
            data.createCell(1).setCellValue("PRJ-UPLOAD-001");
            data.createCell(2).setCellValue("업로드 프로젝트");
            data.createCell(3).setCellValue("진행 중");
            data.createCell(4).setCellValue("50000000");
            data.createCell(5).setCellValue("2024-01-01");
            data.createCell(6).setCellValue("2024-12-31");
            data.createCell(7).setCellValue("업로드 테스트 설명");

            workbook.write(out);
            excelBytes = out.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelBytes);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/projects/excel/upload").file(file))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    private Project createProject(String code, String name, Long partyId, ProjectStatus status, LocalDate startDate) {
        return Project.create(new ProjectCreateRequest(
                partyId,
                code,
                name,
                "테스트 프로젝트 설명",
                status,
                100_000_000L,
                startDate,
                startDate.plusMonths(6)));
    }

}
