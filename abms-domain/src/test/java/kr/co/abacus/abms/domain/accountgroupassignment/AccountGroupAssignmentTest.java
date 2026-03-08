package kr.co.abacus.abms.domain.accountgroupassignment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("계정 권한 그룹 연결 (AccountGroupAssignment)")
class AccountGroupAssignmentTest {

    @Test
    @DisplayName("계정과 권한 그룹 식별자로 연결을 생성한다")
    void create() {
        AccountGroupAssignment assignment = AccountGroupAssignment.create(1L, 2L);

        assertThat(assignment.getAccountId()).isEqualTo(1L);
        assertThat(assignment.getPermissionGroupId()).isEqualTo(2L);
        assertThat(assignment.isDeleted()).isFalse();
    }

}
