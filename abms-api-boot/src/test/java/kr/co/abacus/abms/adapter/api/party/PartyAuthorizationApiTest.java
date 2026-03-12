package kr.co.abacus.abms.adapter.api.party;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
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
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("협력사 API 권한 인가")
class PartyAuthorizationApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "party-auth-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

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

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private Long partyId;

    @BeforeEach
    void setUpAccount() {
        Department department = departmentRepository.save(Department.create(
                "PARTY-AUTH",
                "협력사인가부서",
                DepartmentType.TEAM,
                null,
                null
        ));

        Employee employee = employeeRepository.save(Employee.create(
                department.getIdOrThrow(),
                "협력사인가사용자",
                USERNAME,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1990, 5, 20),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        accountRepository.save(Account.create(employee.getIdOrThrow(), USERNAME, passwordEncoder.encode(PASSWORD)));

        Party party = partyRepository.save(Party.create(new PartyCreateRequest("협력사권한테스트", null, null, null, null)));
        partyId = party.getIdOrThrow();
        Project project = ProjectFixture.createProject(partyId);
        projectRepository.save(project);
        flushAndClear();
    }

    @Test
    @DisplayName("party.read 권한이 없으면 협력사 목록 조회는 403을 반환한다")
    void should_forbidPartySearchWithoutPartyReadPermission() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(get("/api/parties").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("party.read 권한이 있으면 협력사 목록 조회는 200을 반환한다")
    void should_allowPartySearchWithPartyReadPermission() throws Exception {
        grantPermission("party.read", "협력사 조회", "협력사 조회 권한");
        MockHttpSession session = login();

        mockMvc.perform(get("/api/parties").session(session))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("party.write 권한이 없으면 협력사 생성은 403을 반환한다")
    void should_forbidPartyCreateWithoutPartyWritePermission() throws Exception {
        grantPermission("party.read", "협력사 조회", "협력사 조회 권한");
        MockHttpSession session = login();

        mockMvc.perform(post("/api/parties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "name", "신규협력사",
                                "ceoName", "대표",
                                "salesRepName", "담당자",
                                "salesRepPhone", "010-1234-5678",
                                "salesRepEmail", "party@abacus.co.kr"
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("party.write 권한이 있으면 협력사 생성은 201을 반환한다")
    void should_allowPartyCreateWithPartyWritePermission() throws Exception {
        grantPermission("party.write", "협력사 변경", "협력사 변경 권한");
        MockHttpSession session = login();

        mockMvc.perform(post("/api/parties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "name", "신규협력사",
                                "ceoName", "대표",
                                "salesRepName", "담당자",
                                "salesRepPhone", "010-1234-5678",
                                "salesRepEmail", "party@abacus.co.kr"
                        ))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("party.read 권한만 있으면 협력사 관련 프로젝트 조회는 403을 반환한다")
    void should_forbidPartyProjectsWithoutProjectReadPermission() throws Exception {
        grantPermission("party.read", "협력사 조회", "협력사 조회 권한");
        MockHttpSession session = login();

        mockMvc.perform(get("/api/parties/{id}/projects", partyId).session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("party.read와 project.read 권한이 있으면 협력사 관련 프로젝트 조회는 200을 반환한다")
    void should_allowPartyProjectsWithPartyReadAndProjectReadPermission() throws Exception {
        grantPermission("party.read", "협력사 조회", "협력사 조회 권한");
        grantPermission("project.read", "프로젝트 조회", "프로젝트 조회 권한");
        MockHttpSession session = login();

        mockMvc.perform(get("/api/parties/{id}/projects", partyId).session(session))
                .andExpect(status().isOk());
    }

    private void grantPermission(String code, String name, String description) {
        Account account = accountRepository.findByUsername(new kr.co.abacus.abms.domain.shared.Email(USERNAME)).orElseThrow();
        Permission permission = permissionRepository.save(Permission.create(code, name, description));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                code + " 그룹",
                code + " 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.ALL
        ));
        flushAndClear();
    }

    private MockHttpSession login() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
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
}
