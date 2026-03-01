package kr.co.abacus.abms.adapter.infrastructure.project;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.project.Project;

public interface ProjectRepository
        extends Repository<Project, Long>,
        kr.co.abacus.abms.application.project.outbound.ProjectRepository {

    @Override
    @Query("SELECT p FROM Project p WHERE p.period.startDate <= :endOfMonth AND p.period.endDate >= :startOfMonth")
    List<Project> findActiveProjects(LocalDate startOfMonth, LocalDate endOfMonth);
}
