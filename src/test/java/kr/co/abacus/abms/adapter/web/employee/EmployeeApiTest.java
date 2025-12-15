package kr.co.abacus.abms.adapter.web.employee;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import kr.co.abacus.abms.adapter.web.EnumResponse;
import kr.co.abacus.abms.adapter.web.PageResponse;
import kr.co.abacus.abms.adapter.web.employee.dto.EmployeeCreateRequest;
import kr.co.abacus.abms.adapter.web.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.web.employee.dto.EmployeeExcelUploadResponse;
import kr.co.abacus.abms.adapter.web.employee.dto.EmployeeUpdateRequest;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.application.employee.inbound.EmployeeManager;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
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
        Department company = createDepartment("CODE-COMPANY", "테스트회사", DepartmentType.COMPANY);
        departmentRepository.save(company);

        Department division = createDepartment("CODE-DIVISION", "테스트사업부", DepartmentType.DIVISION);
        departmentRepository.save(division);

        Department team = createDepartment("CODE-TEAM", "테스트팀", DepartmentType.TEAM);
        departmentRepository.save(team);

        companyId = company.getId();
        divisionId = division.getId();
        teamId = team.getId();
    }

    @Test
    void create() {
        EmployeeCreateRequest request = createEmployeeCreateRequest(companyId, "test@email.com", "홍길동");
        String responseJson = objectMapper.writeValueAsString(request);

        EmployeeCreateResponse response = restTestClient.post()
            .uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .body(responseJson)
            .exchange()
            .expectStatus().isOk()
            .expectBody(EmployeeCreateResponse.class)
            .returnResult()
            .getResponseBody();

        assertThat(response).isNotNull();
        Employee employee = employeeRepository.findById(response.employeeId()).orElseThrow();

        assertThat(employee.getName()).isEqualTo(request.name());
        assertThat(employee.getEmail().address()).isEqualTo(request.email());
        assertThat(employee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
        assertThat(employee.getAvatar()).isEqualTo(request.avatar());
    }

    @Test
    void create_invalidEmail() {
        EmployeeCreateRequest request = createEmployeeCreateRequest(companyId, "invalid-email", "홍길동");
        String responseJson = objectMapper.writeValueAsString(request);

        restTestClient.post()
            .uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .body(responseJson)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void create_duplicateEmail() {
        employeeRepository.save(createEmployee(teamId, "test@email.com", "기존직원"));
        EmployeeCreateRequest request = createEmployeeCreateRequest(companyId, "test@email.com", "신규직원");
        String responseJson = objectMapper.writeValueAsString(request);

        restTestClient.post().uri("/api/employees").contentType(MediaType.APPLICATION_JSON)
            .body(responseJson)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void find() {
        // given
        Employee employee = createEmployee(teamId, "test@email.com", "테스트직원");
        employeeRepository.save(employee);
        flushAndClear();

        // when & then
        EmployeeSummary response = restTestClient.get()
            .uri("/api/employees/{id}", employee.getId())
            .exchange()
            .expectStatus().isOk()
            .expectBody(EmployeeSummary.class)
            .returnResult()
            .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.employeeId()).isEqualTo(employee.getId());
        assertThat(response.name()).isEqualTo(employee.getName());
        assertThat(response.email()).isEqualTo(employee.getEmail());
    }

    @Test
    void search_sortByGradeLevel() {
        // given
        employeeRepository.save(createEmployee(teamId, "grade-junior@abms.co", "주니어", EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR));
        employeeRepository.save(createEmployee(teamId, "grade-expert@abms.co", "익스퍼트", EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT));
        employeeRepository.save(createEmployee(teamId, "grade-senior@abms.co", "시니어", EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR));
        flushAndClear();

        // when: grade desc 정렬로 검색 시 레벨이 높은 순서(EXPERT > SENIOR > JUNIOR)로 정렬되어야 한다.
        PageResponse<EmployeeSummary> responsePage = restTestClient.get()
            .uri("/api/employees?sort=grade,desc&size=10&page=0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<PageResponse<EmployeeSummary>>() {
            })
            .returnResult()
            .getResponseBody();

        List<EmployeeSummary> contents = responsePage.content();

        // then: 응답이 200이며 content 배열이 등급 레벨 기준으로 정렬되었는지 확인한다.
        assertThat(contents).hasSize(3);
        assertThat(contents.get(0).grade()).isEqualTo(EmployeeGrade.EXPERT);
        assertThat(contents.get(1).grade()).isEqualTo(EmployeeGrade.SENIOR);
        assertThat(contents.get(2).grade()).isEqualTo(EmployeeGrade.JUNIOR);
    }

    @Test
    void search_sortByPositionRank() {
        // given: 직위 rank가 다른 직원 3명을 생성하여 정렬 결과를 확인한다.
        employeeRepository.save(createEmployee(teamId, "grade-junior@abms.co", "익스퍼트", EmployeePosition.DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT));
        employeeRepository.save(createEmployee(teamId, "grade-expert@abms.co", "주니어", EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR));
        employeeRepository.save(createEmployee(teamId, "grade-senior@abms.co", "시니어", EmployeePosition.VICE_PRESIDENT, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR));
        flushAndClear();

        PageResponse<EmployeeSummary> responsePage = restTestClient.get()
            .uri("/api/employees?sort=position,asc&size=10&page=0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<PageResponse<EmployeeSummary>>() {
            })
            .returnResult()
            .getResponseBody();

        assertThat(responsePage).isNotNull();
        List<EmployeeSummary> contents = responsePage.content();

        assertThat(contents).hasSize(3);
        assertThat(contents.get(0).position()).isEqualTo(EmployeePosition.ASSOCIATE);
        assertThat(contents.get(1).position()).isEqualTo(EmployeePosition.DIRECTOR);
        assertThat(contents.get(2).position()).isEqualTo(EmployeePosition.VICE_PRESIDENT);
    }

    @Test
    void getEmployeeGrades() {
        List<EnumResponse> responses = restTestClient.get()
            .uri("/api/employees/grades")
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<EnumResponse>>() {
            })
            .returnResult()
            .getResponseBody();

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
    void getEmployeePositions() {
        List<EnumResponse> responses = restTestClient.get()
            .uri("/api/employees/positions")
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<EnumResponse>>() {
            })
            .returnResult()
            .getResponseBody();

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
    void getEmployeeTypes() {
        List<EnumResponse> responses = restTestClient.get()
            .uri("/api/employees/types")
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<EnumResponse>>() {
            })
            .returnResult()
            .getResponseBody();

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
    void getEmployeeStatuses() {
        List<EnumResponse> responses = restTestClient.get()
            .uri("/api/employees/statuses")
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<EnumResponse>>() {
            })
            .returnResult()
            .getResponseBody();

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
    void getEmployeeAvatars() {
        List<EnumResponse> responses = restTestClient.get()
            .uri("/api/employees/avatars")
            .exchange()
            .expectStatus().isOk()
            .expectBody(new ParameterizedTypeReference<List<EnumResponse>>() {
            })
            .returnResult()
            .getResponseBody();

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
    void downloadExcel() {
        employeeRepository.save(createEmployee(teamId, "test@email.com", "테스트직원"));
        flushAndClear();

        restTestClient.get()
            .uri("/api/employees/excel/download")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .expectHeader().exists("Content-Disposition")
            .expectHeader().valueMatches("Content-Disposition", ".*attachment; filename=.*")
            .expectBody(byte[].class)
            .value(body -> assertThat(body).isNotEmpty());
    }

    @Test
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
            bos.toByteArray()
        );

        var mvcResult = mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .multipart("/api/employees/excel/upload")
                    .file(mockFile)
            )
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
            .andReturn();

        EmployeeExcelUploadResponse response = objectMapper.readValue(
            mvcResult.getResponse().getContentAsByteArray(),
            EmployeeExcelUploadResponse.class
        );
        flushAndClear();

        assertThat(response.successCount()).isEqualTo(1);
        assertThat(response.failures()).isEmpty();

        List<Employee> employees = employeeRepository.findAllByDepartmentIdInAndDeletedFalse(List.of(teamId));
        assertThat(employees)
            .anyMatch(candidate -> candidate.getEmail().address().equals("excel-upload@abms.co"));
    }

    @Test
    void update() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "update-target@email.com", "업데이트 대상")).getId();
        flushAndClear();

        EmployeeUpdateRequest request = createEmployeeUpdateRequest(divisionId, "updated@email.com", "김수정");
        String responseJson = objectMapper.writeValueAsString(request);

        restTestClient.put()
            .uri("/api/employees/{id}", employeeId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseJson)
            .exchange()
            .expectStatus().isOk();
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
    void update_duplicateEmail() {
        UUID employeeId1 = employeeRepository.save(createEmployee(teamId, "dup1@email.com", "직원1")).getId();
        employeeRepository.save(createEmployee(teamId, "dup2@email.com", "직원2")).getId();

        flushAndClear();

        var request = createEmployeeUpdateRequest(teamId, "dup2@email.com", "직원1-수정");
        String responseJson = objectMapper.writeValueAsString(request);

        restTestClient.put()
            .uri("/api/employees/{id}", employeeId1)
            .contentType(MediaType.APPLICATION_JSON)
            .body(responseJson)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void delete() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "delete-target@email.com", "삭제 대상")).getId();
        flushAndClear();

        restTestClient.delete()
            .uri("/api/employees/{id}", employeeId)
            .exchange()
            .expectStatus().isNoContent();
        flushAndClear();

        Employee deletedEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(deletedEmployee.isDeleted()).isTrue();
        assertThat(deletedEmployee.getDeletedBy()).isEqualTo("SYSTEM");
        assertThat(deletedEmployee.getDeletedAt()).isNotNull();
    }

    @Test
    void delete_alreadyDeleted() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "delete-target@email.com", "삭제 대상")).getId();

        employeeManager.delete(employeeId, "adminUser");
        flushAndClear();

        restTestClient.delete()
            .uri("/api/employees/{id}", employeeId)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void restore() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();

        employeeManager.delete(employeeId, "adminUser");
        flushAndClear();

        restTestClient.patch()
            .uri("/api/employees/{id}/restore", employeeId)
            .exchange()
            .expectStatus().isNoContent();
        flushAndClear();

        Employee restoredEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(restoredEmployee.isDeleted()).isFalse();
        assertThat(restoredEmployee.getDeletedAt()).isNull();
        assertThat(restoredEmployee.getDeletedBy()).isNull();
        assertThat(restoredEmployee.getEmail().address()).isEqualTo("restore@email.com");
    }

    @Test
    void promote() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "promote@email.com", "홍길동", EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR)).getId();
        flushAndClear();

        restTestClient.patch()
            .uri("/api/employees/{id}/promote?position=STAFF", employeeId)
            .exchange()
            .expectStatus().isOk();
        flushAndClear();

        Employee promotedEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(promotedEmployee.getPosition()).isEqualTo(EmployeePosition.STAFF);
    }

    @Test
    @DisplayName("직원의 직급를 현재 직급보다 낮은 직급로 승진시키려 할 때 예외가 발생한다.")
    void promote_lowerPosition_throwsException() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        restTestClient.patch()
            .uri("/api/employees/{id}/promote?position=ASSOCIATE", employeeId)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("퇴사한 직원의 직급을 승진시키려 할 때 예외가 발생한다.")
    void promote_resignedEmployee_throwsException() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        employeeManager.resign(employeeId, LocalDate.now());
        flushAndClear();

        restTestClient.patch()
            .uri("/api/employees/{id}/promote?position=STAFF", employeeId)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void resign() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        LocalDate resignationDate = LocalDate.now();
        restTestClient.patch()
            .uri("/api/employees/{id}/resign?resignationDate={date}", employeeId, resignationDate)
            .exchange()
            .expectStatus().isNoContent();
        flushAndClear();

        Employee resignedEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(resignedEmployee.getStatus()).isEqualTo(EmployeeStatus.RESIGNED);
        assertThat(resignedEmployee.getResignationDate()).isEqualTo(resignationDate);
    }

    @Test
    @DisplayName("퇴사일이 입사일 이전일 경우 예외가 발생한다.")
    void resign_beforeJoinDate_throwsException() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "test@example.com", "홍길동", LocalDate.of(2024, 1, 1))).getId();
        flushAndClear();

        restTestClient.patch()
            .uri("/api/employees/{id}/resign?resignationDate={date}",
                employeeId, LocalDate.of(2023, 12, 31))
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void resign_alreadyResigned_throwsException() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        employeeManager.resign(employeeId, LocalDate.now());
        flushAndClear();

        restTestClient.patch()
            .uri("/api/employees/{id}/resign?resignationDate={date}", employeeId, LocalDate.now())
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void takeLeave() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        restTestClient.patch()
            .uri("/api/employees/{id}/take-leave", employeeId)
            .exchange()
            .expectStatus().isOk();
        flushAndClear();

        Employee onLeaveEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(onLeaveEmployee.getStatus()).isEqualTo(EmployeeStatus.ON_LEAVE);
    }

    @Test
    @DisplayName("재직중이 아닌 직원이 휴직처리 될 때 예외가 발생한다.")
    void takeLeave_notActive_throwsException() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        employeeManager.resign(employeeId, LocalDate.now());
        flushAndClear();

        restTestClient.patch()
            .uri("/api/employees/{id}/take-leave", employeeId)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("직원을 휴직중에서 재직중으로 복직시킨다.")
    void activate() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        employeeManager.takeLeave(employeeId);
        flushAndClear();

        restTestClient.patch()
            .uri("/api/employees/{id}/activate", employeeId)
            .exchange()
            .expectStatus().isNoContent();

        flushAndClear();
        Employee activeEmployee = employeeRepository.findById(employeeId).orElseThrow();
        assertThat(activeEmployee.getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

    @Test
    @DisplayName("이미 재직중인 직원이 재활성화 될 때 예외가 발생한다.")
    void activate_alreadyActive_throwsException() {
        UUID employeeId = employeeRepository.save(createEmployee(teamId, "restore@email.com", "홍길동")).getId();
        flushAndClear();

        restTestClient.patch()
            .uri("/api/employees/{id}/activate", employeeId)
            .exchange()
            .expectStatus().isBadRequest();
    }

    private EmployeeCreateRequest createEmployeeCreateRequest(
        UUID departmentId,
        String email,
        String name
    ) {
        return new EmployeeCreateRequest(
            departmentId,
            email,
            name,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            EmployeeAvatar.SKY_GLOW,
            "This is a memo for the employee."
        );
    }

    private EmployeeUpdateRequest createEmployeeUpdateRequest(
        UUID departmentId,
        String email,
        String name
    ) {
        return new EmployeeUpdateRequest(
            departmentId,
            email,
            name,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(1990, 1, 1),
            EmployeePosition.MANAGER,
            EmployeeType.FULL_TIME,
            EmployeeGrade.SENIOR,
            EmployeeAvatar.SKY_GLOW,
            "This is a updated memo for the employee."
        );
    }

    private Employee createEmployee(UUID teamId, String email, String name) {
        return Employee.create(
            teamId,
            name,
            email,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(1990, 5, 20),
            EmployeePosition.ASSOCIATE,
            EmployeeType.FULL_TIME,
            EmployeeGrade.JUNIOR,
            EmployeeAvatar.SKY_GLOW,
            null
        );
    }

    private Employee createEmployee(UUID teamId, String email, String name, LocalDate joinDate) {
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
            null
        );
    }

    private Employee createEmployee(UUID teamId, String email, String name, EmployeePosition position, EmployeeType type, EmployeeGrade grade) {
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
            null
        );
    }

    private Department createDepartment(String code, String name, DepartmentType departmentType) {
        return Department.create(
            code,
            name,
            departmentType,
            null,
            null
        );
    }

}
