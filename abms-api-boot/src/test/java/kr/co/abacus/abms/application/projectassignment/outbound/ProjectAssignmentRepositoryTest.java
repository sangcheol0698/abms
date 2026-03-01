package kr.co.abacus.abms.application.projectassignment.outbound;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.support.IntegrationTestBase;

class ProjectAssignmentRepositoryTest extends IntegrationTestBase {

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Test
    @DisplayName("기간 교차 조회: 조회 기간과 하루라도 겹치는 할당만 조회된다")
    void findActiveAssignments_BoundaryTest() {
        // given

        // 조회 기준: 2026년 2월 (2/1 ~ 2/28)
        LocalDate targetStart = LocalDate.of(2026, 2, 1);
        LocalDate targetEnd = LocalDate.of(2026, 2, 28);

        // 1. [제외] 완전 과거 (1/1 ~ 1/31)
        saveAssignment(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        // 2. [포함] 앞쪽 걸침 (1/20 ~ 2/5)
        saveAssignment(LocalDate.of(2026, 1, 20), LocalDate.of(2026, 2, 5));

        // 3. [포함] 완전 포함 (2/10 ~ 2/20)
        saveAssignment(LocalDate.of(2026, 2, 10), LocalDate.of(2026, 2, 20));

        // 4. [포함] 뒤쪽 걸침 (2/25 ~ 3/10)
        saveAssignment(LocalDate.of(2026, 2, 25), LocalDate.of(2026, 3, 10));

        // 5. [포함] 조회 기간을 감쌈 (1/1 ~ 3/31)
        saveAssignment(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31));

        // 6. [제외] 완전 미래 (3/1 ~ 3/31)
        saveAssignment(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        // 7. [포함] 종료일 없음 (1/15 ~ 쭉) -> Active
        saveAssignment(LocalDate.of(2026, 1, 15), null);

        // 8. [제외] 종료일 없음이지만 미래에 시작 (3/5 ~ 쭉)
        saveAssignment(LocalDate.of(2026, 3, 5), null);

        // when
        List<ProjectAssignment> results = projectAssignmentRepository.findActiveAssignments(targetStart, targetEnd);

        // then
        // 포함되어야 할 케이스: 2, 3, 4, 5, 7 (총 5건)
        assertThat(results).hasSize(5);
    }

    private void saveAssignment(LocalDate start, LocalDate end) {
        // 프로젝트 구성
        Project project = createProject();
        ReflectionTestUtils.setField(project, "period", new Period(start, end));

        // 프로젝트 할당 (인력 투입) 구성
        ProjectAssignmentCreateRequest projectAssignmentCreateRequest = new ProjectAssignmentCreateRequest(
            1L, 1L, AssignmentRole.DEV, start, end);
        ProjectAssignment assignment = ProjectAssignment.assign(project, projectAssignmentCreateRequest);

        projectAssignmentRepository.save(assignment);
        flushAndClear();
    }
}