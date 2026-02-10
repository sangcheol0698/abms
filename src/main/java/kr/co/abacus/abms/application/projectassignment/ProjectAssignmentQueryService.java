package kr.co.abacus.abms.application.projectassignment;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}