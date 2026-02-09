package kr.co.abacus.abms.application.project.inbound;

import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;

public interface ProjectRevenuePlanManager {

    ProjectRevenuePlan create(ProjectRevenuePlanCreateRequest request);

}
