package kr.co.abacus.abms.application.project.inbound;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectUpdateRequest;

public interface ProjectManager {

    Project create(ProjectCreateRequest request);

    Project update(Long id, ProjectUpdateRequest request);

    Project complete(Long id);

    Project cancel(Long id);

    void delete(Long id);

}
