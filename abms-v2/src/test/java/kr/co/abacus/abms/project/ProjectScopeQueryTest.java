package kr.co.abacus.abms.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

/**
 * 권한 범위 프로젝트 조회는 메모리 필터와 같은 결과를 쿼리로 돌려준다.
 */
@IntegrationTest
class ProjectScopeQueryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private Fixtures fixtures;

    private Department mine;
    private Project own;
    private Project joined;
    private Project hidden;

    @BeforeEach
    void setUp() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        mine = fixtures.department("범위팀", null);
        Department other = fixtures.department("남의팀", null);
        own = fixtures.project(mine, 100_000_000, start, end);
        joined = fixtures.project(other, 100_000_000, start, end);
        hidden = fixtures.project(other, 100_000_000, start, end);
    }

    @Test
    void 주관_부서와_참여_프로젝트만_조회한다() {
        List<Project> result = projectRepository.findAllInScope(false, Set.of(mine.id()), Set.of(joined.id()));

        assertThat(result).extracting(Project::id).contains(own.id(), joined.id()).doesNotContain(hidden.id());
    }

    @Test
    void 한쪽_범위가_비어도_다른_쪽으로_조회하고_둘_다_비면_빈_목록이다() {
        assertThat(projectRepository.findAllInScope(false, Set.of(), Set.of(joined.id()))).extracting(Project::id).containsExactly(joined.id());
        assertThat(projectRepository.findAllInScope(false, Set.of(mine.id()), Set.of())).extracting(Project::id).containsExactly(own.id());
        assertThat(projectRepository.findAllInScope(false, Set.of(), Set.of())).isEmpty();
    }

    @Test
    void 전체_범위는_모든_프로젝트다() {
        assertThat(projectRepository.findAllInScope(true, Set.of(), Set.of())).extracting(Project::id).contains(own.id(), joined.id(), hidden.id());
    }

}
