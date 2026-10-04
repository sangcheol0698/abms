package kr.co.abacus.abms.party;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.party.PartyContact.ContactInfo;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class PartyContactTest {

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private PartyService partyService;

    @Autowired
    private MockMvc mvc;

    private Party party;
    private LoginUser admin;
    private LoginUser reader;

    @BeforeEach
    void setUp() {
        Department team = fixtures.department("담당팀", null);
        Employee employee = fixtures.employee(team, "담당관리");
        admin = Fixtures.admin(employee);
        reader = Fixtures.user(employee, Fixtures.grants(PermissionScope.ALL, PermissionCode.PARTY_READ));
        party = fixtures.party("담당상사");
    }

    @Test
    void 담당자를_역할별로_여러_명_두고_대표는_한_명만_유지한다() {
        PartyContact sales = partyService.addContact(party.id(), new ContactInfo("김영업", ContactRole.SALES, null, null, null, null, true));
        PartyContact billing = partyService.addContact(party.id(), new ContactInfo("이정산", ContactRole.BILLING, null, null, null, null, false));

        partyService.updateContact(party.id(), billing.id(), new ContactInfo("이정산", ContactRole.BILLING, "재무팀", null, null, null, true));

        List<PartyContact> contacts = partyService.contacts(party.id());
        assertThat(contacts).extracting(PartyContact::getName).containsExactly("이정산", "김영업");
        assertThat(contacts).filteredOn(PartyContact::isPrimary).extracting(PartyContact::getName).containsExactly("이정산");
        assertThat(partyService.primaryContacts(List.of(party.id())).get(party.id()).getName()).isEqualTo("이정산");
        assertThat(sales.isPrimary()).isFalse();
    }

    @Test
    void 모달로_추가하고_목록과_상세에_대표_담당자를_보여준다() throws Exception {
        mvc.perform(post("/parties/{id}/contacts", party.id()).with(user(admin)).with(csrf()).header("HX-Request", "true")
                        .param("name", "박기술").param("role", "TECH").param("email", "tech@example.com").param("primary", "true"))
                .andExpect(status().isOk());

        mvc.perform(get("/parties/{id}", party.id()).with(user(admin)))
                .andExpect(content().string(containsString("박기술")))
                .andExpect(content().string(containsString("대표 담당자")));
        mvc.perform(get("/parties").with(user(admin)))
                .andExpect(content().string(containsString("tech@example.com")));
    }

    @Test
    void 이메일_형식이_틀리면_422로_모달을_다시_그린다() throws Exception {
        mvc.perform(post("/parties/{id}/contacts", party.id()).with(user(admin)).with(csrf()).header("HX-Request", "true")
                        .param("name", "오류").param("role", "ETC").param("email", "not-an-email"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().string(containsString("이메일 형식")));
    }

    @Test
    void 협력사_관리_권한이_없으면_담당자를_바꿀_수_없다() throws Exception {
        mvc.perform(post("/parties/{id}/contacts", party.id()).with(user(reader)).with(csrf()).param("name", "몰래"))
                .andExpect(status().isForbidden());
    }

}
