package kr.co.abacus.abms.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AbstractEntityTest {

    static class DummyEntity extends AbstractEntity {

    }

    @Test
    @DisplayName("동일한 Id를 가진 엔티티는 equals=true, hashCode도 동일")
    void equals_true_when_same_id() throws Exception {
        Long id = 1L; // Example ID
        DummyEntity a = new DummyEntity();
        DummyEntity b = new DummyEntity();
        setId(a, id);
        setId(b, id);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("서로 다른 Id를 가진 엔티티는 equals=false")
    void equals_false_when_different_id() throws Exception {
        DummyEntity a = new DummyEntity();
        DummyEntity b = new DummyEntity();
        setId(a, 1L);
        setId(b, 2L);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("둘 다 id=null이면 서로 다른 인스턴스는 equals=false")
    void equals_false_when_both_ids_null() {
        DummyEntity a = new DummyEntity();
        DummyEntity b = new DummyEntity();

        assertThat(a).isNotEqualTo(b);
    }

    private static void setId(AbstractEntity entity, Long id) throws Exception {
        Field f = AbstractEntity.class.getDeclaredField("id");
        f.setAccessible(true);
        f.set(entity, id);
    }

}