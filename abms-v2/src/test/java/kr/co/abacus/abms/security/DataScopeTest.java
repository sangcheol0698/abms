package kr.co.abacus.abms.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class DataScopeTest {

    @Test
    void 범위는_합집합으로_적용된다() {
        DataScope scope = new DataScope(false, Set.of(10L), Set.of(7L), Set.of(100L));

        assertThat(scope.coversEmployee(1L, 10L)).isTrue();   // 부서 범위
        assertThat(scope.coversEmployee(7L, 99L)).isTrue();   // 본인
        assertThat(scope.coversEmployee(8L, 99L)).isFalse();
        assertThat(scope.coversDepartment(99L)).isFalse();    // SELF 는 부서 단위 쓰기 권한이 아니다
        assertThat(scope.coversProject(100L, 99L)).isTrue();  // 참여 프로젝트
        assertThat(scope.coversProject(200L, 10L)).isTrue();  // 주관 부서
        assertThat(scope.coversProject(200L, 99L)).isFalse();
    }

    @Test
    void 전체와_없음() {
        assertThat(DataScope.ALL.coversProject(1L, 1L)).isTrue();
        assertThat(DataScope.NONE.isNone()).isTrue();
        assertThat(DataScope.NONE.coversEmployee(1L, 1L)).isFalse();
    }

}
