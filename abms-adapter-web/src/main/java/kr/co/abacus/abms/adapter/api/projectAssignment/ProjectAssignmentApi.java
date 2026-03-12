package kr.co.abacus.abms.adapter.api.projectAssignment;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.co.abacus.abms.adapter.api.projectAssignment.dto.ProjectAssignmentCreateResponse;
import kr.co.abacus.abms.adapter.api.projectAssignment.dto.ProjectAssignmentResponse;
import kr.co.abacus.abms.application.projectassignment.inbound.ProjectAssignmentFinder;
import kr.co.abacus.abms.application.projectassignment.inbound.ProjectAssignmentManager;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class ProjectAssignmentApi {

    private final ProjectAssignmentManager projectAssignmentManager;
    private final ProjectAssignmentFinder projectAssignmentFinder;

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PostMapping("/api/project-assignments")
    public ProjectAssignmentCreateResponse assign(@RequestBody @Valid ProjectAssignmentCreateRequest request) {
        Long id = projectAssignmentManager.create(request).getIdOrThrow();
        return ProjectAssignmentCreateResponse.of(id);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.read')")
    @GetMapping("/api/project-assignments")
    public List<ProjectAssignmentResponse> findByProjectId(@RequestParam Long projectId) {
        return projectAssignmentFinder.findByProjectId(projectId).stream()
            .map(ProjectAssignmentResponse::from)
            .toList();
    }
}
