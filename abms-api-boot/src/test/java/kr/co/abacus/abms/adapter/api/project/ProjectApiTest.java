package kr.co.abacus.abms.adapter.api.project;

import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectCreateApiRequest;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectCreateResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectDetailResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectUpdateApiRequest;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectUpdateResponse;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.dto.ProjectOverviewSummary;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.grouppermissiongrant.GroupPermissionGrant;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectFixture;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Map;

@DisplayName("프로젝트 API (ProjectApi)")
class ProjectApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "project-api-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @Autowired
    private GroupPermissionGrantRepository groupPermissionGrantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PartyRepository partyRepository;

    @BeforeEach
    void setUpAccount() {
        Department department = departmentRepository.save(Department.create(
                "PROJECT-API",
                "프로젝트API부서",
                DepartmentType.TEAM,
                null,
                null
        ));
        Employee employee = employeeRepository.save(Employee.create(
                department.getIdOrThrow(),
                "프로젝트API사용자",
                USERNAME,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 5, 20),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        Account account = accountRepository.save(Account.create(
                employee.getIdOrThrow(),
                USERNAME,
                passwordEncoder.encode(PASSWORD)
        ));

        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "프로젝트 API 권한 그룹",
                "프로젝트 API 테스트용 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));

        grant(permissionGroup, "project.read", "프로젝트 조회");
        grant(permissionGroup, "project.write", "프로젝트 변경");
        grant(permissionGroup, "project.excel.download", "프로젝트 엑셀 다운로드");
        grant(permissionGroup, "project.excel.upload", "프로젝트 엑셀 업로드");
        grant(permissionGroup, "party.read", "협력사 조회");
        flushAndClear();
    }

    @Test
    @DisplayName("프로젝트 생성")
    void create() throws Exception {
        // Given
        Long partyId = createParty("테스트 협력사 A");
        Long leadDepartmentId = createLeadDepartment("프로젝트 생성 부서");
        ProjectCreateApiRequest request = new ProjectCreateApiRequest(
                partyId,
                leadDepartmentId,
                "PRJ-TEST-001",
                "테스트 프로젝트",
                "테스트 프로젝트 설명",
                ProjectStatus.SCHEDULED,
                100_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(document("project/create",
                        requestBody(),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        ProjectCreateResponse response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ProjectCreateResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.projectId()).isNotNull();

        flushAndClear();
        Project savedProject = projectRepository.findById(response.projectId()).orElseThrow();
        assertThat(savedProject.getCode()).isEqualTo("PRJ-TEST-001");
        assertThat(savedProject.getName()).isEqualTo("테스트 프로젝트");
        assertThat(savedProject.getStatus()).isEqualTo(ProjectStatus.SCHEDULED);
    }

    @Test
    @DisplayName("프로젝트 생성 - 중복 코드")
    void create_duplicateCode() throws Exception {
        // Given: 동일한 코드의 프로젝트가 이미 존재
        projectRepository.save(ProjectFixture.createProject("PRJ-DUP-001"));
        flushAndClear();

        ProjectCreateApiRequest request = new ProjectCreateApiRequest(
                99L,
                1L,
                "PRJ-DUP-001",
                "중복 프로젝트",
                null,
                ProjectStatus.SCHEDULED,
                50_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30));
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.post("/api/projects")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("프로젝트 검색 - 코드/이름, 상태, 기간 조건")
    void search() throws Exception {
        Long partyAlphaId = createParty("테스트 협력사 Alpha");
        Long partyBetaId = createParty("테스트 협력사 Beta");
        projectRepository.save(createProject("PRJ-ALPHA-001", "알파 프로젝트", partyAlphaId, 1L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2024, 1, 10)));
        projectRepository.save(createProject("PRJ-ALPHA-002", "알파 보조", partyAlphaId, 1L, ProjectStatus.COMPLETED,
                LocalDate.of(2024, 2, 5)));
        projectRepository.save(createProject("PRJ-BETA-001", "베타 프로젝트", partyBetaId, 1L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2023, 5, 1)));
        flushAndClear();

        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/projects")
                        .session(session)
                        .param("name", "알파")
                        .param("statuses", ProjectStatus.IN_PROGRESS.name())
                        .param("periodStart", "2024-01-01")
                        .param("periodEnd", "2024-12-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(document("project/search",
                        queryParameters(
                                parameterWithName("name").description("프로젝트명 검색어").optional(),
                                parameterWithName("statuses").description("프로젝트 상태 목록").optional(),
                                parameterWithName("periodStart").description("조회 시작일").optional(),
                                parameterWithName("periodEnd").description("조회 종료일").optional(),
                                parameterWithName("page").description("조회할 페이지 번호"),
                                parameterWithName("size").description("페이지 크기")
                        ),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].code").value("PRJ-ALPHA-001"));
    }

    @Test
    @DisplayName("프로젝트 요약 정보를 조회한다")
    void overviewSummary() throws Exception {
        Long partyId = createParty("요약 협력사");
        projectRepository.save(createProject("PRJ-SUM-API-001", "요약 프로젝트 1", partyId, 1L, ProjectStatus.SCHEDULED,
                LocalDate.of(2024, 1, 10)));
        projectRepository.save(createProject("PRJ-SUM-API-002", "요약 프로젝트 2", partyId, 1L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2024, 2, 10)));
        projectRepository.save(createProject("PRJ-SUM-API-003", "요약 프로젝트 3", partyId, 1L, ProjectStatus.COMPLETED,
                LocalDate.of(2024, 3, 10)));
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/projects/summary")
                        .session(session)
                        .param("name", "요약 프로젝트")
                        .param("partyIds", String.valueOf(partyId)))
                .andDo(document("project/summary",
                        queryParameters(
                                parameterWithName("name").description("프로젝트명 검색어").optional(),
                                parameterWithName("partyIds").description("협력사 ID 목록").optional()
                        ),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        ProjectOverviewSummary response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ProjectOverviewSummary.class);

        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isEqualTo(3);
        assertThat(response.scheduledCount()).isEqualTo(1);
        assertThat(response.inProgressCount()).isEqualTo(1);
        assertThat(response.completedCount()).isEqualTo(1);
        assertThat(response.totalContractAmount()).isEqualTo(300_000_000L);
    }

    // @Test
    // @DisplayName("프로젝트 상세 조회")
    // void find() {
    //     // Given
    //     Project project = ProjectFixture.createProject("PRJ-FIND-001");
    //     projectRepository.save(project);
    //     flushAndClear();
    //
    //     // When & Then
    //     ProjectDetailResponse response = restTestClient.get()
    //             .uri("/api/projects/{id}", project.getId())
    //             .exchange()
    //             .expectStatus().isOk()
    //             .expectBody(ProjectDetailResponse.class)
    //             .returnResult()
    //             .getResponseBody();
    //
    //     assertThat(response).isNotNull();
    //     assertThat(response.projectId()).isEqualTo(project.getId());
    //     assertThat(response.code()).isEqualTo("PRJ-FIND-001");
    //     assertThat(response.name()).isEqualTo("테스트 프로젝트");
    // }

    @Test
    @DisplayName("프로젝트 상세 조회 - 존재하지 않는 프로젝트")
    void find_notFound() throws Exception {
        // Given
        Long nonExistentId = 9999L;

        // When & Then
        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}", nonExistentId).session(session))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("프로젝트 수정")
    void update() throws Exception {
        // Given
        Long oldPartyId = createParty("테스트 협력사 Old");
        Long newPartyId = createParty("테스트 협력사 New");
        Long leadDepartmentId = createLeadDepartment("프로젝트 수정 부서");
        Project project = createProject("PRJ-UPDATE-001", "테스트 프로젝트", oldPartyId, leadDepartmentId, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2024, 1, 1));
        projectRepository.save(project);
        flushAndClear();

        ProjectUpdateApiRequest request = new ProjectUpdateApiRequest(
                newPartyId,
                leadDepartmentId,
                "수정된 프로젝트명",
                "수정된 설명",
                ProjectStatus.IN_PROGRESS.name(),
                150_000_000L,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 12, 31));
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/projects/{id}", project.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(document("project/update",
                        pathParameters(
                                parameterWithName("id").description("수정할 프로젝트 ID")
                        ),
                        requestBody(),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        ProjectUpdateResponse response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ProjectUpdateResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.projectId()).isEqualTo(project.getId());

        flushAndClear();
        Project updatedProject = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(updatedProject.getName()).isEqualTo("수정된 프로젝트명");
    }

    @Test
    @DisplayName("프로젝트 완료 처리")
    void complete() throws Exception {
        // Given
        Long partyId = createParty("테스트 협력사 Complete");
        Project project = createProject("PRJ-COMPLETE-001", "완료 테스트", partyId, 1L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2024, 1, 1));
        projectRepository.save(project);
        flushAndClear();

        // When & Then
        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/projects/{id}/complete", project.getId()).session(session))
                .andDo(document("project/complete",
                        pathParameters(
                                parameterWithName("id").description("완료 처리할 프로젝트 ID")
                        )))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        flushAndClear();
        Project completedProject = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(completedProject.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    @DisplayName("프로젝트 취소 처리")
    void cancel() throws Exception {
        // Given
        Long partyId = createParty("테스트 협력사 Cancel");
        Project project = createProject("PRJ-CANCEL-001", "취소 테스트", partyId, 1L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2024, 1, 1));
        projectRepository.save(project);
        flushAndClear();

        // When & Then
        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.patch("/api/projects/{id}/cancel", project.getId()).session(session))
                .andDo(document("project/cancel",
                        pathParameters(
                                parameterWithName("id").description("취소 처리할 프로젝트 ID")
                        )))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

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
        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/projects/{id}", project.getId()).session(session))
                .andDo(document("project/delete",
                        pathParameters(
                                parameterWithName("id").description("삭제할 프로젝트 ID")
                        )))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        flushAndClear();
        assertThat(projectRepository.findByIdAndDeletedFalse(project.getId())).isEmpty();
    }

    @Test
    @DisplayName("프로젝트 삭제 - 존재하지 않는 프로젝트")
    void delete_notFound() throws Exception {
        // Given
        Long nonExistentId = 9999L;

        // When & Then
        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/projects/{id}", nonExistentId).session(session))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("프로젝트 엑셀 다운로드")
    void downloadExcel() throws Exception {
        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/projects/excel/download").session(session))
                .andDo(document("project/excel-download"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(MockMvcResultMatchers.header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("프로젝트 엑셀 샘플 다운로드")
    void downloadExcelSample() throws Exception {
        MockHttpSession session = login();
        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/projects/excel/sample").session(session))
                    .andDo(document("project/excel-sample"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .andExpect(MockMvcResultMatchers.header().exists("Content-Disposition"));
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @Test
    @DisplayName("프로젝트 상세 조회")
    void find() throws Exception {
        Long partyId = createParty("상세 협력사");
        Long leadDepartmentId = createLeadDepartment("상세 부서");
        Project project = createProject("PRJ-FIND-001", "상세 프로젝트", partyId, leadDepartmentId, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2024, 1, 1));
        projectRepository.save(project);
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/projects/{id}", project.getId()).session(session))
                .andDo(document("project/get",
                        pathParameters(
                                parameterWithName("id").description("조회할 프로젝트 ID")
                        ),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ProjectDetailResponse response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), ProjectDetailResponse.class);
        assertThat(response.projectId()).isEqualTo(project.getId());
    }

    @Test
    @DisplayName("프로젝트 상태 목록을 조회한다")
    void getStatuses() throws Exception {
        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/projects/statuses").session(session))
                .andDo(document("project/statuses", responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("프로젝트 엑셀 업로드")
    void uploadExcel() throws Exception {
        String partyName = "업로드 협력사";
        partyRepository.save(Party.create(new PartyCreateRequest(partyName, null, null, null, null)));
        Long leadDepartmentId = createLeadDepartment("업로드 부서");
        flushAndClear();

        byte[] excelBytes;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Projects");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("협력사 이름");
            header.createCell(1).setCellValue("주관 부서 ID");
            header.createCell(2).setCellValue("프로젝트 코드");
            header.createCell(3).setCellValue("프로젝트명");
            header.createCell(4).setCellValue("상태");
            header.createCell(5).setCellValue("계약금액");
            header.createCell(6).setCellValue("시작일");
            header.createCell(7).setCellValue("종료일");
            header.createCell(8).setCellValue("설명");

            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue(partyName);
            data.createCell(1).setCellValue(String.valueOf(leadDepartmentId));
            data.createCell(2).setCellValue("PRJ-UPLOAD-001");
            data.createCell(3).setCellValue("업로드 프로젝트");
            data.createCell(4).setCellValue("진행 중");
            data.createCell(5).setCellValue("50000000");
            data.createCell(6).setCellValue("2024-01-01");
            data.createCell(7).setCellValue("2024-12-31");
            data.createCell(8).setCellValue("업로드 테스트 설명");

            workbook.write(out);
            excelBytes = out.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
                "file", "projects.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelBytes);

        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/projects/excel/upload")
                        .file(file)
                        .session(session))
                .andDo(document("project/excel-upload",
                        requestParts(
                                partWithName("file").description("업로드할 프로젝트 엑셀 파일")
                        ),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    // @Test
    // @DisplayName("프로젝트 엑셀 업로드")
    // void uploadExcel() throws Exception {
    //     // Given: 협력사 생성
    //     String partyName = "네이버클라우드";
    //     kr.co.abacus.abms.domain.party.Party party = kr.co.abacus.abms.domain.party.Party.create(
    //             new kr.co.abacus.abms.domain.party.PartyCreateRequest(partyName, null, null, null, null));
    //     partyRepository.save(party);
    //     flushAndClear();
    //
    //     byte[] excelBytes;
    //     try (XSSFWorkbook workbook = new XSSFWorkbook();
    //          ByteArrayOutputStream out = new ByteArrayOutputStream()) {
    //         Sheet sheet = workbook.createSheet("Projects");
    //         Row header = sheet.createRow(0);
    //         header.createCell(0).setCellValue("협력사 이름");
    //         header.createCell(1).setCellValue("프로젝트 코드");
    //         header.createCell(2).setCellValue("프로젝트명");
    //         header.createCell(3).setCellValue("상태");
    //         header.createCell(4).setCellValue("계약금액");
    //         header.createCell(5).setCellValue("시작일");
    //         header.createCell(6).setCellValue("종료일");
    //         header.createCell(7).setCellValue("설명");
    //
    //         Row data = sheet.createRow(1);
    //         data.createCell(0).setCellValue(partyName);
    //         data.createCell(1).setCellValue("PRJ-UPLOAD-001");
    //         data.createCell(2).setCellValue("업로드 프로젝트");
    //         data.createCell(3).setCellValue("진행 중");
    //         data.createCell(4).setCellValue("50000000");
    //         data.createCell(5).setCellValue("2024-01-01");
    //         data.createCell(6).setCellValue("2024-12-31");
    //         data.createCell(7).setCellValue("업로드 테스트 설명");
    //
    //         workbook.write(out);
    //         excelBytes = out.toByteArray();
    //     }
    //
    //     MockMultipartFile file = new MockMultipartFile(
    //             "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    //             excelBytes);
    //
    //     mockMvc.perform(MockMvcRequestBuilders.multipart("/api/projects/excel/upload").file(file))
    //             .andExpect(MockMvcResultMatchers.status().isOk());
    // }

    private Project createProject(String code, String name, Long partyId, Long leadDepartmentId, ProjectStatus status, LocalDate startDate) {
        return Project.create(
                partyId,
                leadDepartmentId,
                code,
                name,
                "테스트 프로젝트 설명",
                status,
                100_000_000L,
                startDate,
                startDate.plusMonths(6));
    }

    private Long createParty(String name) {
        Party party = partyRepository.save(Party.create(
                new PartyCreateRequest(name, null, null, null, null)));
        return party.getIdOrThrow();
    }

    private Long createLeadDepartment(String name) {
        Department department = departmentRepository.save(Department.create(
                "PRJ-" + Math.abs(name.hashCode()),
                name,
                DepartmentType.TEAM,
                null,
                null
        ));
        return department.getIdOrThrow();
    }

    private void grant(PermissionGroup permissionGroup, String code, String name) {
        Permission permission = permissionRepository.save(Permission.create(
                code,
                name,
                name + " 권한"
        ));
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.ALL
        ));
    }

    private MockHttpSession login() throws Exception {
        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

}
