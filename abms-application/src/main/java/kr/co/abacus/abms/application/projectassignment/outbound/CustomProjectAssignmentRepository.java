package kr.co.abacus.abms.application.projectassignment.outbound;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectItem;
import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectSearchCondition;
import kr.co.abacus.abms.application.projectassignment.dto.ProjectAssignmentItem;
import kr.co.abacus.abms.application.projectassignment.dto.ProjectAssignmentSearchCondition;

public interface CustomProjectAssignmentRepository {

    Page<EmployeeProjectItem> searchEmployeeProjects(EmployeeProjectSearchCondition condition, Pageable pageable);

    java.util.List<ProjectAssignmentItem> findProjectAssignments(Long projectId);

    Page<ProjectAssignmentItem> searchProjectAssignments(ProjectAssignmentSearchCondition condition, Pageable pageable);
}
