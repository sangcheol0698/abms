package kr.co.abacus.abms.application.project.outbound;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.domain.project.Project;

public interface CustomProjectRepository {

    Page<ProjectSummary> search(ProjectSearchCondition condition, Pageable pageable);

    List<Project> search(ProjectSearchCondition condition);

}
