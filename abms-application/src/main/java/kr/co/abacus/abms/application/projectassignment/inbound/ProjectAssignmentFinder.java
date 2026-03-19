package kr.co.abacus.abms.application.projectassignment.inbound;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectItem;
import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectSearchCondition;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;

public interface ProjectAssignmentFinder {

    List<ProjectAssignment> findByProjectId(Long projectId);

    Page<EmployeeProjectItem> findByEmployeeId(EmployeeProjectSearchCondition condition, Pageable pageable);
}
