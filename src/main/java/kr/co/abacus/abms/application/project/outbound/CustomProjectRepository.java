package kr.co.abacus.abms.application.project.outbound;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;

public interface CustomProjectRepository {

    Page<ProjectSummary> search(ProjectSearchCondition condition, Pageable pageable);

}
