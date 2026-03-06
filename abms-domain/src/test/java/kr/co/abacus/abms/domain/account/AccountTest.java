package kr.co.abacus.abms.domain.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("계정 (Account)")
class AccountTest {

    @Test
    @DisplayName("비밀번호를 변경하면 해시와 변경 시각이 갱신된다")
    void changePassword_updatesPasswordAndTimestamp() {
        Account account = Account.create(1L, "tester@iabacus.co.kr", "{bcrypt}old-password");
        LocalDateTime oldChangedAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        ReflectionTestUtils.setField(account, "passwordChangedAt", oldChangedAt);

        account.changePassword("{bcrypt}new-password");

        assertThat(account.getPassword()).isEqualTo("{bcrypt}new-password");
        assertThat(account.getPasswordChangedAt()).isAfter(oldChangedAt);
    }

}
