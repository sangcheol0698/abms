package kr.co.abacus.abms.application.project.inbound;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;

public interface ProjectFinder {

    Project find(Long id);

    List<Project> findAll();

    Page<Project> findAll(Pageable pageable);

    List<Project> findAllByPartyId(Long partyId);

    List<Project> findAllByStatus(ProjectStatus status);

}
