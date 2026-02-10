package kr.co.abacus.abms.application.project.outbound;


import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;

public interface ProjectRevenuePlanRepository extends Repository<ProjectRevenuePlan, Long> {

    ProjectRevenuePlan save(ProjectRevenuePlan projectRevenuePlan);

    Boolean existsByProjectIdAndSequence(Long projectId, Integer sequence);

    Optional<ProjectRevenuePlan> findByProjectIdAndSequence(Long projectId, Integer sequence);

    List<ProjectRevenuePlan> findByProjectId(Long projectId);

}
