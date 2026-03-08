package kr.co.abacus.abms.domain.grouppermissiongrant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("권한 그룹 권한 부여 (GroupPermissionGrant)")
class GroupPermissionGrantTest {

    @Test
    @DisplayName("권한 그룹에 권한과 범위를 부여한다")
    void create() {
        GroupPermissionGrant grant = GroupPermissionGrant.create(1L, 2L, PermissionScope.OWN_DEPARTMENT);

        assertThat(grant.getPermissionGroupId()).isEqualTo(1L);
        assertThat(grant.getPermissionId()).isEqualTo(2L);
        assertThat(grant.getScope()).isEqualTo(PermissionScope.OWN_DEPARTMENT);
        assertThat(grant.isDeleted()).isFalse();
    }

}
