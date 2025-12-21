package kr.co.abacus.abms.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("기본 엔티티 (BaseEntity)")
class BaseEntityTest {

    @Test
    @DisplayName("삭제 처리(soft delete)한다")
    void softDelete() {
        BaseEntity entity = new BaseEntity() {
        };

        entity.softDelete("testUser");

        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.getDeletedBy()).isEqualTo("testUser");
    }

    @Test
    @DisplayName("삭제된 엔티티를 복구한다")
    void restore() {
        BaseEntity entity = new BaseEntity() {
        };
        entity.softDelete("testUser");
        assertThat(entity.isDeleted()).isTrue();

        entity.restore();

        assertThat(entity.isDeleted()).isFalse();
        assertThat(entity.getDeletedAt()).isNull();
        assertThat(entity.getDeletedBy()).isNull();
    }

}