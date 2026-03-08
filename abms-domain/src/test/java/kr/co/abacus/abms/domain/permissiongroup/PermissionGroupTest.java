package kr.co.abacus.abms.domain.permissiongroup;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("권한 그룹 (PermissionGroup)")
class PermissionGroupTest {

    @Test
    @DisplayName("이름, 설명, 그룹 타입으로 권한 그룹을 생성한다")
    void create() {
        PermissionGroup permissionGroup = PermissionGroup.create(
                "프로젝트 조회 그룹",
                "프로젝트 조회 권한을 모아둔 그룹이다.",
                PermissionGroupType.CUSTOM
        );

        assertThat(permissionGroup.getName()).isEqualTo("프로젝트 조회 그룹");
        assertThat(permissionGroup.getDescription()).isEqualTo("프로젝트 조회 권한을 모아둔 그룹이다.");
        assertThat(permissionGroup.getGroupType()).isEqualTo(PermissionGroupType.CUSTOM);
        assertThat(permissionGroup.isDeleted()).isFalse();
    }

}
