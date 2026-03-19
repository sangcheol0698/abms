package kr.co.abacus.abms.application.projectassignment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectItem;
import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectSearchCondition;
import kr.co.abacus.abms.application.projectassignment.inbound.ProjectAssignmentFinder;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectAssignmentQueryService implements ProjectAssignmentFinder {

    private final ProjectAssignmentRepository projectAssignmentRepository;

    @Override
    public List<ProjectAssignment> findByProjectId(Long projectId) {
        return projectAssignmentRepository.findByProjectId(projectId);
    }

    @Override
    public Page<EmployeeProjectItem> findByEmployeeId(EmployeeProjectSearchCondition condition, Pageable pageable) {
        return projectAssignmentRepository.searchEmployeeProjects(condition, pageable);
    }
}
