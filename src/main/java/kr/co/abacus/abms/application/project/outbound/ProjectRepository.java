package kr.co.abacus.abms.application.project.outbound;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;

public interface ProjectRepository extends Repository<Project, Long>, CustomProjectRepository {

    Project save(Project project);

    Optional<Project> findById(Long id);

    Optional<Project> findByIdAndDeletedFalse(Long id);

    List<Project> findAllByDeletedFalse();

    Page<Project> findAllByDeletedFalse(Pageable pageable);

    boolean existsByCode(String code);

    List<Project> findAllByPartyIdAndDeletedFalse(Long partyId);

    List<Project> findAllByStatusAndDeletedFalse(ProjectStatus status);

    int countByStatus(ProjectStatus status);

    @Query("SELECT p FROM Project p WHERE p.period.startDate <= :endOfMonth AND p.period.endDate >= :startOfMonth")
    List<Project> findActiveProjects(LocalDate startOfMonth, LocalDate endOfMonth);

}
