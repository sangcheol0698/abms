package kr.co.abacus.abms.domain.permission;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("권한 (Permission)")
class PermissionTest {

    @Test
    @DisplayName("권한 코드와 이름, 설명으로 권한을 생성한다")
    void create() {
        Permission permission = Permission.create(
                "project.read",
                "프로젝트 조회",
                "프로젝트 목록과 상세를 조회할 수 있다."
        );

        assertThat(permission.getCode()).isEqualTo("project.read");
        assertThat(permission.getName()).isEqualTo("프로젝트 조회");
        assertThat(permission.getDescription()).isEqualTo("프로젝트 목록과 상세를 조회할 수 있다.");
        assertThat(permission.isDeleted()).isFalse();
    }

}
