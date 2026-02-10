package kr.co.abacus.abms.adapter.api.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("프로젝트 매출 계획 API (ProjectRevenuePlanApi)")
class ProjectRevenuePlanApiTest extends ApiIntegrationTestBase {

    @Autowired
    private ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Test
    @DisplayName("프로젝트 매출 계획 생성")
    void create() {

    }

}
