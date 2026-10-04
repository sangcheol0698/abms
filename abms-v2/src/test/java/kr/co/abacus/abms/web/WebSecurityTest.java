package kr.co.abacus.abms.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.account.AccountRepository;
import kr.co.abacus.abms.account.AccountService;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class WebSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        Department team = fixtures.department("팀", null);
        employee = fixtures.employee(team, "사용자");
    }

    @Test
    void 로그인_페이지는_누구나_볼_수_있다() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("프로젝트 손익 관리 시스템")));
    }

    @Test
    void 일반_POST_폼에는_CSRF_hidden_필드가_있어_스크립트_없이도_제출된다() throws Exception {
        mvc.perform(get("/parties/new").with(user(Fixtures.admin(employee))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.matchesPattern(
                        "(?s).*<form method=\"post\" action=\"/parties\"[^>]*><input type=\"hidden\" name=\"_csrf\" value=\"[^\"]+\">.*")));
    }

    @Test
    void 응답마다_nonce_기반_CSP를_보내고_스크립트_태그에_같은_nonce를_붙인다() throws Exception {
        var result = mvc.perform(get("/login")).andExpect(status().isOk()).andReturn();
        String policy = result.getResponse().getHeader("Content-Security-Policy");
        org.assertj.core.api.Assertions.assertThat(policy).contains("'strict-dynamic'", "object-src 'none'").doesNotContain("unsafe-eval");
        String nonce = policy.replaceAll("(?s).*'nonce-([^']+)'.*", "$1");
        org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsString())
                .contains("<script nonce=\"" + nonce + "\"").doesNotContain("onclick=");
    }

    @Test
    void 비로그인_사용자는_로그인_페이지로_이동한다() throws Exception {
        mvc.perform(get("/employees"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void 비로그인_HTMX_요청은_HX_Redirect_로_응답한다() throws Exception {
        mvc.perform(get("/employees").header("HX-Request", "true"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("HX-Redirect", "/login"));
    }

    @Test
    void 발급한_임시_비밀번호로_로그인하고_실패가_누적되면_잠긴다() throws Exception {
        AccountService.IssuedAccount issued = accountService.issue(employee.id());
        String username = issued.account().getUsername();

        mvc.perform(formLogin("/login").user(username).password(issued.temporaryPassword()))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/"));

        for (int i = 0; i < 5; i++) {
            mvc.perform(formLogin("/login").user(username).password("wrong-password"))
                    .andExpect(unauthenticated());
        }
        accountRepository.flush();
        mvc.perform(formLogin("/login").user(username).password(issued.temporaryPassword()))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?locked"));
    }

    @Test
    void CSRF_토큰_없는_변경_요청은_거부한다() throws Exception {
        LoginUser admin = Fixtures.admin(employee);
        mvc.perform(post("/parties").with(user(admin)).param("name", "새 협력사"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 관리자_화면은_해당_권한이_있어야_한다() throws Exception {
        LoginUser member = Fixtures.user(employee, java.util.Map.of());
        mvc.perform(get("/admin/accounts").with(user(member))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/permission-groups").with(user(member))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/accounts").with(user(Fixtures.admin(employee)))).andExpect(status().isOk());
    }

    @Test
    void 대시보드_권한이_없으면_내_정보로_이동한다() throws Exception {
        LoginUser member = Fixtures.user(employee, java.util.Map.of());
        mvc.perform(get("/").with(user(member)))
                .andExpect(redirectedUrl("/me"));
    }

    @Test
    void 화면_안_이동_boost_으로_권한_없는_화면에_가면_화면은_그대로_두고_필요한_권한을_토스트로_알린다() throws Exception {
        var plain = Fixtures.user(employee, Fixtures.grants(kr.co.abacus.abms.access.PermissionScope.SELF,
                kr.co.abacus.abms.access.PermissionCode.EMPLOYEE_READ));

        // 보안 필터(URL)에서 막힌 경우
        mvc.perform(get("/admin/accounts").with(user(plain)).header("HX-Request", "true").header("HX-Boosted", "true"))
                .andExpect(status().isForbidden())
                .andExpect(header().string("HX-Reswap", "none"))
                .andExpect(header().string("HX-Trigger", containsString("toast")));

        // 컨트롤러에서 막힌 경우
        mvc.perform(get("/sites/new").with(user(plain)).header("HX-Request", "true").header("HX-Boosted", "true"))
                .andExpect(status().isForbidden())
                .andExpect(header().string("HX-Reswap", "none"))
                .andExpect(header().string("HX-Trigger", containsString("toast")));
    }

}
