package kr.co.abacus.abms.adapter.api.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.relaxedResponseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeCreateRequest;
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
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("공통 오류 응답 문서화")
class ApiErrorDocumentationTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "error-docs-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private EmployeeRepository employeeRepository;

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

    private Long teamId;

    @BeforeEach
    void setUp() {
        Department team = departmentRepository.save(Department.create(
                "ERROR-DOCS-TEAM",
                "오류문서팀",
                DepartmentType.TEAM,
                null,
                null
        ));
        teamId = team.getIdOrThrow();

        Employee employee = employeeRepository.save(Employee.create(
                teamId,
                "오류문서사용자",
                USERNAME,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 1, 1),
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

        Permission readPermission = permissionRepository.save(Permission.create(
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
                "오류 문서 권한 그룹",
                "오류 문서화용 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));
        groupPermissionGrantRepository.saveAll(List.of(
                GroupPermissionGrant.create(permissionGroup.getIdOrThrow(), readPermission.getIdOrThrow(), PermissionScope.ALL),
                GroupPermissionGrant.create(permissionGroup.getIdOrThrow(), writePermission.getIdOrThrow(), PermissionScope.ALL)
        ));

        employeeRepository.save(Employee.create(
                teamId,
                "중복이메일직원",
                "duplicate-error@abacus.co.kr",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 2, 2),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        flushAndClear();
    }

    @Test
    @DisplayName("잘못된 요청의 공통 오류 응답을 문서화한다")
    void badRequest() throws Exception {
        MockHttpSession session = login();

        EmployeeCreateRequest request = new EmployeeCreateRequest(
                teamId,
                "duplicate-error@abacus.co.kr",
                "중복직원",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1995, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .session(session))
                .andDo(document("errors/bad-request",
                        relaxedResponseFields(
                                fieldWithPath("title").description("오류 제목"),
                                fieldWithPath("status").description("HTTP 상태 코드"),
                                fieldWithPath("detail").description("오류 상세 메시지"),
                                fieldWithPath("timestamp").description("오류 발생 시각"),
                                fieldWithPath("exception").description("예외 클래스명")
                        )))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리소스 없음의 공통 오류 응답을 문서화한다")
    void notFound() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/api/employees/{id}", 999999L).session(session))
                .andDo(document("errors/not-found",
                        pathParameters(
                                parameterWithName("id").description("존재하지 않는 직원 ID")
                        ),
                        relaxedResponseFields(
                                fieldWithPath("title").description("오류 제목"),
                                fieldWithPath("status").description("HTTP 상태 코드"),
                                fieldWithPath("detail").description("오류 상세 메시지"),
                                fieldWithPath("timestamp").description("오류 발생 시각"),
                                fieldWithPath("exception").description("예외 클래스명")
                        )))
                .andExpect(status().isNotFound());
    }

    private MockHttpSession login() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();
        return session;
    }

}
