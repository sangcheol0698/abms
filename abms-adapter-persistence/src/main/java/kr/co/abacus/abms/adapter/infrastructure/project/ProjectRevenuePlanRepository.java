package kr.co.abacus.abms.adapter.infrastructure.project;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;

public interface ProjectRevenuePlanRepository
        extends Repository<ProjectRevenuePlan, Long>,
        kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository {
}
