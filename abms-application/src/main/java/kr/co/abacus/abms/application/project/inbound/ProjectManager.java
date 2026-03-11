package kr.co.abacus.abms.application.project.inbound;

import kr.co.abacus.abms.application.project.dto.ProjectCreateCommand;
import kr.co.abacus.abms.application.project.dto.ProjectUpdateCommand;

public interface ProjectManager {

    Long create(ProjectCreateCommand command);

    Long update(Long id, ProjectUpdateCommand command);

    void complete(Long id);

    void cancel(Long id);

    void delete(Long id);

}
