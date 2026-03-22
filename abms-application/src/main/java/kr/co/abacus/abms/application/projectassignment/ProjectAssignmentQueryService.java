package kr.co.abacus.abms.application.projectassignment;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectItem;
import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectSearchCondition;
import kr.co.abacus.abms.application.projectassignment.dto.ProjectAssignmentItem;
import kr.co.abacus.abms.application.projectassignment.dto.ProjectAssignmentSearchCondition;
import kr.co.abacus.abms.application.projectassignment.inbound.ProjectAssignmentFinder;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentNotFoundException;
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
    public ProjectAssignment findById(Long assignmentId) {
        return projectAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ProjectAssignmentNotFoundException("존재하지 않는 프로젝트 투입 정보입니다."));
    }

    @Override
    public List<ProjectAssignmentItem> findItemsByProjectId(Long projectId) {
        return projectAssignmentRepository.findProjectAssignments(projectId);
    }

    @Override
    public Page<ProjectAssignmentItem> searchByProjectId(ProjectAssignmentSearchCondition condition, Pageable pageable) {
        return projectAssignmentRepository.searchProjectAssignments(condition, pageable);
    }

    @Override
    public Page<EmployeeProjectItem> findByEmployeeId(EmployeeProjectSearchCondition condition, Pageable pageable) {
        return projectAssignmentRepository.searchEmployeeProjects(condition, pageable);
    }

    @Override
    public Page<EmployeeProjectItem> findByEmployeeId(
            EmployeeProjectSearchCondition condition,
            CurrentActor actor,
            Pageable pageable
    ) {
        return projectAssignmentRepository.searchEmployeeProjects(condition, actor, pageable);
    }
}
