package kr.co.abacus.abms.adapter.api.auth;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;

import kr.co.abacus.abms.adapter.security.CustomUserDetails;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.auth.outbound.DefaultPermissionGroupRepository;
import kr.co.abacus.abms.application.auth.outbound.PasswordResetTokenRepository;
import kr.co.abacus.abms.application.auth.outbound.RegistrationTokenRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.auth.PasswordResetToken;
import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;
import kr.co.abacus.abms.domain.auth.RegistrationToken;
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
import kr.co.abacus.abms.domain.shared.Email;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("인증 API (AuthApi)")
class AuthApiTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "auth-user@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RegistrationTokenRepository registrationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private DefaultPermissionGroupRepository defaultPermissionGroupRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private GroupPermissionGrantRepository groupPermissionGrantRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @BeforeEach
    void setUpAccount() {
        Employee employee = employeeRepository.save(createEmployee(USERNAME, "인증사용자"));
        accountRepository.save(Account.create(employee.getIdOrThrow(), USERNAME, passwordEncoder.encode(PASSWORD)));
        flushAndClear();
    }

    @Test
    @DisplayName("로그인하지 않으면 보호된 API에 접근할 수 없다")
    void should_requireAuthentication() throws Exception {
        mockMvc.perform(get("/api/employees/positions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인에 성공하면 세션으로 보호된 API에 접근할 수 있다")
    void should_loginAndAccessProtectedApi() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andDo(document("auth/login",
                        requestFields(
                                fieldWithPath("username").description("로그인에 사용할 계정 이메일"),
                                fieldWithPath("password").description("로그인 비밀번호")
                        )))
                .andExpect(status().isOk())
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);
        assertThat(session).isNotNull();

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 후 현재 사용자 정보를 조회할 수 있다")
    void should_getCurrentUser_whenAuthenticated() throws Exception {
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

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USERNAME))
                .andExpect(jsonPath("$.name").value("인증사용자"))
                .andExpect(jsonPath("$.permissions").isArray())
                .andExpect(jsonPath("$.permissions").isEmpty());
    }

    @Test
    @DisplayName("로그인 후 현재 사용자 권한 정보를 정렬된 형식으로 조회할 수 있다")
    void should_getCurrentUserPermissions_whenAuthenticated() throws Exception {
        Account account = accountRepository.findByUsername(new Email(USERNAME)).orElseThrow();
        Permission employeeReadPermission = permissionRepository.save(Permission.create(
                "employee.read",
                "직원 조회",
                "직원 조회 권한"
        ));
        Permission employeeManagePermission = permissionRepository.save(Permission.create(
                "employee.manage",
                "직원 관리",
                "직원 관리 권한"
        ));

        PermissionGroup readPermissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 조회 그룹",
                "직원 조회 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        PermissionGroup managePermissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 관리 그룹",
                "직원 관리 권한 그룹",
                PermissionGroupType.CUSTOM
        ));

        accountGroupAssignmentRepository.saveAll(List.of(
                AccountGroupAssignment.create(account.getIdOrThrow(), readPermissionGroup.getIdOrThrow()),
                AccountGroupAssignment.create(account.getIdOrThrow(), managePermissionGroup.getIdOrThrow())
        ));
        groupPermissionGrantRepository.saveAll(List.of(
                GroupPermissionGrant.create(readPermissionGroup.getIdOrThrow(),
                        employeeReadPermission.getIdOrThrow(),
                        PermissionScope.SELF),
                GroupPermissionGrant.create(managePermissionGroup.getIdOrThrow(),
                        employeeReadPermission.getIdOrThrow(),
                        PermissionScope.OWN_DEPARTMENT),
                GroupPermissionGrant.create(managePermissionGroup.getIdOrThrow(),
                        employeeManagePermission.getIdOrThrow(),
                        PermissionScope.ALL)
        ));
        flushAndClear();

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

        mockMvc.perform(get("/api/auth/me").session(session))
                .andDo(document("auth/me",
                        responseFields(
                                fieldWithPath("name").description("현재 사용자 이름"),
                                fieldWithPath("email").description("현재 사용자 이메일"),
                                fieldWithPath("employeeId").description("현재 사용자 직원 ID").optional(),
                                fieldWithPath("departmentId").description("현재 사용자 부서 ID").optional(),
                                fieldWithPath("permissions").description("현재 사용자 권한 목록"),
                                fieldWithPath("permissions[].code").description("권한 코드"),
                                fieldWithPath("permissions[].scopes").description("권한 범위 목록"),
                                fieldWithPath("permissions[].scopes[]").description("개별 권한 범위")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USERNAME))
                .andExpect(jsonPath("$.name").value("인증사용자"))
                .andExpect(jsonPath("$.employeeId").isNumber())
                .andExpect(jsonPath("$.departmentId").isNumber())
                .andExpect(jsonPath("$.permissions[0].code").value("employee.manage"))
                .andExpect(jsonPath("$.permissions[0].scopes[0]").value("ALL"))
                .andExpect(jsonPath("$.permissions[1].code").value("employee.read"))
                .andExpect(jsonPath("$.permissions[1].scopes[0]").value("OWN_DEPARTMENT"))
                .andExpect(jsonPath("$.permissions[1].scopes[1]").value("SELF"));
    }

    @Test
    @DisplayName("로그인 세션 principal에는 계정과 권한 스냅샷이 저장된다")
    void should_storePrincipalSnapshotInSession() throws Exception {
        Account account = accountRepository.findByUsername(new Email(USERNAME)).orElseThrow();
        Permission employeeReadPermission = permissionRepository.save(Permission.create(
                "employee.read",
                "직원 조회",
                "직원 조회 권한"
        ));
        PermissionGroup readPermissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "직원 조회 스냅샷 그룹",
                "직원 조회 스냅샷 권한 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                readPermissionGroup.getIdOrThrow()
        ));
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                readPermissionGroup.getIdOrThrow(),
                employeeReadPermission.getIdOrThrow(),
                PermissionScope.SELF
        ));
        flushAndClear();

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

        SecurityContext securityContext = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(securityContext).isNotNull();
        assertThat(securityContext.getAuthentication().getPrincipal()).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails principal = (CustomUserDetails) securityContext.getAuthentication().getPrincipal();
        assertThat(principal.getAccountId()).isNotNull();
        assertThat(principal.getEmployeeId()).isNotNull();
        assertThat(principal.getDepartmentId()).isNotNull();
        assertThat(principal.getPermissionsByCode()).containsKey("employee.read");
        assertThat(principal.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .contains("employee.read");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인에 실패한다")
    void should_failLogin_whenPasswordIsWrong() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", "WrongPassword!"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인한 사용자는 현재 비밀번호 확인 후 비밀번호를 변경할 수 있다")
    void should_changePassword_whenCurrentPasswordMatches() throws Exception {
        Account account = accountRepository.findByUsername(new Email(USERNAME)).orElseThrow();
        LocalDateTime oldPasswordChangedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        ReflectionTestUtils.setField(account, "passwordChangedAt", oldPasswordChangedAt);
        accountRepository.save(account);
        flushAndClear();

        mockMvc.perform(patch("/api/auth/password")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "currentPassword", PASSWORD,
                                "newPassword", "ChangedPassword123!"
                        ))))
                .andDo(document("auth/change-password",
                        requestFields(
                                fieldWithPath("currentPassword").description("현재 비밀번호"),
                                fieldWithPath("newPassword").description("새 비밀번호")
                        )))
                .andExpect(status().isOk());

        flushAndClear();

        Account changed = accountRepository.findByUsername(new Email(USERNAME)).orElseThrow();
        assertThat(passwordEncoder.matches("ChangedPassword123!", changed.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(PASSWORD, changed.getPassword())).isFalse();
        assertThat(changed.getPasswordChangedAt()).isAfter(oldPasswordChangedAt);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", "ChangedPassword123!"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인하지 않으면 비밀번호를 변경할 수 없다")
    void should_requireAuthentication_whenChangingPassword() throws Exception {
        mockMvc.perform(patch("/api/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "currentPassword", PASSWORD,
                                "newPassword", "ChangedPassword123!"
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 비밀번호 변경에 실패한다")
    void should_failChangePassword_whenCurrentPasswordIsWrong() throws Exception {
        mockMvc.perform(patch("/api/auth/password")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "currentPassword", "WrongPassword123!",
                                "newPassword", "ChangedPassword123!"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새 비밀번호가 정책에 맞지 않으면 비밀번호 변경에 실패한다")
    void should_failChangePassword_whenNewPasswordIsWeak() throws Exception {
        mockMvc.perform(patch("/api/auth/password")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "currentPassword", PASSWORD,
                                "newPassword", "password1234"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새 비밀번호가 현재 비밀번호와 같으면 비밀번호 변경에 실패한다")
    void should_failChangePassword_whenNewPasswordMatchesCurrentPassword() throws Exception {
        mockMvc.perform(patch("/api/auth/password")
                        .with(user(USERNAME))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "currentPassword", PASSWORD,
                                "newPassword", PASSWORD
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이메일 형식이 아니면 400을 반환한다")
    void should_returnBadRequest_whenUsernameIsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", "invalid-email",
                                "password", PASSWORD
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 요청 시 토큰을 생성한다")
    void should_requestRegistration() throws Exception {
        String email = "new-user@iabacus.co.kr";
        employeeRepository.save(createEmployee(email, "신규직원"));
        flushAndClear();

        mockMvc.perform(post("/api/auth/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email))))
                .andDo(document("auth/registration-request",
                        requestFields(
                                fieldWithPath("email").description("회원가입을 요청할 회사 이메일")
                        )))
                .andExpect(status().isOk());

        RegistrationToken registrationToken = registrationTokenRepository
                .findFirstByEmailOrderByCreatedAtDesc(new Email(email))
                .orElseThrow();

        assertThat(registrationToken.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("회원가입 확정 시 계정을 생성한다")
    void should_confirmRegistration() throws Exception {
        String email = "confirmed-user@iabacus.co.kr";
        String newPassword = "NewPassword123!";
        employeeRepository.save(createEmployee(email, "확정직원"));
        flushAndClear();

        mockMvc.perform(post("/api/auth/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email))))
                .andExpect(status().isOk());

        RegistrationToken registrationToken = registrationTokenRepository
                .findFirstByEmailOrderByCreatedAtDesc(new Email(email))
                .orElseThrow();

        mockMvc.perform(post("/api/auth/registration-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "token", registrationToken.getToken(),
                                "password", newPassword
                        ))))
                .andDo(document("auth/registration-confirmation",
                        requestFields(
                                fieldWithPath("token").description("회원가입 확정 토큰"),
                                fieldWithPath("password").description("설정할 비밀번호")
                        )))
                .andExpect(status().isOk());
        flushAndClear();

        // 회원 가입 시 권한 그룹이 일반 그룹으로 기본 지정된다.
        Account account = accountRepository.findByUsername(new Email(email)).orElseThrow();
        PermissionGroup defaultPermissionGroup = defaultPermissionGroupRepository.findByGroupTypeAndNameAndDeletedFalse(
                PermissionGroupType.SYSTEM,
                "일반 그룹"
        ).orElseThrow();
        List<AccountGroupAssignment> assignments =
                accountGroupAssignmentRepository.findAllByAccountIdAndDeletedFalse(account.getIdOrThrow());

        assertThat(passwordEncoder.matches(newPassword, account.getPassword())).isTrue();
        assertThat(assignments)
                .extracting(AccountGroupAssignment::getPermissionGroupId)
                .containsExactly(defaultPermissionGroup.getIdOrThrow());
        assertThat(registrationTokenRepository.findByToken(registrationToken.getToken())).isEmpty();
    }

    @Test
    @DisplayName("회원가입 확정 토큰이 유효하지 않으면 400을 반환한다")
    void should_returnBadRequest_whenTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/registration-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "token", "invalid-token",
                                "password", "Password123!"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 확정 비밀번호가 정책에 맞지 않으면 400을 반환한다")
    void should_returnBadRequest_whenPasswordIsWeak() throws Exception {
        String email = "weak-password-user@iabacus.co.kr";
        employeeRepository.save(createEmployee(email, "비번약함직원"));
        flushAndClear();

        mockMvc.perform(post("/api/auth/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email))))
                .andExpect(status().isOk());

        RegistrationToken registrationToken = registrationTokenRepository
                .findFirstByEmailOrderByCreatedAtDesc(new Email(email))
                .orElseThrow();

        mockMvc.perform(post("/api/auth/registration-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "token", registrationToken.getToken(),
                                "password", "password1234"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 시 항상 성공 응답을 반환하고 가입 계정이면 토큰을 생성한다")
    void should_requestPasswordReset() throws Exception {
        String resetEmail = "reset-user@iabacus.co.kr";
        Employee employee = employeeRepository.save(createEmployee(resetEmail, "재설정요청직원"));
        accountRepository.save(Account.create(employee.getIdOrThrow(), resetEmail, passwordEncoder.encode(PASSWORD)));
        flushAndClear();

        mockMvc.perform(post("/api/auth/password-reset-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", resetEmail))))
                .andDo(document("auth/password-reset-request",
                        requestFields(
                                fieldWithPath("email").description("비밀번호 재설정을 요청할 회사 이메일")
                        )))
                .andExpect(status().isOk());

        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findFirstByEmailOrderByCreatedAtDesc(new Email(resetEmail))
                .orElseThrow();

        assertThat(passwordResetToken.getToken()).isNotBlank();

        mockMvc.perform(post("/api/auth/password-reset-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "missing@iabacus.co.kr"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 재설정 토큰으로 새 비밀번호를 설정할 수 있다")
    void should_confirmPasswordReset() throws Exception {
        Account account = accountRepository.findByUsername(new Email(USERNAME)).orElseThrow();
        passwordResetTokenRepository.save(PasswordResetToken.create(
                account.getIdOrThrow(),
                USERNAME,
                "api-reset-token",
                LocalDateTime.now().plusMinutes(30)
        ));
        flushAndClear();

        mockMvc.perform(post("/api/auth/password-reset-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "token", "api-reset-token",
                                "password", "ResetPassword123!"
                        ))))
                .andDo(document("auth/password-reset-confirmation",
                        requestFields(
                                fieldWithPath("token").description("비밀번호 재설정 토큰"),
                                fieldWithPath("password").description("새 비밀번호")
                        )))
                .andExpect(status().isOk());
        flushAndClear();

        Account changed = accountRepository.findByUsername(new Email(USERNAME)).orElseThrow();
        assertThat(passwordEncoder.matches("ResetPassword123!", changed.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(PASSWORD, changed.getPassword())).isFalse();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", "ResetPassword123!"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효하지 않은 비밀번호 재설정 토큰이면 400을 반환한다")
    void should_returnBadRequest_whenPasswordResetTokenIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/password-reset-confirmations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "token", "invalid-reset-token",
                                "password", "ResetPassword123!"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 요청 이메일이 회사 도메인이 아니면 400을 반환한다")
    void should_returnBadRequest_whenRegistrationEmailIsNotCompanyDomain() throws Exception {
        mockMvc.perform(post("/api/auth/registration-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", "new-user@abacus.co.kr"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인한 사용자는 로그아웃할 수 있다")
    void should_logout_whenAuthenticated() throws Exception {
        MockHttpSession session = login();

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andDo(document("auth/logout"))
                .andExpect(status().isNoContent());
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
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

    private Employee createEmployee(String email, String name) {
        return Employee.create(
                1L,
                name,
                email,
                LocalDate.of(2025, 1, 2),
                LocalDate.of(1995, 6, 10),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );
    }

}
