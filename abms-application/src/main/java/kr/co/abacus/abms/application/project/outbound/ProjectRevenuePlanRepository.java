package kr.co.abacus.abms.application.project.outbound;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;

public interface ProjectRevenuePlanRepository {

    ProjectRevenuePlan save(ProjectRevenuePlan projectRevenuePlan);

    Boolean existsByProjectIdAndSequence(Long projectId, Integer sequence);

    Optional<ProjectRevenuePlan> findByProjectIdAndSequence(Long projectId, Integer sequence);

    List<ProjectRevenuePlan> findByProjectId(Long projectId);

    List<ProjectRevenuePlan> findByRevenueDateBetweenAndIsIssuedTrue(LocalDate startOfMonth, LocalDate endOfMonth);

    List<ProjectRevenuePlan> findByProjectIdAndRevenueDateBetweenAndIsIssuedTrue(Long projectId, LocalDate start, LocalDate end);
}
