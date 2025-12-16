package kr.co.abacus.abms.application.project.inbound;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;

public interface ProjectFinder {

    Project find(UUID id);

    List<Project> findAll();

    Page<Project> findAll(Pageable pageable);

    List<Project> findAllByPartyId(UUID partyId);

    List<Project> findAllByStatus(ProjectStatus status);

}
