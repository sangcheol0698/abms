package kr.co.abacus.abms.application.projectassignment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.projectassignment.inbound.ProjectAssignmentManager;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ProjectAssignmentModifyService implements ProjectAssignmentManager {

    private final ProjectRepository projectRepository;
    private final ProjectAssignmentRepository projectAssignmentRepository;

    @Override
    public ProjectAssignment create(ProjectAssignmentCreateRequest createRequest) {
        Project project = projectRepository.findById(createRequest.projectId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로젝트입니다."));

        ProjectAssignment projectAssignment = ProjectAssignment.assign(project, createRequest);

        return projectAssignmentRepository.save(projectAssignment);
    }

}
