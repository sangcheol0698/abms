package kr.co.abacus.abms.application.projectassignment.outbound;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectItem;
import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectSearchCondition;

public interface CustomProjectAssignmentRepository {

    Page<EmployeeProjectItem> searchEmployeeProjects(EmployeeProjectSearchCondition condition, Pageable pageable);
}
