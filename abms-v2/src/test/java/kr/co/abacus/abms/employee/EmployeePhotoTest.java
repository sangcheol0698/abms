package kr.co.abacus.abms.employee;

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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class EmployeePhotoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee me;
    private Employee colleague;
    private LoginUser self;

    @BeforeEach
    void setUp() throws Exception {
        Department team = fixtures.department("사진팀", null);
        me = fixtures.employee(team, "본인");
        colleague = fixtures.employee(team, "동료");
        self = Fixtures.user(me, Fixtures.grants(PermissionScope.SELF, PermissionCode.EMPLOYEE_READ, PermissionCode.EMPLOYEE_WRITE));
    }

    private static MockMultipartFile photo() throws Exception {
        return new MockMultipartFile("file", "얼굴.png", "image/png", ProfileImagesTest.png(600, 400));
    }

    @Test
    void 사진이_없으면_기본_아바타를_보여준다() throws Exception {
        assertThat(me.photoUrl()).isNull();

        mvc.perform(get("/employees/{id}", me.id()).with(user(self)))
                .andExpect(content().string(containsString("role=\"img\" aria-label=\"본인\"")));
    }

    @Test
    void 본인은_사진을_올리고_320px_JPEG_로_내려받는다() throws Exception {
        mvc.perform(multipart("/employees/{id}/photo", me.id()).file(photo()).param("returnTo", "me")
                        .with(user(self)).with(csrf()).header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/me"));

        Employee saved = employeeRepository.findById(me.id()).orElseThrow();
        assertThat(saved.photoUrl()).startsWith("/employees/" + me.id() + "/photo?v=");
        byte[] body = mvc.perform(get(saved.photoUrl()).with(user(self)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("Cache-Control", containsString("immutable")))
                .andReturn().getResponse().getContentAsByteArray();
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(body));
        assertThat(image.getWidth()).isEqualTo(320);
        assertThat(image.getHeight()).isEqualTo(320);
    }

    @Test
    void 다른_직원의_사진은_수정_권한이_없으면_바꿀_수_없다() throws Exception {
        mvc.perform(multipart("/employees/{id}/photo", colleague.id()).file(photo()).with(user(self)).with(csrf()).header("HX-Request", "true"))
                .andExpect(status().isForbidden());

        mvc.perform(multipart("/employees/{id}/photo", colleague.id()).file(photo()).with(user(Fixtures.admin(me))).with(csrf())
                        .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(header().string("HX-Redirect", "/employees/" + colleague.id()));
    }

    @Test
    void 사진을_삭제하면_기본_아바타로_돌아가고_사진_주소는_404() throws Exception {
        mvc.perform(multipart("/employees/{id}/photo", me.id()).file(photo()).with(user(self)).with(csrf()).header("HX-Request", "true"));

        mvc.perform(post("/employees/{id}/photo/delete", me.id()).with(user(self)).with(csrf()).header("HX-Request", "true"))
                .andExpect(status().isOk());

        assertThat(employeeRepository.findById(me.id()).orElseThrow().photoUrl()).isNull();
        mvc.perform(get("/employees/{id}/photo", me.id()).with(user(self))).andExpect(status().isNotFound());
    }

    @Test
    void 이미지가_아닌_파일은_토스트로_거부한다() throws Exception {
        mvc.perform(multipart("/employees/{id}/photo", me.id())
                        .file(new MockMultipartFile("file", "a.png", "image/png", "<svg/>".getBytes()))
                        .with(user(self)).with(csrf()).header("HX-Request", "true"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(header().string("HX-Trigger", containsString("toast")));
    }

}
