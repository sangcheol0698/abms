package kr.co.abacus.abms.adapter.api.employee;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
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

import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeCreateRequest;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeDepartmentTransferRequest;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeEmploymentTypeConvertRequest;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeePositionUpdateRequest;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeUpdateRequest;
import kr.co.abacus.abms.application.auth.CurrentActor;
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

@DisplayName("직원 API 쓰기 권한 인가")
class EmployeeWriteAuthorizationApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "employee-write-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeManager employeeManager;

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

    private Long ownDivisionId;
    private Long childTeamId;
    private Long outsideDivisionId;
    private Long selfEmployeeId;
    private Long sameDepartmentEmployeeId;
    private Long childDepartmentEmployeeId;
    private Long outsideDepartmentEmployeeId;

    @BeforeEach
    void setUpAccount() {
        Department ownDivision = departmentRepository.save(Department.create(
                "WRITE-AUTH-DIVISION",
                "쓰기 인가 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));
        Department childTeam = departmentRepository.save(Department.create(
                "WRITE-AUTH-TEAM",
                "쓰기 인가 팀",
                DepartmentType.TEAM,
                null,
                ownDivision
        ));
        Department outsideDivision = departmentRepository.save(Department.create(
                "WRITE-AUTH-OUTSIDE",
                "다른 본부",
                DepartmentType.DIVISION,
                null,
                null
        ));

        ownDivisionId = ownDivision.getIdOrThrow();
        childTeamId = childTeam.getIdOrThrow();
        outsideDivisionId = outsideDivision.getIdOrThrow();

        Employee currentEmployee = employeeRepository.save(createEmployee(ownDivisionId, USERNAME, "쓰기인가사용자"));
        selfEmployeeId = currentEmployee.getIdOrThrow();
        accountRepository.save(Account.create(selfEmployeeId, USERNAME, passwordEncoder.encode(PASSWORD)));

        sameDepartmentEmployeeId = employeeRepository.save(createEmployee(
                ownDivisionId,
                "write-same@abacus.co.kr",
                "같은부서직원"
        )).getIdOrThrow();
        childDepartmentEmployeeId = employeeRepository.save(createEmployee(
                childTeamId,
                "write-child@abacus.co.kr",
                "하위부서직원"
        )).getIdOrThrow();
        outsideDepartmentEmployeeId = employeeRepository.save(createEmployee(
                outsideDivisionId,
                "write-outside@abacus.co.kr",
                "외부부서직원"
        )).getIdOrThrow();
        flushAndClear();
    }

    @Test
    @DisplayName("로그인했지만 employee.write 권한이 없으면 변경 계열 API는 403을 반환한다")
    void should_returnForbidden_whenWritingEmployeeWithoutEmployeeWritePermission() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest(ownDivisionId, "new@abacus.co.kr", "신규직원")))
                        .session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/employees/{id}", selfEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest(selfEmployeeId, ownDivisionId)))
                        .session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/employees/{id}/transfer-department", selfEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EmployeeDepartmentTransferRequest(outsideDivisionId)))
                        .session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/employees/{id}/convert-employment-type", selfEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EmployeeEmploymentTypeConvertRequest(EmployeeType.PART_TIME)))
                        .session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/employees/{id}", selfEmployeeId).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위는 자기 부서 직원 생성만 허용한다")
    void should_allowCreateOnlyInOwnDepartment_whenGrantedOwnDepartmentScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest(ownDivisionId, "create-own@abacus.co.kr", "부서생성")))
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest(outsideDivisionId, "create-outside@abacus.co.kr", "외부생성")))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SELF 범위는 직원 생성을 허용하지 않는다")
    void should_forbidCreate_whenGrantedSelfScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.SELF);
        MockHttpSession session = login();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest(ownDivisionId, "create-self@abacus.co.kr", "셀프생성")))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SELF 범위는 본인 이름, 생년월일, 아바타만 수정할 수 있다")
    void should_allowOnlySelfProfileFields_whenGrantedSelfScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.SELF);
        MockHttpSession session = login();

        EmployeeUpdateRequest allowedRequest = new EmployeeUpdateRequest(
                ownDivisionId,
                USERNAME,
                "새이름",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1991, 6, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.AQUA_SPLASH,
                null
        );

        mockMvc.perform(put("/api/employees/{id}", selfEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(allowedRequest))
                        .session(session))
                .andExpect(status().isOk());

        flushAndClear();
        Employee updated = employeeRepository.findByIdAndDeletedFalse(selfEmployeeId).orElseThrow();
        assertThat(updated.getName()).isEqualTo("새이름");
        assertThat(updated.getBirthDate()).isEqualTo(LocalDate.of(1991, 6, 1));
        assertThat(updated.getAvatar()).isEqualTo(EmployeeAvatar.AQUA_SPLASH);
    }

    @Test
    @DisplayName("SELF 범위는 허용되지 않은 필드 변경을 막는다")
    void should_forbidRestrictedSelfUpdates_whenGrantedSelfScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.SELF);
        MockHttpSession session = login();

        for (EmployeeUpdateRequest request : Set.of(
                new EmployeeUpdateRequest(ownDivisionId, "changed@abacus.co.kr", "쓰기인가사용자",
                        LocalDate.of(2024, 1, 1), LocalDate.of(1990, 5, 20), EmployeePosition.ASSOCIATE,
                        EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR, EmployeeAvatar.SKY_GLOW, null),
                new EmployeeUpdateRequest(ownDivisionId, USERNAME, "쓰기인가사용자",
                        LocalDate.of(2024, 1, 1), LocalDate.of(1990, 5, 20), EmployeePosition.ASSOCIATE,
                        EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR, EmployeeAvatar.SKY_GLOW, "메모"),
                new EmployeeUpdateRequest(outsideDivisionId, USERNAME, "쓰기인가사용자",
                        LocalDate.of(2024, 1, 1), LocalDate.of(1990, 5, 20), EmployeePosition.ASSOCIATE,
                        EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR, EmployeeAvatar.SKY_GLOW, null),
                new EmployeeUpdateRequest(ownDivisionId, USERNAME, "쓰기인가사용자",
                        LocalDate.of(2025, 1, 1), LocalDate.of(1990, 5, 20), EmployeePosition.ASSOCIATE,
                        EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR, EmployeeAvatar.SKY_GLOW, null),
                new EmployeeUpdateRequest(ownDivisionId, USERNAME, "쓰기인가사용자",
                        LocalDate.of(2024, 1, 1), LocalDate.of(1990, 5, 20), EmployeePosition.SENIOR_ASSOCIATE,
                        EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR, EmployeeAvatar.SKY_GLOW, null),
                new EmployeeUpdateRequest(ownDivisionId, USERNAME, "쓰기인가사용자",
                        LocalDate.of(2024, 1, 1), LocalDate.of(1990, 5, 20), EmployeePosition.ASSOCIATE,
                        EmployeeType.FREELANCER, EmployeeGrade.JUNIOR, EmployeeAvatar.SKY_GLOW, null),
                new EmployeeUpdateRequest(ownDivisionId, USERNAME, "쓰기인가사용자",
                        LocalDate.of(2024, 1, 1), LocalDate.of(1990, 5, 20), EmployeePosition.ASSOCIATE,
                        EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.SKY_GLOW, null)
        )) {
            mockMvc.perform(put("/api/employees/{id}", selfEmployeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request))
                            .session(session))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    @DisplayName("SELF 범위는 타인 수정을 허용하지 않는다")
    void should_forbidUpdatingOthers_whenGrantedSelfScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.SELF);
        MockHttpSession session = login();

        mockMvc.perform(put("/api/employees/{id}", sameDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest(sameDepartmentEmployeeId, ownDivisionId)))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWN_DEPARTMENT_TREE 범위는 하위 부서 직원 수정을 허용하고 범위 밖 직원 수정을 막는다")
    void should_controlUpdateByDepartmentTreeScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT_TREE);
        MockHttpSession session = login();

        mockMvc.perform(put("/api/employees/{id}", childDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest(childDepartmentEmployeeId, childTeamId)))
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/employees/{id}", outsideDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest(outsideDepartmentEmployeeId, outsideDivisionId)))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위는 범위 밖 부서로 이동시키는 수정을 막는다")
    void should_forbidMovingEmployeeOutsideAllowedDepartment_whenGrantedOwnDepartmentScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(put("/api/employees/{id}", sameDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest(sameDepartmentEmployeeId, outsideDivisionId)))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("부서 변경이 발생하면 대상 계정의 기존 세션은 만료된다")
    void should_expireSessionAfterDepartmentChange() throws Exception {
        grantEmployeeWritePermission(PermissionScope.ALL);
        MockHttpSession session = login();

        mockMvc.perform(put("/api/employees/{id}", selfEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(updateRequest(selfEmployeeId, outsideDivisionId)))
                        .session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("삭제와 복구는 범위 안이면 허용하고 범위 밖이면 403을 반환한다")
    void should_controlDeleteAndRestoreByScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT_TREE);
        MockHttpSession session = login();

        mockMvc.perform(delete("/api/employees/{id}", childDepartmentEmployeeId).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/employees/{id}", outsideDepartmentEmployeeId).session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/employees/{id}/restore", childDepartmentEmployeeId).session(session))
                .andExpect(status().isNoContent());

        employeeManager.delete(employeeWriteActor(), outsideDepartmentEmployeeId, 1L);
        flushAndClear();

        mockMvc.perform(patch("/api/employees/{id}/restore", outsideDepartmentEmployeeId).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("상태 변경, 승진, 인사 변경은 범위 안이면 허용하고 범위 밖이면 403을 반환한다")
    void should_controlStatusChangesPromotionAndBusinessActionsByScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT_TREE);
        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/resign", sameDepartmentEmployeeId)
                        .param("resignationDate", "2025-06-30")
                        .session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/employees/{id}/resign", outsideDepartmentEmployeeId)
                        .param("resignationDate", "2025-06-30")
                        .session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/employees/{id}/take-leave", childDepartmentEmployeeId).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/employees/{id}/take-leave", outsideDepartmentEmployeeId).session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/employees/{id}/activate", childDepartmentEmployeeId).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/employees/{id}/activate", outsideDepartmentEmployeeId).session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/employees/{id}/promote", childDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EmployeePositionUpdateRequest(EmployeePosition.SENIOR_ASSOCIATE, null)))
                        .session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/employees/{id}/promote", outsideDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EmployeePositionUpdateRequest(EmployeePosition.SENIOR_ASSOCIATE, null)))
                        .session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/employees/{id}/transfer-department", childDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EmployeeDepartmentTransferRequest(ownDivisionId)))
                        .session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/employees/{id}/transfer-department", outsideDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EmployeeDepartmentTransferRequest(ownDivisionId)))
                        .session(session))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/employees/{id}/convert-employment-type", childDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EmployeeEmploymentTypeConvertRequest(EmployeeType.PART_TIME)))
                        .session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(patch("/api/employees/{id}/convert-employment-type", outsideDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EmployeeEmploymentTypeConvertRequest(EmployeeType.PART_TIME)))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("부서 이동이 발생하면 대상 계정의 기존 세션은 만료된다")
    void should_expireSessionAfterDepartmentTransfer() throws Exception {
        grantEmployeeWritePermission(PermissionScope.ALL);
        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/transfer-department", selfEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EmployeeDepartmentTransferRequest(outsideDivisionId)))
                        .session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위는 범위 밖 부서로의 이동을 막는다")
    void should_forbidTransferOutsideAllowedDepartment_whenGrantedOwnDepartmentScope() throws Exception {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(patch("/api/employees/{id}/transfer-department", sameDepartmentEmployeeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(new EmployeeDepartmentTransferRequest(outsideDivisionId)))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("대상이 없으면 기존처럼 404를 반환한다")
    void should_returnNotFound_whenWriteTargetDoesNotExist() throws Exception {
        grantEmployeeWritePermission(PermissionScope.ALL);
        MockHttpSession session = login();

        mockMvc.perform(delete("/api/employees/{id}", 99999L).session(session))
                .andExpect(status().isNotFound());

        mockMvc.perform(patch("/api/employees/{id}/restore", 99999L).session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("employee.excel.upload 권한이 없으면 엑셀 업로드는 403을 반환한다")
    void should_returnForbidden_whenUploadingExcelWithoutEmployeeExcelUploadPermission() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(multipart("/api/employees/excel/upload")
                        .file(createUploadFile(ownDivisionId, "upload-no-permission@abacus.co.kr"))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("employee.write 권한만 있으면 엑셀 업로드는 403을 반환한다")
    void should_returnForbidden_whenUploadingExcelWithOnlyEmployeeWritePermission() throws Exception {
        grantEmployeeWritePermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(multipart("/api/employees/excel/upload")
                        .file(createUploadFile(ownDivisionId, "upload-write-only@abacus.co.kr"))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("employee.excel.upload 권한만 있으면 직원 생성 API는 403을 반환한다")
    void should_returnForbidden_whenCreatingEmployeeWithOnlyEmployeeExcelUploadPermission() throws Exception {
        grantEmployeeExcelUploadPermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(createRequest(ownDivisionId, "create-upload-only@abacus.co.kr", "업로드전용")))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("OWN_DEPARTMENT 범위는 허용된 부서 엑셀 업로드만 허용한다")
    void should_allowExcelUploadOnlyInOwnDepartment_whenGrantedOwnDepartmentScope() throws Exception {
        grantEmployeeExcelUploadPermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(multipart("/api/employees/excel/upload")
                        .file(createUploadFile(ownDivisionId, "upload-own@abacus.co.kr"))
                        .session(session))
                .andExpect(status().isOk());

        flushAndClear();
        assertThat(employeeRepository.findByEmailAndDeletedFalse(new kr.co.abacus.abms.domain.shared.Email("upload-own@abacus.co.kr")))
                .isPresent();
    }

    @Test
    @DisplayName("범위 밖 부서가 포함된 엑셀 업로드는 전체를 403으로 거부한다")
    void should_forbidExcelUpload_whenAnyRowIsOutsideAllowedScope() throws Exception {
        grantEmployeeExcelUploadPermission(PermissionScope.OWN_DEPARTMENT);
        MockHttpSession session = login();

        mockMvc.perform(multipart("/api/employees/excel/upload")
                        .file(createUploadFile(ownDivisionId, "upload-mixed-own@abacus.co.kr", outsideDivisionId,
                                "upload-mixed-outside@abacus.co.kr"))
                        .session(session))
                .andExpect(status().isForbidden());

        flushAndClear();
        assertThat(employeeRepository.findByEmailAndDeletedFalse(new kr.co.abacus.abms.domain.shared.Email("upload-mixed-own@abacus.co.kr")))
                .isEmpty();
        assertThat(employeeRepository.findByEmailAndDeletedFalse(new kr.co.abacus.abms.domain.shared.Email("upload-mixed-outside@abacus.co.kr")))
                .isEmpty();
    }

    @Test
    @DisplayName("SELF 범위는 엑셀 업로드를 허용하지 않는다")
    void should_forbidExcelUpload_whenGrantedSelfScope() throws Exception {
        grantEmployeeExcelUploadPermission(PermissionScope.SELF);
        MockHttpSession session = login();

        mockMvc.perform(multipart("/api/employees/excel/upload")
                        .file(createUploadFile(ownDivisionId, "upload-self@abacus.co.kr"))
                        .session(session))
                .andExpect(status().isForbidden());
    }

    private void grantEmployeeWritePermission(PermissionScope... scopes) {
        grantEmployeePermission(
                "employee.write",
                "직원 쓰기",
                "직원 쓰기 권한",
                "직원 쓰기 그룹",
                "직원 쓰기 권한 그룹",
                scopes
        );
    }

    private void grantEmployeeExcelUploadPermission(PermissionScope... scopes) {
        grantEmployeePermission(
                "employee.excel.upload",
                "직원 엑셀 업로드",
                "직원 엑셀 업로드 권한",
                "직원 엑셀 업로드 그룹",
                "직원 엑셀 업로드 권한 그룹",
                scopes
        );
    }

    private void grantEmployeePermission(
            String permissionCode,
            String permissionName,
            String permissionDescription,
            String groupName,
            String groupDescription,
            PermissionScope... scopes
    ) {
        Account account = accountRepository.findByUsername(new kr.co.abacus.abms.domain.shared.Email(USERNAME)).orElseThrow();
        Permission permission = permissionRepository.save(Permission.create(
                permissionCode,
                permissionName,
                permissionDescription
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                groupName,
                groupDescription,
                PermissionGroupType.CUSTOM
        ));

        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));
        groupPermissionGrantRepository.saveAll(
                Set.of(scopes).stream()
                        .map(scope -> GroupPermissionGrant.create(
                                permissionGroup.getIdOrThrow(),
                                permission.getIdOrThrow(),
                                scope
                        ))
                        .toList()
        );
        flushAndClear();
    }

    private MockHttpSession login() throws Exception {
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private EmployeeCreateRequest createRequest(Long departmentId, String email, String name) {
        return new EmployeeCreateRequest(
                departmentId,
                email,
                name,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 5, 20),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );
    }

    private EmployeeUpdateRequest updateRequest(Long employeeId, Long departmentId) {
        Employee employee = employeeRepository.findByIdAndDeletedFalse(employeeId).orElseThrow();
        return new EmployeeUpdateRequest(
                departmentId,
                employee.getEmail().address(),
                employee.getName() + "수정",
                employee.getJoinDate(),
                employee.getBirthDate(),
                employee.getPosition(),
                employee.getType(),
                employee.getGrade(),
                employee.getAvatar(),
                employee.getMemo()
        );
    }

    private MockMultipartFile createUploadFile(Long departmentId, String email) throws Exception {
        return createUploadFile(departmentId, email, null, null);
    }

    private MockMultipartFile createUploadFile(Long firstDepartmentId, String firstEmail,
            Long secondDepartmentId, String secondEmail) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Employees");
        Row header = sheet.createRow(0);
        String[] headers = {
                "부서 코드", "이메일", "이름", "입사일", "생년월일", "직책", "근무 유형", "등급", "메모"
        };
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }

        writeUploadRow(sheet.createRow(1), firstDepartmentId, firstEmail);
        if (secondDepartmentId != null && secondEmail != null) {
            writeUploadRow(sheet.createRow(2), secondDepartmentId, secondEmail);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        return new MockMultipartFile(
                "file",
                "employees.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
        );
    }

    private void writeUploadRow(Row row, Long departmentId, String email) {
        String departmentCode = departmentRepository.findByIdAndDeletedFalse(departmentId)
                .map(Department::getCode)
                .orElseThrow();
        row.createCell(0).setCellValue(departmentCode);
        row.createCell(1).setCellValue(email);
        row.createCell(2).setCellValue("업로드직원");
        row.createCell(3).setCellValue("2025-01-02");
        row.createCell(4).setCellValue("1995-06-10");
        row.createCell(5).setCellValue(EmployeePosition.ASSOCIATE.getDescription());
        row.createCell(6).setCellValue(EmployeeType.FULL_TIME.getDescription());
        row.createCell(7).setCellValue(EmployeeGrade.JUNIOR.getDescription());
        row.createCell(8).setCellValue("");
    }

    private Employee createEmployee(Long departmentId, String email, String name) {
        return Employee.create(
                departmentId,
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

    private CurrentActor employeeWriteActor() {
        return new CurrentActor(
                1L,
                "employee-write-authorization-test",
                null,
                null,
                Map.of("employee.write", Set.of(PermissionScope.ALL))
        );
    }
}
