package kr.co.abacus.abms.department;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DepartmentTreeTest {

    private final Department root = dept(1L, "A0", "회사", null);
    private final Department division = dept(2L, "A1", "본부", 1L);
    private final Department team1 = dept(3L, "A11", "1팀", 2L);
    private final Department team2 = dept(4L, "A12", "2팀", 2L);
    private final Department other = dept(5L, "A2", "다른 본부", 1L);
    private final DepartmentTree tree = new DepartmentTree(List.of(team2, other, root, team1, division));

    @Test
    void 하위_부서를_모두_찾는다() {
        assertThat(tree.subtreeIds(2L)).containsExactlyInAnyOrder(2L, 3L, 4L);
        assertThat(tree.subtreeIds(1L)).hasSize(5);
        assertThat(tree.subtreeIds(4L)).containsExactly(4L);
    }

    @Test
    void 루트부터의_경로와_들여쓰기_목록() {
        assertThat(tree.path(3L)).extracting(Department::getName).containsExactly("회사", "본부", "1팀");
        assertThat(tree.flatten()).extracting(n -> n.department().getName() + ":" + n.depth())
                .containsExactly("회사:0", "본부:1", "1팀:2", "2팀:2", "다른 본부:1");
    }

    private static Department dept(Long id, String code, String name, Long parentId) {
        Department department = Department.create(code, name, DepartmentType.TEAM, parentId);
        ReflectionTestUtils.setField(department, "id", id);
        return department;
    }

}
