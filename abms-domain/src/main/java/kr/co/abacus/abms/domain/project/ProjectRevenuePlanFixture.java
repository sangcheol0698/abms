package kr.co.abacus.abms.domain.project;

import java.time.LocalDate;

public class ProjectRevenuePlanFixture {

    public static ProjectRevenuePlan createProjectRevenuePlan(Long projectId, Integer sequence) {
        return ProjectRevenuePlan.create(createProjectRevenuePlanCreateRequest(projectId, sequence));
    }

    public static ProjectRevenuePlanCreateRequest createProjectRevenuePlanCreateRequest(Long projectId, Integer sequence) {
        return new ProjectRevenuePlanCreateRequest(
                projectId,
                sequence,
                LocalDate.of(2026, 1, 1),
                RevenueType.DOWN_PAYMENT,
                10000000L,
                "메모"
        );
    }

}
