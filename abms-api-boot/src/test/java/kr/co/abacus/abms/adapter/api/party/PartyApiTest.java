package kr.co.abacus.abms.adapter.api.party;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.party.dto.PartyResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectResponse;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.party.dto.PartyOverviewSummary;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
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

import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

@DisplayName("협력사 API (PartyApi)")
class PartyApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "party-api-user@abacus.co.kr";
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
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    void setUpAccount() {
        Department department = departmentRepository.save(Department.create(
                "PARTY-API",
                "협력사API부서",
                DepartmentType.TEAM,
                null,
                null
        ));
        Employee employee = employeeRepository.save(Employee.create(
                department.getIdOrThrow(),
                "협력사API사용자",
                USERNAME,
                java.time.LocalDate.of(2024, 1, 1),
                java.time.LocalDate.of(1990, 5, 20),
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
                "협력사 API 권한 그룹",
                "협력사 API 테스트용 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                permissionGroup.getIdOrThrow()
        ));

        grant(permissionGroup, "party.read", "협력사 조회");
        grant(permissionGroup, "party.write", "협력사 변경");
        grant(permissionGroup, "project.read", "프로젝트 조회");
        flushAndClear();
    }

    @Test
    @DisplayName("협력사 목록 조회 - 페이징")
    void list_withPaging() throws Exception {
        // Given: 15개의 협력사 생성
        for (int i = 1; i <= 15; i++) {
            Party party = Party.create(new PartyCreateRequest(
                    "협력사 " + i,
                    "대표이사 " + i,
                    "담당자 " + i,
                    "010-0000-000" + i,
                    "contact" + i + "@example.com"));
            partyRepository.save(party);
        }
        flushAndClear();

        // When & Then: 첫 번째 페이지 (size=10)
        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/parties")
                        .session(session)
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(document("party/list",
                        queryParameters(
                                parameterWithName("page").description("조회할 페이지 번호"),
                                parameterWithName("size").description("페이지 크기")
                        ),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(10))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(15))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.last").value(false));
    }

    @Test
    @DisplayName("협력사 목록 조회 - 응답 구조 확인")
    void list_responseStructure() throws Exception {
        // Given
        Party party = Party.create(new PartyCreateRequest(
                "테스트 협력사",
                "홍길동",
                "김담당",
                "010-1234-5678",
                "contact@test.com"));
        partyRepository.save(party);
        flushAndClear();

        // When & Then
        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/parties")
                        .session(session)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value("테스트 협력사"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].ceo").value("홍길동"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].manager").value("김담당"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].contact").value("010-1234-5678"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].email").value("contact@test.com"));
    }

    @Test
    @DisplayName("협력사 프로젝트 조회")
    void list_projectsByParty() throws Exception {
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(
                "테스트 협력사",
                "홍길동",
                "김담당",
                "010-1234-5678",
                "contact@test.com")));
        Party otherParty = partyRepository.save(Party.create(new PartyCreateRequest(
                "다른 협력사",
                "임꺽정",
                "박담당",
                "010-0000-0000",
                "contact2@test.com")));

        projectRepository.save(ProjectFixture.createProject(
                "PRJ-PARTY-001", "협력사 프로젝트 1", party.getId(), 1L));
        projectRepository.save(ProjectFixture.createProject(
                "PRJ-PARTY-002", "협력사 프로젝트 2", party.getId(), 1L));
        projectRepository.save(ProjectFixture.createProject(
                "PRJ-OTHER-001", "다른 협력사 프로젝트", otherParty.getId(), 1L));
        flushAndClear();

        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.get("/api/parties/{id}/projects", party.getId()).session(session))
                .andDo(document("party/projects",
                        pathParameters(
                                parameterWithName("id").description("협력사 ID")
                        ),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].partyId").value(party.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[*].code").isArray());
    }

    @Test
    @DisplayName("협력사 요약 정보를 조회한다")
    void summary() throws Exception {
        Party alpha = partyRepository.save(Party.create(new PartyCreateRequest(
                "요약 협력사 Alpha",
                "홍길동",
                "김담당",
                "010-1234-5678",
                "contact@test.com")));
        Party beta = partyRepository.save(Party.create(new PartyCreateRequest(
                "요약 협력사 Beta",
                "홍길동",
                "김담당",
                "010-1234-5678",
                "contact@test.com")));
        partyRepository.save(Party.create(new PartyCreateRequest(
                "요약 협력사 Gamma",
                "홍길동",
                "김담당",
                "010-1234-5678",
                "contact@test.com")));

        projectRepository.save(createProject("PRJ-PARTY-SUM-API-001", alpha.getId(), ProjectStatus.IN_PROGRESS, 100_000_000L));
        projectRepository.save(createProject("PRJ-PARTY-SUM-API-002", beta.getId(), ProjectStatus.COMPLETED, 200_000_000L));
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/parties/summary")
                        .session(session)
                        .param("name", "요약 협력사"))
                .andDo(document("party/summary",
                        queryParameters(
                                parameterWithName("name").description("협력사명 검색어").optional()
                        ),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        PartyOverviewSummary response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), PartyOverviewSummary.class);

        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isEqualTo(3);
        assertThat(response.withProjectsCount()).isEqualTo(2);
        assertThat(response.withInProgressProjectsCount()).isEqualTo(1);
        assertThat(response.withoutProjectsCount()).isEqualTo(1);
        assertThat(response.totalContractAmount()).isEqualTo(300_000_000L);
    }

    @Test
    @DisplayName("협력사 상세를 조회한다")
    void get() throws Exception {
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(
                "상세 협력사",
                "대표자",
                "담당자",
                "010-1111-2222",
                "detail@test.com"
        )));
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/parties/{id}", party.getId()).session(session))
                .andDo(document("party/get",
                        pathParameters(
                                parameterWithName("id").description("조회할 협력사 ID")
                        ),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        PartyResponse response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), PartyResponse.class);
        assertThat(response.partyId()).isEqualTo(party.getId());
        assertThat(response.name()).isEqualTo("상세 협력사");
    }

    @Test
    @DisplayName("협력사를 생성한다")
    void create() throws Exception {
        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/parties")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "생성 협력사",
                                "ceoName", "생성 대표",
                                "salesRepName", "생성 담당",
                                "salesRepPhone", "010-3333-4444",
                                "salesRepEmail", "create@test.com"
                        ))))
                .andDo(document("party/create",
                        requestBody(),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();

        PartyResponse response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), PartyResponse.class);
        assertThat(response.name()).isEqualTo("생성 협력사");
    }

    @Test
    @DisplayName("협력사를 수정한다")
    void update() throws Exception {
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(
                "수정 전 협력사",
                "수정 전 대표",
                "수정 전 담당",
                "010-5555-6666",
                "before@test.com"
        )));
        flushAndClear();

        MockHttpSession session = login();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/api/parties/{id}", party.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "수정 후 협력사",
                                "ceoName", "수정 후 대표",
                                "salesRepName", "수정 후 담당",
                                "salesRepPhone", "010-7777-8888",
                                "salesRepEmail", "after@test.com"
                        ))))
                .andDo(document("party/update",
                        pathParameters(
                                parameterWithName("id").description("수정할 협력사 ID")
                        ),
                        requestBody(),
                        responseBody()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        PartyResponse response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), PartyResponse.class);
        assertThat(response.name()).isEqualTo("수정 후 협력사");
    }

    @Test
    @DisplayName("협력사를 삭제한다")
    void delete() throws Exception {
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(
                "삭제 협력사",
                null,
                null,
                null,
                null
        )));
        flushAndClear();

        MockHttpSession session = login();
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/parties/{id}", party.getId()).session(session))
                .andDo(document("party/delete",
                        pathParameters(
                                parameterWithName("id").description("삭제할 협력사 ID")
                        )))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    private Project createProject(String code, Long partyId, ProjectStatus status, long contractAmount) {
        return Project.create(
                partyId,
                1L,
                code,
                code,
                "테스트 프로젝트 설명",
                status,
                contractAmount,
                java.time.LocalDate.of(2024, 1, 1),
                java.time.LocalDate.of(2024, 12, 31)
        );
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
