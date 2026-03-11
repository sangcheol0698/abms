package kr.co.abacus.abms.adapter.api.employee;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import kr.co.abacus.abms.adapter.api.common.EnumResponse;
import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeCreateRequest;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeDetailResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeExcelUploadResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeSearchResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeUpdateRequest;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeePositionUpdateRequest;
import kr.co.abacus.abms.application.employee.dto.EmployeeOverviewSummary;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.inbound.EmployeeManager;
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
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.grouppermissiongrant.GroupPermissionGrant;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("직원 API (EmployeeApi)")
class EmployeeApiTest extends ApiIntegrationTestBase {

    private static final String READER_USERNAME = "employee-reader@abacus.co.kr";

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeManager employeeManager;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @Autowired
    private GroupPermissionGrantRepository groupPermissionGrantRepository;

    private Long companyId;
    private Long divisionId;
    private Long teamId;

    @BeforeEach
    void setUpDepartments() {
        Department company = createDepartment("CODE-COMPANY", "테스트회사", DepartmentType.COMPANY);
        departmentRepository.save(company);

        Department division = createDepartment("CODE-DIVISION", "테스트사업부", DepartmentType.DIVISION);
        departmentRepository.save(division);

        Department team = createDepartment("CODE-TEAM", "테스트팀", DepartmentType.TEAM);
        departmentRepository.save(team);

        companyId = company.getId();
        divisionId = division.getId();
        teamId = team.getId();

        Employee reader = employeeRepository.save(createEmployee(teamId, READER_USERNAME, "권한조회자"));
        Account account = accountRepository.save(Account.create(
                reader.getIdOrThrow(),
                READER_USERNAME,
                passwordEncoder.encode("Password123!")
        ));
        Permission permission = permissionRepository.save(Permission.create(
                "employee.read",
                "직원 조회",
                "직원 조회 권한"
        ));
        Permission writePermission = permissionRepository.save(Permission.create(
                "employee.write",
                "직원 쓰기",
                "직원 쓰기 권한"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 조회 그룹",
                "직원 조회 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));
        groupPermissionGrantRepository.saveAll(List.of(
                GroupPermissionGrant.create(
                        permissionGroup.getIdOrThrow(),
                        permission.getIdOrThrow(),
                        PermissionScope.ALL
                ),
                GroupPermissionGrant.create(
                        permissionGroup.getIdOrThrow(),
                        writePermission.getIdOrThrow(),
                        PermissionScope.ALL
                )
        ));
        flushAndClear();
    }

    @Test
    @DisplayName("신규 직원을 등록한다")
    void create() throws Exception {
        EmployeeCreateRequest request = createEmployeeCreateRequest(companyId, "test@email.com", "홍길동");
        MockHttpSession session = login();

        MvcResult result = mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeCreateResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                EmployeeCreateResponse.class
        );

        assertThat(response).isNotNull();
        Employee employee = employeeRepository.findById(response.employeeId()).orElseThrow();

        assertThat(employee.getName()).isEqualTo(request.name());
        assertThat(employee.getEmail().address()).isEqualTo(request.email());
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(employee.getAvatar()).isEqualTo(request.avatar());
    }

    @Test
    @DisplayName("잘못된 이메일 형식으로 직원 등록 시 400 에러를 반환한다")
    void create_invalidEmail() throws Exception {
        EmployeeCreateRequest request = createEmployeeCreateRequest(companyId, "invalid-email", "홍길동");
        MockHttpSession session = login();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("중복된 이메일로 직원 등록 시 400 에러를 반환한다")
    void create_duplicateEmail() throws Exception {
        employeeRepository.save(createEmployee(teamId, "test@email.com", "기존직원"));
        EmployeeCreateRequest request = createEmployeeCreateRequest(companyId, "test@email.com", "신규직원");
        MockHttpSession session = login();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("직원 상세 정보를 조회한다")
    void find() throws Exception {
        // given
        Employee employee = createEmployee(teamId, "test@email.com", "테스트직원");
        employeeRepository.save(employee);
        flushAndClear();

        // when & then
        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(get("/api/employees/{id}", employee.getId()).session(session))
                .andExpect(status().isOk())
                .andReturn();
        EmployeeDetailResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                EmployeeDetailResponse.class
        );

        assertThat(response).isNotNull();
        assertThat(response.employeeId()).isEqualTo(employee.getId());
        assertThat(response.name()).isEqualTo(employee.getName());
        assertThat(response.email()).isEqualTo(employee.getEmail());
    }

    @Test
    @DisplayName("직원 검색 - 등급(Grade) 기준으로 정렬한다")
    void search_sortByGradeLevel() throws Exception {
        // given
        employeeRepository.save(createEmployee(teamId, "grade-junior@abms.co", "주니어", EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR));
        employeeRepository.save(createEmployee(teamId, "grade-expert@abms.co", "익스퍼트", EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME, EmployeeGrade.EXPERT));
        employeeRepository.save(createEmployee(teamId, "grade-senior@abms.co", "시니어", EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME, EmployeeGrade.SENIOR));
        flushAndClear();

        // when: grade desc 정렬로 검색 시 레벨이 높은 순서(EXPERT > SENIOR > JUNIOR)로 정렬되어야 한다.
        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(get("/api/employees")
                        .param("sort", "grade,desc")
                        .param("size", "10")
                        .param("page", "0")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();
        PageResponse<EmployeeSearchResponse> responsePage = readPageResponse(result, EmployeeSearchResponse.class);

        List<EmployeeSearchResponse> contents = responsePage.content();
        List<EmployeeSearchResponse> targetContents = contents.stream()
                .filter(response -> Set.of(
                        "grade-junior@abms.co",
                        "grade-expert@abms.co",
                        "grade-senior@abms.co"
                ).contains(response.email()))
                .toList();

        // then: 응답이 200이며 content 배열이 등급 레벨 기준으로 정렬되었는지 확인한다.
        assertThat(targetContents).hasSize(3);
        assertThat(targetContents.get(0).grade())
                .isEqualTo(new EnumResponse(EmployeeGrade.EXPERT.name(), EmployeeGrade.EXPERT.getDescription(), EmployeeGrade.EXPERT.getLevel()));
        assertThat(targetContents.get(1).grade())
                .isEqualTo(new EnumResponse(EmployeeGrade.SENIOR.name(), EmployeeGrade.SENIOR.getDescription(), EmployeeGrade.SENIOR.getLevel()));
        assertThat(targetContents.get(2).grade())
                .isEqualTo(new EnumResponse(EmployeeGrade.JUNIOR.name(), EmployeeGrade.JUNIOR.getDescription(), EmployeeGrade.JUNIOR.getLevel()));
    }

    @Test
    @DisplayName("직원 요약 정보를 조회한다")
    void overviewSummary() throws Exception {
        employeeRepository.save(createEmployee(teamId, "summary-employee-1@abms.co", "요약 직원 1"));
        employeeRepository.save(createEmployee(teamId, "summary-employee-2@abms.co", "요약 직원 2",
                EmployeePosition.ASSOCIATE, EmployeeType.FREELANCER, EmployeeGrade.JUNIOR));
        Employee onLeaveEmployee = createEmployee(teamId, "summary-employee-3@abms.co", "요약 직원 3",
                EmployeePosition.PRINCIPAL, EmployeeType.OUTSOURCING, EmployeeGrade.SENIOR);
        onLeaveEmployee.takeLeave();
        employeeRepository.save(onLeaveEmployee);
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(get("/api/employees/summary")
                        .param("name", "요약 직원")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeOverviewSummary response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                EmployeeOverviewSummary.class
        );

        assertThat(response.totalCount()).isEqualTo(3);
        assertThat(response.activeCount()).isEqualTo(2);
        assertThat(response.onLeaveCount()).isEqualTo(1);
        assertThat(response.fullTimeCount()).isEqualTo(1);
        assertThat(response.freelancerCount()).isEqualTo(1);
        assertThat(response.outsourcingCount()).isEqualTo(1);
        assertThat(response.partTimeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("직원 검색 - 직책(Position) 기준으로 정렬한다")
    void search_sortByPositionRank() throws Exception {
        // given: 직위 rank가 다른 직원 3명을 생성하여 정렬 결과를 확인한다.
        employeeRepository.save(createEmployee(teamId, "grade-junior@abms.co", "익스퍼트", EmployeePosition.DIRECTOR,
                EmployeeType.FULL_TIME, EmployeeGrade.EXPERT));
        employeeRepository.save(createEmployee(teamId, "grade-expert@abms.co", "주니어", EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR));
        employeeRepository.save(createEmployee(teamId, "grade-senior@abms.co", "시니어", EmployeePosition.VICE_PRESIDENT,
                EmployeeType.FULL_TIME, EmployeeGrade.SENIOR));
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(get("/api/employees")
                        .param("sort", "position,asc")
                        .param("size", "10")
                        .param("page", "0")
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();
        PageResponse<EmployeeSearchResponse> responsePage = readPageResponse(result, EmployeeSearchResponse.class);

        assertThat(responsePage).isNotNull();
        List<EmployeeSearchResponse> contents = responsePage.content();
        List<EmployeeSearchResponse> targetContents = contents.stream()
                .filter(response -> Set.of(
                        "grade-junior@abms.co",
                        "grade-expert@abms.co",
                        "grade-senior@abms.co"
                ).contains(response.email()))
                .toList();

        assertThat(targetContents).hasSize(3);
        assertThat(targetContents.get(0).position()).isEqualTo(
                new EnumResponse(EmployeePosition.ASSOCIATE.name(), EmployeePosition.ASSOCIATE.getDescription(), EmployeePosition.ASSOCIATE.getLevel()));
        assertThat(targetContents.get(1).position()).isEqualTo(
                new EnumResponse(EmployeePosition.DIRECTOR.name(), EmployeePosition.DIRECTOR.getDescription(), EmployeePosition.DIRECTOR.getLevel()));
        assertThat(targetContents.get(2).position()).isEqualTo(new EnumResponse(EmployeePosition.VICE_PRESIDENT.name(),
                EmployeePosition.VICE_PRESIDENT.getDescription(), EmployeePosition.VICE_PRESIDENT.getLevel()));
    }

    @Test
    @DisplayName("직원 등급(Grade) 목록을 조회한다")
    void getEmployeeGrades() throws Exception {
        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(get("/api/employees/grades").session(session))
                .andExpect(status().isOk())
                .andReturn();
        List<EnumResponse> responses = readListResponse(result, EnumResponse.class);

        for (EmployeeGrade grade : EmployeeGrade.values()) {
            EnumResponse enumResponse = responses.stream()
                    .filter(r -> r.code().equals(grade.name()))
                    .findFirst()
                    .orElseThrow();

            assertThat(enumResponse.code()).isEqualTo(grade.name());
            assertThat(enumResponse.description()).isEqualTo(grade.getDescription());
        }
    }

    @Test
    @DisplayName("직원 직책(Position) 목록을 조회한다")
    void getEmployeePositions() throws Exception {
        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(get("/api/employees/positions").session(session))
                .andExpect(status().isOk())
                .andReturn();
        List<EnumResponse> responses = readListResponse(result, EnumResponse.class);

        for (EmployeePosition position : EmployeePosition.values()) {
            EnumResponse found = responses.stream()
                    .filter(r -> r.code().equals(position.name()))
                    .findFirst()
                    .orElseThrow();

            assertThat(found.code()).isEqualTo(position.name());
            assertThat(found.description()).isEqualTo(position.getDescription());
        }
    }

    @Test
    @DisplayName("직원 근무 유형(Type) 목록을 조회한다")
    void getEmployeeTypes() throws Exception {
        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(get("/api/employees/types").session(session))
                .andExpect(status().isOk())
                .andReturn();
        List<EnumResponse> responses = readListResponse(result, EnumResponse.class);

        assertThat(responses).hasSize(EmployeeType.values().length);

        for (EmployeeType type : EmployeeType.values()) {
            EnumResponse found = responses.stream()
                    .filter(r -> r.code().equals(type.name()))
                    .findFirst()
                    .orElseThrow();

            assertThat(found.code()).isEqualTo(type.name());
            assertThat(found.description()).isEqualTo(type.getDescription());
        }
    }

    @Test
    @DisplayName("직원 상태(Status) 목록을 조회한다")
    void getEmployeeStatuses() throws Exception {
        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(get("/api/employees/statuses").session(session))
                .andExpect(status().isOk())
                .andReturn();
        List<EnumResponse> responses = readListResponse(result, EnumResponse.class);

        assertThat(responses).hasSize(EmployeeStatus.values().length);

        for (EmployeeStatus status : EmployeeStatus.values()) {
            EnumResponse found = responses.stream()
                    .filter(r -> r.code().equals(status.name()))
                    .findFirst()
                    .orElseThrow();

            assertThat(found.code()).isEqualTo(status.name());
            assertThat(found.description()).isEqualTo(status.getDescription());
        }
    }

    @Test
    @DisplayName("직원 아바타(Avatar) 목록을 조회한다")
    void getEmployeeAvatars() throws Exception {
        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(get("/api/employees/avatars").session(session))
                .andExpect(status().isOk())
                .andReturn();
        List<EnumResponse> responses = readListResponse(result, EnumResponse.class);

        for (EmployeeAvatar avatar : EmployeeAvatar.values()) {
            EnumResponse found = responses.stream()
                    .filter(r -> r.code().equals(avatar.name()))
                    .findFirst()
                    .orElseThrow();

            assertThat(found.code()).isEqualTo(avatar.name());
            assertThat(found.description()).isEqualTo(avatar.getDescription());
        }
    }

    @Test
    @DisplayName("직원 목록을 엑셀 파일로 다운로드한다")
    void downloadExcel() throws Exception {
        employeeRepository.save(createEmployee(teamId, "test@email.com", "테스트직원"));
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(get("/api/employees/excel/download").session(session))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().exists("Content-Disposition"))
                .andReturn();

        assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty();
    }

    @Test
    @DisplayName("직원 등록용 엑셀 샘플 파일을 다운로드한다")
    void downloadExcelSample() {
        restTestClient.get()
                .uri("/api/employees/excel/sample")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .expectHeader().exists("Content-Disposition")
                .expectHeader().valueMatches("Content-Disposition", ".*attachment; filename=.*")
                .expectBody(byte[].class)
                .value(body -> assertThat(body).isNotEmpty());
    }

    @Test
    @DisplayName("엑셀 파일을 업로드하여 직원을 일괄 등록한다")
    void uploadExcel() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");
        Row header = sheet.createRow(0);
        String[] headers = {
                "부서 코드",
                "이메일",
                "이름",
                "입사일",
                "생년월일",
                "직책",
                "근무 유형",
                "등급",
                "메모"
        };
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        String teamCode = departmentRepository.findByIdAndDeletedFalse(teamId)
                .map(Department::getCode)
                .orElseThrow();

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue(teamCode);
        row.createCell(1).setCellValue("excel-upload@abms.co");
        row.createCell(2).setCellValue("업로드");
        row.createCell(3).setCellValue("2025-01-02");
        row.createCell(4).setCellValue("1995-06-10");
        row.createCell(5).setCellValue(EmployeePosition.ASSOCIATE.getDescription());
        row.createCell(6).setCellValue(EmployeeType.FULL_TIME.getDescription());
        row.createCell(7).setCellValue(EmployeeGrade.JUNIOR.getDescription());
        row.createCell(8).setCellValue("업로드 메모");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "employees.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray());

        MockHttpSession session = login();

        var mvcResult = mockMvc.perform(multipart("/api/employees/excel/upload")
                        .file(mockFile)
                        .session(session))
                .andExpect(status().isOk())
                .andReturn();

        EmployeeExcelUploadResponse response = objectMapper.readValue(
                mvcResult.getResponse().getContentAsByteArray(),
                EmployeeExcelUploadResponse.class);
        flushAndClear();

        assertThat(response.successCount()).isEqualTo(1);
        assertThat(response.failures()).isEmpty();

        List<Employee> employees = employeeRepository.findAllByDepartmentIdInAndDeletedFalse(List.of(teamId));
        assertThat(employees)
                .anyMatch(candidate -> candidate.getEmail().address().equals("excel-upload@abms.co"));
    }

    // @Test
    // void uploadExcel() throws Exception {
    // // 1. 엑셀 워크북 생성 로직 (기존과 동일)
    // Workbook workbook = new XSSFWorkbook();
    // Sheet sheet = workbook.createSheet("Employees");
    // Row header = sheet.createRow(0);
    // String[] headers = {"부서 코드", "이메일", "이름", "입사일", "생년월일", "직책", "근무 유형", "등급",
    // "메모"};
    // for (int i = 0; i < headers.length; i++) {
    // header.createCell(i).setCellValue(headers[i]);
    // }
    //
    // String teamCode = departmentRepository.findByIdAndDeletedFalse(teamId)
    // .map(Department::getCode)
    // .orElseThrow();
    //
    // Row row = sheet.createRow(1);
    // row.createCell(0).setCellValue(teamCode);
    // row.createCell(1).setCellValue("excel-upload@abms.co");
    // row.createCell(2).setCellValue("업로드");
    // row.createCell(3).setCellValue("2025-01-02");
    // row.createCell(4).setCellValue("1995-06-10");
    // row.createCell(5).setCellValue(EmployeePosition.ASSOCIATE.getDescription());
    // row.createCell(6).setCellValue(EmployeeType.FULL_TIME.getDescription());
    // row.createCell(7).setCellValue(EmployeeGrade.JUNIOR.getDescription());
    // row.createCell(8).setCellValue("업로드 메모");
    //
    // ByteArrayOutputStream bos = new ByteArrayOutputStream();
    // workbook.write(bos);
    // workbook.close();
    //
    // // 2. MultipartBodyBuilder를 사용하여 요청 본문 구성
    // MultipartBodyBuilder builder = new MultipartBodyBuilder();
    // builder.part("file", bos.toByteArray())
    // .filename("employees.xlsx")
    // .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    //
    // // 3. RestTestClient 실행 및 문서화
    // restTestClient.post()
    // .uri("/api/employees/excel/upload")
    // .body(builder.build())
    // .exchange()
    // .expectStatus().isOk()
    // .expectBody(EmployeeExcelUploadResponse.class)
    // .consumeWith(result -> {
    // // 4. 응답 본문 검증
    // EmployeeExcelUploadResponse response = result.getResponseBody();
    // assertThat(response).isNotNull();
    // assertThat(response.successCount()).isEqualTo(1);
    // assertThat(response.failures()).isEmpty();
    //
    // flushAndClear();
    //
    // // DB 검증
    // var employees =
    // employeeRepository.findAllByDepartmentIdInAndDeletedFalse(List.of(teamId));
    // assertThat(employees).anyMatch(candidate ->
    // candidate.getEmail().address().equals("excel-upload@abms.co"));
    // });
    // }

    @Test
    @DisplayName("직원 정보를 수정한다")
    void update() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "update-target@email.com", "업데이트 대상")).getId();
        flushAndClear();

        EmployeeUpdateRequest request = createEmployeeUpdateRequest(divisionId, "updated@email.com", "김수정");
        MockHttpSession session = login();

        mockMvc.perform(put("/api/employees/{id}", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andExpect(status().isOk());
        flushAndClear();

        Employee updatedEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(updatedEmployee.getDepartmentId()).isEqualTo(request.departmentId());
        assertThat(updatedEmployee.getName()).isEqualTo(request.name());
        assertThat(updatedEmployee.getEmail().address()).isEqualTo(request.email());
        assertThat(updatedEmployee.getJoinDate()).isEqualTo(request.joinDate());
        assertThat(updatedEmployee.getBirthDate()).isEqualTo(request.birthDate());
        assertThat(updatedEmployee.getPosition()).isEqualTo(request.position());
        assertThat(updatedEmployee.getType()).isEqualTo(request.type());
        assertThat(updatedEmployee.getGrade()).isEqualTo(request.grade());
        assertThat(updatedEmployee.getAvatar()).isEqualTo(request.avatar());
        assertThat(updatedEmployee.getMemo()).isEqualTo(request.memo());
    }

    @Test
    @DisplayName("이미 사용 중인 이메일로 수정 시 예외가 발생한다")
    void update_duplicateEmail() throws Exception {
        Long employeeId1 = employeeRepository.save(createEmployee(teamId, "dup1@email.com", "직원1")).getId();
        employeeRepository.save(createEmployee(teamId, "dup2@email.com", "직원2")).getId();

        flushAndClear();

        var request = createEmployeeUpdateRequest(teamId, "dup2@email.com", "직원1-수정");
        MockHttpSession session = login();

        mockMvc.perform(put("/api/employees/{id}", employeeId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("직원을 삭제(Soft Delete)한다")
    void delete() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "delete-target@email.com", "삭제 대상")).getId();
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/employees/{id}", employeeId)
                        .session(session))
                .andExpect(status().isNoContent());
        flushAndClear();

        Employee deletedEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(deletedEmployee.isDeleted()).isTrue();
        assertThat(deletedEmployee.getDeletedBy()).isNotNull();
        assertThat(deletedEmployee.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 삭제된 직원을 삭제하려 할 때 예외가 발생한다")
    void delete_alreadyDeleted() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "delete-target@email.com", "삭제 대상")).getId();

        employeeManager.delete(employeeId, 1L);
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/employees/{id}", employeeId)
                        .session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("삭제된 직원을 복구한다")
    void restore() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();

        employeeManager.delete(employeeId, 1L);
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/restore", employeeId).session(session))
                .andExpect(status().isNoContent());
        flushAndClear();

        Employee restoredEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(restoredEmployee.isDeleted()).isFalse();
        assertThat(restoredEmployee.getDeletedAt()).isNull();
        assertThat(restoredEmployee.getDeletedBy()).isNull();
        assertThat(restoredEmployee.getEmail().address()).isEqualTo("restore@email.com");
    }

    @Test
    @DisplayName("직원의 직급을 승진시킨다")
    void promote() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "promote@email.com", "홍길동",
                EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR)).getId();
        flushAndClear();

        EmployeePositionUpdateRequest request = new EmployeePositionUpdateRequest(EmployeePosition.SENIOR_ASSOCIATE, null);
        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/promote", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andExpect(status().isNoContent());
        flushAndClear();

        Employee promotedEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(promotedEmployee.getPosition()).isEqualTo(EmployeePosition.SENIOR_ASSOCIATE);
    }

    @Test
    @DisplayName("직원의 직급를 현재 직급보다 낮은 직급로 승진시키려 할 때 예외가 발생한다.")
    void promote_lowerPosition_throwsException() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        EmployeePositionUpdateRequest request = new EmployeePositionUpdateRequest(EmployeePosition.ASSOCIATE, null);
        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/promote", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("퇴사한 직원의 직급을 승진시킨다.")
    void promote_resignedEmployee_throwsException() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        employeeManager.resign(employeeId, LocalDate.of(2025, 1, 30));
        flushAndClear();

        EmployeePositionUpdateRequest request = new EmployeePositionUpdateRequest(EmployeePosition.SENIOR_ASSOCIATE, null);
        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/promote", employeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("직원을 퇴사 처리한다")
    void resign() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        LocalDate resignationDate = LocalDate.of(2025, 6, 30);
        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/resign", employeeId)
                        .param("resignationDate", resignationDate.toString())
                        .session(session))
                .andExpect(status().isNoContent());
        flushAndClear();

        Employee resignedEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(resignedEmployee.getStatus()).isEqualTo(EmployeeStatus.RESIGNED);
        assertThat(resignedEmployee.getResignationDate()).isEqualTo(resignationDate);
    }

    @Test
    @DisplayName("퇴사일이 입사일 이전일 경우 예외가 발생한다.")
    void resign_beforeJoinDate_throwsException() throws Exception {
        Long employeeId = employeeRepository
                .save(createEmployee(teamId, "test@example.com", "홍길동", LocalDate.of(2024, 1, 1))).getId();
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/resign", employeeId)
                        .param("resignationDate", LocalDate.of(2023, 12, 31).toString())
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 퇴사한 직원을 다시 퇴사 처리하려 할 때 예외가 발생한다")
    void resign_alreadyResigned_throwsException() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        employeeManager.resign(employeeId, LocalDate.of(2025, 1, 30));
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/resign", employeeId)
                        .param("resignationDate", LocalDate.of(2025, 1, 30).toString())
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("직원을 휴직 처리한다")
    void takeLeave() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/take-leave", employeeId).session(session))
                .andExpect(status().isNoContent());
        flushAndClear();

        Employee onLeaveEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(onLeaveEmployee.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
    }

    @Test
    @DisplayName("재직중이 아닌 직원이 휴직처리 될 때 예외가 발생한다.")
    void takeLeave_notActive_throwsException() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        employeeManager.resign(employeeId, LocalDate.of(2025, 1, 30));
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/take-leave", employeeId).session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("직원을 휴직중에서 재직중으로 복직시킨다.")
    void activate() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        employeeManager.takeLeave(employeeId);
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/activate", employeeId).session(session))
                .andExpect(status().isNoContent());

        flushAndClear();
        Employee activeEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(activeEmployee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

    @Test
    @DisplayName("이미 재직중인 직원이 재활성화 될 때 예외가 발생한다.")
    void activate_alreadyActive_throwsException() throws Exception {
        Long employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/activate", employeeId).session(session))
                .andExpect(status().isBadRequest());
    }

    private EmployeeCreateRequest createEmployeeCreateRequest(
            Long departmentId,
            String email,
            String name) {
        return new EmployeeCreateRequest(
                departmentId,
                email,
                name,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.TEAM_LEADER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                "This is a memo for the employee.");
    }

    private EmployeeUpdateRequest createEmployeeUpdateRequest(
            Long departmentId,
            String email,
            String name) {
        return new EmployeeUpdateRequest(
                departmentId,
                email,
                name,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.TEAM_LEADER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                "This is a updated memo for the employee.");
    }

    private MockHttpSession login() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", READER_USERNAME,
                                "password", "Password123!"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    private <T> PageResponse<T> readPageResponse(MvcResult result, Class<T> contentType) throws Exception {
        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructParametricType(PageResponse.class, contentType)
        );
    }

    private <T> List<T> readListResponse(MvcResult result, Class<T> elementType) throws Exception {
        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, elementType)
        );
    }

    private Employee createEmployee(Long teamId, String email, String name) {
        return Employee.create(
                teamId,
                name,
                email,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 5, 20),
                EmployeePosition.SENIOR_ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null);
    }

    private Employee createEmployee(Long teamId, String email, String name, LocalDate joinDate) {
        return Employee.create(
                teamId,
                name,
                email,
                joinDate,
                LocalDate.of(1990, 5, 20),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null);
    }

    private Employee createEmployee(Long teamId, String email, String name, EmployeePosition position,
                                    EmployeeType type, EmployeeGrade grade) {
        return Employee.create(
                teamId,
                name,
                email,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 5, 20),
                position,
                type,
                grade,
                EmployeeAvatar.SKY_GLOW,
                null);
    }

    private Department createDepartment(String code, String name, DepartmentType departmentType) {
        return Department.create(
                code,
                name,
                departmentType,
                null,
                null);
    }

}
