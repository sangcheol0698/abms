package kr.co.abacus.abms.application.project.required;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;

public interface ProjectRepository extends Repository<Project, UUID> {

    Project save(Project project);

    Optional<Project> findById(UUID id);

    Optional<Project> findByIdAndDeletedFalse(UUID id);

    List<Project> findAllByDeletedFalse();

    Page<Project> findAllByDeletedFalse(Pageable pageable);

    boolean existsByCode(String code);

    List<Project> findAllByPartyIdAndDeletedFalse(UUID partyId);

    List<Project> findAllByStatusAndDeletedFalse(ProjectStatus status);

}
