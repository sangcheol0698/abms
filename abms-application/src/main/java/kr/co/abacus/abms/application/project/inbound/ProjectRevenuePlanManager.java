package kr.co.abacus.abms.application.project.inbound;

import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanUpdateRequest;

public interface ProjectRevenuePlanManager {

    ProjectRevenuePlan create(ProjectRevenuePlanCreateRequest request);

    ProjectRevenuePlan update(Long projectId, Integer sequence, ProjectRevenuePlanUpdateRequest request);

    ProjectRevenuePlan issue(Long projectId, Integer sequence);

    ProjectRevenuePlan cancel(Long projectId, Integer sequence);

}
