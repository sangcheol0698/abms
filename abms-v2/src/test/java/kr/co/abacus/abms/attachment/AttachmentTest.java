package kr.co.abacus.abms.attachment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class AttachmentTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private AttachmentRepository attachmentRepository;

    private Project project;
    private Project hidden;
    private LoginUser admin;
    private LoginUser member;

    @BeforeEach
    void setUp() {
        Department team = fixtures.department("첨부팀", null);
        Department other = fixtures.department("남의팀", null);
        Employee employee = fixtures.employee(team, "첨부자");
        project = fixtures.project(team, 100_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        hidden = fixtures.project(other, 100_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        admin = Fixtures.admin(employee);
        member = Fixtures.user(employee, Fixtures.grants(PermissionScope.OWN_DEPARTMENT, PermissionCode.PROJECT_READ));
    }

    private static MockMultipartFile file(String name, String content) {
        return new MockMultipartFile("file", name, "application/pdf", content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void 업로드하면_섹션을_다시_그리고_한글_파일명으로_내려받는다() throws Exception {
        mvc.perform(multipart("/attachments").file(file("계약서 최종.pdf", "PDF-본문")).param("ownerType", "PROJECT")
                        .param("ownerId", String.valueOf(project.id())).param("category", "CONTRACT")
                        .with(user(admin)).with(csrf()).header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("계약서 최종.pdf")))
                .andExpect(content().string(containsString("계약서")));

        Attachment saved = attachmentRepository.findAllByOwnerTypeAndOwnerIdOrderByIdDesc(AttachmentOwner.PROJECT, project.id()).getFirst();
        mvc.perform(get("/attachments/{id}/download", saved.id()).with(user(member)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(header().string("Content-Disposition", containsString("filename*=UTF-8''")))
                .andExpect(content().bytes("PDF-본문".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void 허용하지_않는_형식은_거부한다() throws Exception {
        mvc.perform(multipart("/attachments").file(file("악성.html", "<script>")).param("ownerType", "PROJECT")
                        .param("ownerId", String.valueOf(project.id())).with(user(admin)).with(csrf()).header("HX-Request", "true"))
                .andExpect(status().isUnprocessableContent());

        assertThat(attachmentRepository.findAllByOwnerTypeAndOwnerIdOrderByIdDesc(AttachmentOwner.PROJECT, project.id())).isEmpty();
    }

    @Test
    void 대상의_조회_권한이_없으면_내려받을_수_없고_쓰기_권한이_없으면_올릴_수_없다() throws Exception {
        mvc.perform(multipart("/attachments").file(file("a.pdf", "x")).param("ownerType", "PROJECT")
                        .param("ownerId", String.valueOf(hidden.id())).with(user(admin)).with(csrf()).header("HX-Request", "true"))
                .andExpect(status().isOk());
        Attachment secret = attachmentRepository.findAllByOwnerTypeAndOwnerIdOrderByIdDesc(AttachmentOwner.PROJECT, hidden.id()).getFirst();

        mvc.perform(get("/attachments/{id}/download", secret.id()).with(user(member))).andExpect(status().isForbidden());
        mvc.perform(multipart("/attachments").file(file("b.pdf", "x")).param("ownerType", "PROJECT")
                        .param("ownerId", String.valueOf(project.id())).with(user(member)).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void 삭제하면_목록에서_사라진다() throws Exception {
        mvc.perform(multipart("/attachments").file(file("지울.pdf", "x")).param("ownerType", "PROJECT")
                .param("ownerId", String.valueOf(project.id())).with(user(admin)).with(csrf()).header("HX-Request", "true"));
        Attachment saved = attachmentRepository.findAllByOwnerTypeAndOwnerIdOrderByIdDesc(AttachmentOwner.PROJECT, project.id()).getFirst();

        mvc.perform(post("/attachments/{id}/delete", saved.id()).with(user(admin)).with(csrf()).header("HX-Request", "true"))
                .andExpect(status().isOk());

        assertThat(attachmentRepository.findAllByOwnerTypeAndOwnerIdOrderByIdDesc(AttachmentOwner.PROJECT, project.id())).isEmpty();
    }

    @Test
    void 파일명에서_경로를_떼어_낸다() {
        assertThat(AttachmentService.originalName("C:\\\\Users\\\\me\\\\견적서.xlsx")).isEqualTo("견적서.xlsx");
        assertThat(AttachmentService.originalName("../../etc/passwd.txt")).isEqualTo("passwd.txt");
    }

}
