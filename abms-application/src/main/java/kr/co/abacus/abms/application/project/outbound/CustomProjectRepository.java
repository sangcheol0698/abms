package kr.co.abacus.abms.application.project.outbound;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.project.dto.ProjectOverviewSummary;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.domain.project.Project;

public interface CustomProjectRepository {

    Page<ProjectSummary> search(ProjectSearchCondition condition, Pageable pageable);

    Page<ProjectSummary> search(ProjectSearchCondition condition, @Nullable CurrentActor actor, Pageable pageable);

    boolean canRead(Long projectId, CurrentActor actor);

    ProjectOverviewSummary summarize(ProjectSearchCondition condition);

    ProjectOverviewSummary summarize(ProjectSearchCondition condition, @Nullable CurrentActor actor);

    List<Project> search(ProjectSearchCondition condition);

    List<Project> search(ProjectSearchCondition condition, @Nullable CurrentActor actor);

}
