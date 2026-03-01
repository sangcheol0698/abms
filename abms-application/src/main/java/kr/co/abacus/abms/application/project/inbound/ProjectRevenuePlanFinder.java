package kr.co.abacus.abms.application.project.inbound;

import java.util.List;

import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;

public interface ProjectRevenuePlanFinder {

    ProjectRevenuePlan findByProjectIdAndSequence(Long projectId, Integer sequence);

    List<ProjectRevenuePlan> findByProjectId(Long projectId);

}
