package kr.co.abacus.abms.application.project.inbound;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.project.dto.ProjectCreateCommand;
import kr.co.abacus.abms.application.project.dto.ProjectUpdateCommand;

public interface ProjectManager {

    Long create(CurrentActor actor, ProjectCreateCommand command);

    Long update(CurrentActor actor, Long id, ProjectUpdateCommand command);

    void complete(CurrentActor actor, Long id);

    void cancel(CurrentActor actor, Long id);

    void delete(CurrentActor actor, Long id);

}
