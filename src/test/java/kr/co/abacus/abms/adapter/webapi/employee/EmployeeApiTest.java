package kr.co.abacus.abms.adapter.webapi.employee;

import static kr.co.abacus.abms.domain.employee.EmployeeFixture.*;
import static kr.co.abacus.abms.support.AssertThatUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeAvatarResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeExcelUploadResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeGradeResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeePositionResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeStatusResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeTypeResponse;
import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.provided.EmployeeManager;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

class EmployeeApiTest extends ApiIntegrationTestBase {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeManager employeeManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    private UUID companyId;
    private UUID divisionId;
    private UUID teamId;

    @BeforeEach
    void setUpDepartments() {
        Department company = DepartmentFixture.createTestCompany();
        departmentRepository.save(company);
        Department division = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION, null),
            company
        );
        departmentRepository.save(division);
        Department team = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM, null),
            division
        );
        departmentRepository.save(team);
        flushAndClear();
        companyId = company.getId();
        divisionId = division.getId();
        teamId = team.getId();
    }

    @Test
    void create() throws JsonProcessingException, UnsupportedEncodingException {
        EmployeeCreateRequest request = createEmployeeCreateRequestWithDepartment(companyId);
        String responseJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .content(responseJson).exchange();
        flushAndClear();

        assertThat(result)
            .apply(print())
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.employeeId", notNull())
            .hasPathSatisfying("$.email", equalsTo(request.email()));

        EmployeeCreateResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeCreateResponse.class);
        Employee employee = employeeRepository.findById(response.employeeId()).orElseThrow();

        assertThat(employee.getName()).isEqualTo(request.name());
        assertThat(employee.getEmail().address()).isEqualTo(request.email());
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(employee.getAvatar()).isEqualTo(request.avatar());
    }

    @Test
    void create_invalidEmail() throws JsonProcessingException {
        EmployeeCreateRequest request = createEmployeeCreateRequestWithDepartment(companyId, "invalid-email");
        String responseJson = objectMapper.writeValueAsString(request);

        assertThat(mvcTester.post().uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .content(responseJson))
            .apply(print())
            .hasStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void create_duplicateEmail() throws JsonProcessingException {
        employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId));

        EmployeeCreateRequest request = createEmployeeCreateRequestWithDepartment(companyId);
        String responseJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.post().uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .content(responseJson).exchange();

        assertThat(result)
            .apply(print())
            .hasStatus(HttpStatus.CONFLICT.value());
    }

    @Test
    void find() throws UnsupportedEncodingException, JsonProcessingException {
        Employee savedEmployee = employeeManager.create(createEmployeeCreateRequestWithDepartment(companyId));

        MvcTestResult result = mvcTester.get().uri("/api/employees/{id}", savedEmployee.getId()).exchange();

        assertThat(result)
            .apply(print())
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.departmentId", equalsTo(savedEmployee.getDepartmentId().toString()))
            .hasPathSatisfying("$.departmentName", equalsTo("테스트회사"))
            .hasPathSatisfying("$.employeeId", equalsTo(savedEmployee.getId().toString()))
            .hasPathSatisfying("$.name", equalsTo(savedEmployee.getName()))
            .hasPathSatisfying("$.email", equalsTo(savedEmployee.getEmail().address()))
            .hasPathSatisfying("$.avatarCode", equalsTo(savedEmployee.getAvatar().name()))
            .hasPathSatisfying("$.avatarLabel", equalsTo(savedEmployee.getAvatar().getDisplayName()));

        EmployeeResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), EmployeeResponse.class);

        assertThat(response.departmentId()).isEqualTo(savedEmployee.getDepartmentId());
        assertThat(response.departmentName()).isEqualTo("테스트회사");
        assertThat(response.employeeId()).isEqualTo(savedEmployee.getId().toString());
        assertThat(response.name()).isEqualTo(savedEmployee.getName());
        assertThat(response.email()).isEqualTo(savedEmployee.getEmail().address());
        assertThat(response.avatarCode()).isEqualTo(savedEmployee.getAvatar().name());
        assertThat(response.avatarLabel()).isEqualTo(savedEmployee.getAvatar().getDisplayName());
    }

    @Test
    void search_sortByGradeLevel() throws Exception {
        // given: 등급 레벨이 다른 직원 3명을 생성하여 정렬 결과를 확인한다.
        employeeManager.create(createCustomEmployee(teamId, "grade-junior@abms.co", "주니어", EmployeeGrade.JUNIOR, EmployeePosition.ASSOCIATE));
        employeeManager.create(createCustomEmployee(teamId, "grade-expert@abms.co", "익스퍼트", EmployeeGrade.EXPERT, EmployeePosition.MANAGER));
        employeeManager.create(createCustomEmployee(teamId, "grade-senior@abms.co", "시니어", EmployeeGrade.SENIOR, EmployeePosition.LEADER));
        flushAndClear();

        // when: grade desc 정렬로 검색 시 레벨이 높은 순서(EXPERT > SENIOR > JUNIOR)로 정렬되어야 한다.
        MvcTestResult result = mvcTester.get()
            .uri("/api/employees?sort=grade,desc&size=10&page=0")
            .exchange();

        // then: 응답이 200이며 content 배열이 등급 레벨 기준으로 정렬되었는지 확인한다.
        assertThat(result).apply(print()).hasStatusOk();

        JsonNode content = extractContent(result);
        assertThat(content).hasSize(3);
        assertThat(content.get(0).get("grade").asText()).isEqualTo(EmployeeGrade.EXPERT.getDescription());
        assertThat(content.get(1).get("grade").asText()).isEqualTo(EmployeeGrade.SENIOR.getDescription());
        assertThat(content.get(2).get("grade").asText()).isEqualTo(EmployeeGrade.JUNIOR.getDescription());
    }

    @Test
    void search_sortByPositionRank() throws Exception {
        // given: 직위 rank가 다른 직원 3명을 생성하여 정렬 결과를 확인한다.
        employeeManager.create(createCustomEmployee(teamId, "position-director@abms.co", "디렉터", EmployeeGrade.SENIOR, EmployeePosition.DIRECTOR));
        employeeManager.create(createCustomEmployee(teamId, "position-associate@abms.co", "어소시에이트", EmployeeGrade.MID_LEVEL, EmployeePosition.ASSOCIATE));
        employeeManager.create(createCustomEmployee(teamId, "position-vice@abms.co", "부사장", EmployeeGrade.EXPERT, EmployeePosition.VICE_PRESIDENT));
        flushAndClear();

        // when: position asc 정렬 시 rank가 낮은 순서(ASSOCIATE > DIRECTOR > VICE_PRESIDENT)로 정렬되어야 한다.
        MvcTestResult result = mvcTester.get()
            .uri("/api/employees?sort=position,asc&size=10&page=0")
            .exchange();

        // then: 응답이 200이며 직위 설명이 rank 오름차순으로 정렬되었는지 확인한다.
        assertThat(result).apply(print()).hasStatusOk();

        JsonNode content = extractContent(result);
        assertThat(content).hasSize(3);
        assertThat(content.get(0).get("position").asText()).isEqualTo(EmployeePosition.ASSOCIATE.getDescription());
        assertThat(content.get(1).get("position").asText()).isEqualTo(EmployeePosition.DIRECTOR.getDescription());
        assertThat(content.get(2).get("position").asText()).isEqualTo(EmployeePosition.VICE_PRESIDENT.getDescription());
    }

    @Test
    void getEmployeeGrades() throws Exception {
        MvcTestResult result = mvcTester.get().uri("/api/employees/grades").exchange();

        assertThat(result).apply(print()).hasStatusOk();

        List<EmployeeGradeResponse> responses = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {}
        );

        assertThat(responses).hasSize(EmployeeGrade.values().length);

        for (EmployeeGrade grade : EmployeeGrade.values()) {
            EmployeeGradeResponse found = responses.stream()
                .filter(r -> r.name().equals(grade.name()))
                .findFirst()
                .orElseThrow();

            assertThat(found.name()).isEqualTo(grade.name());
            assertThat(found.description()).isEqualTo(grade.getDescription());
            assertThat(found.level()).isEqualTo(grade.getLevel());
        }
    }

    @Test
    void getEmployeePositions() throws Exception {
        MvcTestResult result = mvcTester.get().uri("/api/employees/positions").exchange();

        assertThat(result).apply(print()).hasStatusOk();

        List<EmployeePositionResponse> responses = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {
            }
        );

        assertThat(responses).hasSize(EmployeePosition.values().length);

        for (EmployeePosition position : EmployeePosition.values()) {
            EmployeePositionResponse found = responses.stream()
                .filter(r -> r.name().equals(position.name()))
                .findFirst()
                .orElseThrow();

            assertThat(found.name()).isEqualTo(position.name());
            assertThat(found.description()).isEqualTo(position.getDescription());
            assertThat(found.rank()).isEqualTo(position.getRank());
        }
    }

    @Test
    void getEmployeeTypes() throws Exception {
        MvcTestResult result = mvcTester.get().uri("/api/employees/types").exchange();

        assertThat(result).apply(print()).hasStatusOk();

        List<EmployeeTypeResponse> responses = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {
            }
        );

        assertThat(responses).hasSize(EmployeeType.values().length);

        for (EmployeeType type : EmployeeType.values()) {
            EmployeeTypeResponse found = responses.stream()
                .filter(r -> r.name().equals(type.name()))
                .findFirst()
                .orElseThrow();

            assertThat(found.name()).isEqualTo(type.name());
            assertThat(found.description()).isEqualTo(type.getDescription());
        }
    }

    @Test
    void getEmployeeStatuses() throws Exception {
        MvcTestResult result = mvcTester.get().uri("/api/employees/statuses").exchange();

        assertThat(result).apply(print()).hasStatusOk();

        List<EmployeeStatusResponse> responses = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {
            }
        );

        assertThat(responses).hasSize(EmployeeStatus.values().length);

        for (EmployeeStatus status : EmployeeStatus.values()) {
            EmployeeStatusResponse found = responses.stream()
                .filter(r -> r.name().equals(status.name()))
                .findFirst()
                .orElseThrow();

            assertThat(found.name()).isEqualTo(status.name());
            assertThat(found.description()).isEqualTo(status.getDescription());
        }
    }

    @Test
    void getEmployeeAvatars() throws Exception {
        MvcTestResult result = mvcTester.get().uri("/api/employees/avatars").exchange();

        assertThat(result).apply(print()).hasStatusOk();

        List<EmployeeAvatarResponse> responses = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            new TypeReference<>() {}
        );

        assertThat(responses).hasSize(EmployeeAvatar.values().length);

        for (EmployeeAvatar avatar : EmployeeAvatar.values()) {
            EmployeeAvatarResponse found = responses.stream()
                .filter(r -> r.code().equals(avatar.name()))
                .findFirst()
                .orElseThrow();

            assertThat(found.code()).isEqualTo(avatar.name());
            assertThat(found.displayName()).isEqualTo(avatar.getDisplayName());
        }
    }

    @Test
    void downloadExcel() throws Exception {
        employeeManager.create(createEmployeeCreateRequestWithDepartment(teamId, "excel-download@abms.co"));
        flushAndClear();

        MvcTestResult result = mvcTester.get()
            .uri("/api/employees/excel/download")
            .exchange();

        assertThat(result)
            .apply(print())
            .hasStatusOk();
        assertThat(result.getResponse().getContentType())
            .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty();
        assertThat(result.getResponse().getHeader("Content-Disposition"))
            .contains("attachment; filename=");
    }

    @Test
    void downloadExcelSample() throws Exception {
        MvcTestResult result = mvcTester.get()
            .uri("/api/employees/excel/sample")
            .exchange();

        assertThat(result)
            .apply(print())
            .hasStatusOk();
        assertThat(result.getResponse().getContentType())
            .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(result.getResponse().getContentAsByteArray()).isNotEmpty();
        assertThat(result.getResponse().getHeader("Content-Disposition"))
            .contains("attachment; filename=");
    }

    @Test
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

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "employees.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            bos.toByteArray()
        );

        MvcTestResult result = mvcTester.post()
            .uri("/api/employees/excel/upload")
            .multipart()
            .file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .exchange();
        flushAndClear();

        assertThat(result)
            .apply(print())
            .hasStatusOk();

        EmployeeExcelUploadResponse response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            EmployeeExcelUploadResponse.class
        );

        assertThat(response.successCount()).isEqualTo(1);
        assertThat(response.failures()).isEmpty();

        List<Employee> employees = employeeRepository.findAllByDepartmentIdInAndDeletedFalse(List.of(teamId));
        assertThat(employees)
            .anyMatch(candidate -> candidate.getEmail().address().equals("excel-upload@abms.co"));
    }

    @Test
    void update() throws Exception {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(teamId, "update-target@email.com"));
        flushAndClear();

        var request = createEmployeeUpdateRequestWithDepartment(divisionId, "김수정", "updated@email.com");
        String responseJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.put()
            .uri("/api/employees/{id}", employee.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(responseJson)
            .exchange();
        flushAndClear();

        assertThat(result)
            .apply(print())
            .hasStatusOk()
            .bodyJson()
            .hasPathSatisfying("$.departmentId", equalsTo(divisionId.toString()))
            .hasPathSatisfying("$.name", equalsTo(request.name()))
            .hasPathSatisfying("$.email", equalsTo(request.email()))
            .hasPathSatisfying("$.joinDate", equalsTo(request.joinDate().toString()))
            .hasPathSatisfying("$.birthDate", equalsTo(request.birthDate().toString()))
            .hasPathSatisfying("$.avatarCode", equalsTo(request.avatar().name()))
            .hasPathSatisfying("$.avatarLabel", equalsTo(request.avatar().getDisplayName()))
            .hasPathSatisfying("$.memo", equalsTo(request.memo()));

        Employee updatedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
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
    void update_duplicateEmail() throws Exception {
        Employee employee1 = employeeManager.create(createEmployeeCreateRequestWithDepartment(teamId, "dup1@email.com"));
        Employee employee2 = employeeManager.create(createEmployeeCreateRequestWithDepartment(teamId, "dup2@email.com"));
        flushAndClear();

        var request = createEmployeeUpdateRequestWithDepartment(divisionId, employee1.getName(), employee2.getEmail().address());
        String responseJson = objectMapper.writeValueAsString(request);

        MvcTestResult result = mvcTester.put()
            .uri("/api/employees/{id}", employee1.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(responseJson)
            .exchange();

        assertThat(result)
            .apply(print())
            .hasStatus(HttpStatus.CONFLICT.value());
    }

    @Test
    void delete() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(teamId, "delete-target@email.com"));
        flushAndClear();

        MvcTestResult result = mvcTester.delete()
            .uri("/api/employees/{id}", employee.getId())
            .exchange();
        flushAndClear();

        assertThat(result)
            .apply(print())
            .hasStatus(HttpStatus.NO_CONTENT.value());

        Employee deletedEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(deletedEmployee.isDeleted()).isTrue();
        assertThat(deletedEmployee.getDeletedBy()).isEqualTo("SYSTEM");
        assertThat(deletedEmployee.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_alreadyDeleted() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(teamId, "already-deleted@email.com"));
        employeeManager.delete(employee.getId(), "adminUser");
        flushAndClear();

        MvcTestResult result = mvcTester.delete()
            .uri("/api/employees/{id}", employee.getId())
            .exchange();

        assertThat(result)
            .apply(print())
            .hasStatus(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void restore() {
        Employee employee = employeeManager.create(createEmployeeCreateRequestWithDepartment(teamId, "restore@email.com"));
        employeeManager.delete(employee.getId(), "adminUser");
        flushAndClear();

        MvcTestResult result = mvcTester.patch()
            .uri("/api/employees/{id}/restore", employee.getId())
            .exchange();
        flushAndClear();

        assertThat(result)
            .apply(print())
            .hasStatus(HttpStatus.NO_CONTENT.value());

        Employee restoredEmployee = employeeRepository.findById(employee.getId()).orElseThrow();
        assertThat(restoredEmployee.isDeleted()).isFalse();
        assertThat(restoredEmployee.getDeletedAt()).isNull();
        assertThat(restoredEmployee.getDeletedBy()).isNull();
        assertThat(restoredEmployee.getEmail().address()).isEqualTo("restore@email.com");
    }

    private EmployeeCreateRequest createCustomEmployee(
        UUID departmentId,
        String email,
        String name,
        EmployeeGrade grade,
        EmployeePosition position
    ) {
        // 정렬 검증을 위해 입력값을 자유롭게 조합할 수 있는 테스트 전용 팩토리 메서드다.
        return new EmployeeCreateRequest(
            departmentId,
            email,
            name,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(1990, 1, 1),
            position,
            EmployeeType.FULL_TIME,
            grade,
            EmployeeAvatar.SKY_GLOW,
            null
        );
    }

    private JsonNode extractContent(MvcTestResult result) throws JsonProcessingException, UnsupportedEncodingException {
        // MockMvc 응답에서 content 배열만 추출해 정렬 순서를 명확히 검증한다.
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("content");
    }

}
