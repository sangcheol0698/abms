package kr.co.abacus.abms.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BaseEntityTest {

    @Test
    void softDelete() {
        BaseEntity entity = new BaseEntity() {
        };

        entity.softDelete("testUser");

        assertThat(entity.isDeleted()).isTrue();
        assertThat(entity.getDeletedAt()).isNotNull();
        assertThat(entity.getDeletedBy()).isEqualTo("testUser");
    }

    @Test
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