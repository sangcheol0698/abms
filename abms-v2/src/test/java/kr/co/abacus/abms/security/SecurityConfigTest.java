package kr.co.abacus.abms.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SecurityConfigTest {

    @Test
    void URL_단위로_막힌_화면은_필요한_권한_이름을_알려_준다() {
        assertThat(SecurityConfig.deniedMessage("/admin/accounts/3")).isEqualTo("'계정 관리' 권한이 있어야 볼 수 있는 화면입니다.");
        assertThat(SecurityConfig.deniedMessage("/reports")).isEqualTo("'주간 보고서' 권한이 있어야 볼 수 있는 화면입니다.");
        assertThat(SecurityConfig.deniedMessage("/somewhere")).isEqualTo("이 화면을 볼 권한이 없습니다.");
    }

}
