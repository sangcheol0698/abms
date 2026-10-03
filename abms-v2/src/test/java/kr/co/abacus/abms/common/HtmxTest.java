package kr.co.abacus.abms.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import kr.co.abacus.abms.common.web.Htmx;

class HtmxTest {

    @Test
    void 토스트_메시지는_ASCII_로_이스케이프된_HX_Trigger_헤더가_된다() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        Htmx.toast(response, Htmx.ToastType.SUCCESS, "저장 \"완료\"");

        String header = response.getHeader("HX-Trigger");
        assertThat(header).isEqualTo("{\"toast\":{\"type\":\"success\",\"message\":\"\\uc800\\uc7a5 \\\"\\uc644\\ub8cc\\\"\"}}");
        assertThat(header.chars().allMatch(c -> c < 128)).isTrue();
    }

    @Test
    void 여러_이벤트를_합친다() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        Htmx.event(response, "closeModal");
        Htmx.toast(response, Htmx.ToastType.INFO, "ok");

        assertThat(response.getHeader("HX-Trigger")).isEqualTo("{\"closeModal\":true,\"toast\":{\"type\":\"info\",\"message\":\"ok\"}}");
    }

}
