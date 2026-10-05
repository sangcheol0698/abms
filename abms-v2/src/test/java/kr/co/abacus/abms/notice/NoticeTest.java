package kr.co.abacus.abms.notice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.notice.Notice.NoticeInfo;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class NoticeTest {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private MockMvc mvc;

    private LoginUser admin;
    private LoginUser member;

    @BeforeEach
    void setUp() {
        noticeRepository.deleteAll();
        Department team = fixtures.department("공지팀", null);
        Employee writer = fixtures.employee(team, "작성자");
        Employee reader = fixtures.employee(team, "직원");
        admin = Fixtures.admin(writer);
        member = Fixtures.user(reader, Fixtures.grants(PermissionScope.SELF, PermissionCode.EMPLOYEE_READ));
    }

    private Notice notice(String title, boolean popup, NoticeImportance importance, LocalDateTime startsAt, LocalDateTime endsAt) {
        return noticeService.create(admin, new NoticeInfo(title, "본문 **강조**", importance, false, popup, startsAt, endsAt));
    }

    @Test
    void 게시_종료는_시작보다_늦어야_하고_제목과_본문은_필수다() {
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> notice("기간 오류", false, NoticeImportance.NORMAL, now, now.minusHours(1)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("게시 종료");
        assertThatThrownBy(() -> noticeService.create(admin, new NoticeInfo(" ", "본문", null, false, false, null, null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("제목");
    }

    @Test
    void 팝업으로_등록한_공지만_안내_팝업으로_띄우고_중요도가_높은_것부터_보여준다() {
        notice("팝업 아님", false, NoticeImportance.URGENT, null, null);
        Notice normal = notice("일반 팝업", true, NoticeImportance.NORMAL, null, null);
        Notice urgent = notice("긴급 팝업", true, NoticeImportance.URGENT, null, null);

        assertThat(noticeService.nextPopup(member.accountId(), Set.of())).contains(urgent);
        assertThat(noticeService.nextPopup(member.accountId(), Set.of(urgent.id()))).contains(normal);
    }

    @Test
    void 게시_기간_밖의_공지는_팝업과_목록에서_빠지고_관리자만_볼_수_있다() {
        LocalDateTime now = LocalDateTime.now();
        Notice scheduled = notice("예약 공지", true, NoticeImportance.NORMAL, now.plusDays(1), null);
        notice("종료 공지", true, NoticeImportance.NORMAL, now.minusDays(3), now.minusDays(1));

        assertThat(noticeService.nextPopup(member.accountId(), Set.of())).isEmpty();
        assertThat(noticeService.list(member, NoticeSearch.ALL, org.springframework.data.domain.PageRequest.of(0, 20)).getContent()).isEmpty();
        assertThat(noticeService.list(admin, NoticeSearch.ALL, org.springframework.data.domain.PageRequest.of(0, 20)).getContent()).hasSize(2);
        assertThatThrownBy(() -> noticeService.open(member, scheduled.id())).isInstanceOf(kr.co.abacus.abms.common.domain.NotFoundException.class);
    }

    private java.util.List<String> titles(LoginUser user, NoticeSearch search) {
        return noticeService.list(user, search, org.springframework.data.domain.PageRequest.of(0, 20)).getContent().stream()
                .map(Notice::getTitle).toList();
    }

    @Test
    void 제목_본문_검색과_중요도_안_읽음_상태로_거른다() {
        LocalDateTime now = LocalDateTime.now();
        Notice server = notice("서버 점검 안내", false, NoticeImportance.URGENT, null, null);
        noticeService.create(admin, new NoticeInfo("복지 안내", "100% 지원되는 건강검진", NoticeImportance.NORMAL, false, false, null, null));
        notice("예약 점검", false, NoticeImportance.NORMAL, now.plusDays(1), null);
        noticeService.open(member, server.id());

        assertThat(titles(member, new NoticeSearch("점검", null, null, false))).containsExactly("서버 점검 안내");
        assertThat(titles(member, new NoticeSearch("건강", null, null, false))).containsExactly("복지 안내");
        assertThat(titles(member, new NoticeSearch("100%", null, null, false))).containsExactly("복지 안내");
        assertThat(titles(member, new NoticeSearch("0%지", null, null, false))).isEmpty();
        assertThat(titles(member, new NoticeSearch(null, NoticeImportance.URGENT, null, false))).containsExactly("서버 점검 안내");
        assertThat(titles(member, new NoticeSearch(null, null, null, true))).containsExactly("복지 안내");
        // 일반 사용자는 상태 조건을 넘겨도 게시 중인 공지만 본다.
        assertThat(titles(member, new NoticeSearch(null, null, NoticeSearch.Status.SCHEDULED, false))).doesNotContain("예약 점검").hasSize(2);
        assertThat(titles(admin, new NoticeSearch("점검", null, NoticeSearch.Status.SCHEDULED, false))).containsExactly("예약 점검");
    }

    @Test
    void 목록_화면은_검색_조건을_유지한다() throws Exception {
        notice("서버 점검 안내", false, NoticeImportance.URGENT, null, null);
        notice("복지 안내", false, NoticeImportance.NORMAL, null, null);

        mvc.perform(get("/notices").param("q", "점검").with(user(member)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("서버 점검 안내")))
                .andExpect(content().string(not(containsString("복지 안내"))))
                .andExpect(content().string(containsString("value=\"점검\"")));
        mvc.perform(get("/notices").param("q", "없는말").with(user(member)))
                .andExpect(content().string(containsString("조건에 맞는 공지가 없습니다.")));
    }

    @Test
    void 오늘_하루_보지_않기와_다시_보지_않기는_사용자별로_팝업을_숨긴다() {
        Notice today = notice("오늘 숨김", true, NoticeImportance.URGENT, null, null);
        Notice forever = notice("영구 숨김", true, NoticeImportance.IMPORTANT, null, null);

        noticeService.hidePopupToday(member.accountId(), today.id());
        noticeService.hidePopupForever(member.accountId(), forever.id());

        assertThat(noticeService.nextPopup(member.accountId(), Set.of())).isEmpty();
        assertThat(noticeService.nextPopup(admin.accountId(), Set.of())).contains(today);
    }

    @Test
    void 공지를_열면_읽음으로_표시하고_안_읽은_수가_줄어든다() {
        Notice first = notice("첫 공지", false, NoticeImportance.NORMAL, null, null);
        notice("둘째 공지", false, NoticeImportance.NORMAL, null, null);

        assertThat(noticeService.unreadCount(member.accountId())).isEqualTo(2);
        noticeService.open(member, first.id());
        assertThat(noticeService.unreadCount(member.accountId())).isEqualTo(1);
    }

    @Test
    void 팝업이_있으면_화면에_팝업을_불러오고_닫기는_이번_로그인_동안만_숨긴다() throws Exception {
        Notice popup = notice("점검 안내", true, NoticeImportance.IMPORTANT, null, null);
        MockHttpSession session = new MockHttpSession();

        mvc.perform(get("/notices").with(user(member)).session(session))
                .andExpect(content().string(containsString("hx-get=\"/notices/popup\"")));
        mvc.perform(get("/notices/popup").with(user(member)).session(session).header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("점검 안내")))
                .andExpect(content().string(containsString("오늘 하루 보지 않기")));

        mvc.perform(post("/notices/{id}/popup/dismiss", popup.id()).param("mode", "close").with(user(member)).with(csrf())
                        .session(session).header("HX-Request", "true"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/notices").with(user(member)).session(session))
                .andExpect(content().string(not(containsString("hx-get=\"/notices/popup\""))));
        // 다른 로그인(세션)에서는 다시 보인다
        assertThat(noticeService.nextPopup(member.accountId(), Set.of())).contains(popup);
    }

    @Test
    void 등록_폼에서_안내_팝업_제공을_고르고_권한이_없으면_등록할_수_없다() throws Exception {
        mvc.perform(post("/notices").with(user(admin)).with(csrf())
                        .param("title", "팝업 없는 공지").param("body", "내용").param("importance", "NORMAL"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/notices").with(user(admin)).with(csrf())
                        .param("title", "팝업 공지").param("body", "내용").param("importance", "IMPORTANT").param("popup", "true")
                        .param("startsAt", "2026-01-01T09:00"))
                .andExpect(status().is3xxRedirection());

        assertThat(noticeRepository.findAll()).extracting(Notice::getTitle, Notice::isPopup)
                .containsExactlyInAnyOrder(org.assertj.core.groups.Tuple.tuple("팝업 없는 공지", false), org.assertj.core.groups.Tuple.tuple("팝업 공지", true));
        mvc.perform(get("/notices/new").with(user(member))).andExpect(status().isForbidden());
        mvc.perform(post("/notices").with(user(member)).with(csrf()).param("title", "몰래").param("body", "x")).andExpect(status().isForbidden());
    }

}
