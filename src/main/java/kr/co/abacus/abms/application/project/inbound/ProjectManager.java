package kr.co.abacus.abms.application.project.inbound;

import java.util.UUID;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectUpdateRequest;

public interface ProjectManager {

    Project create(ProjectCreateRequest request);

    Project update(UUID id, ProjectUpdateRequest request);

    Project complete(UUID id);

    Project cancel(UUID id);

    void delete(UUID id);

}
