package kr.co.abacus.abms.adapter.api.projectAssignment;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import kr.co.abacus.abms.adapter.security.CurrentActorResolver;
import kr.co.abacus.abms.adapter.api.projectAssignment.dto.ProjectAssignmentCreateResponse;
import kr.co.abacus.abms.adapter.api.projectAssignment.dto.ProjectAssignmentEndApiRequest;
import kr.co.abacus.abms.adapter.api.projectAssignment.dto.ProjectAssignmentResponse;
import kr.co.abacus.abms.adapter.api.projectAssignment.dto.ProjectAssignmentSearchRequest;
import kr.co.abacus.abms.adapter.api.projectAssignment.dto.ProjectAssignmentUpdateApiRequest;
import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.application.employee.inbound.EmployeeFinder;
import kr.co.abacus.abms.application.project.ProjectQueryService;
import kr.co.abacus.abms.application.projectassignment.inbound.ProjectAssignmentFinder;
import kr.co.abacus.abms.application.projectassignment.inbound.ProjectAssignmentManager;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentNotFoundException;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class ProjectAssignmentApi {

    private final ProjectAssignmentManager projectAssignmentManager;
    private final ProjectAssignmentFinder projectAssignmentFinder;
    private final ProjectQueryService projectQueryService;
    private final CurrentActorResolver currentActorResolver;
    private final EmployeeFinder employeeFinder;

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PostMapping("/api/project-assignments")
    public ProjectAssignmentCreateResponse assign(
            @RequestBody @Valid ProjectAssignmentCreateRequest request,
            Authentication authentication
    ) {
        Long id = projectAssignmentManager.create(currentActorResolver.resolve(authentication), request).getIdOrThrow();
        return ProjectAssignmentCreateResponse.of(id);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.read')")
    @GetMapping("/api/project-assignments")
    public PageResponse<ProjectAssignmentResponse> findByProjectId(
            @RequestParam Long projectId,
            ProjectAssignmentSearchRequest request,
            Pageable pageable,
            Authentication authentication
    ) {
        var actor = currentActorResolver.resolve(authentication);
        if (!projectQueryService.canRead(projectId, actor)) {
            throw new org.springframework.security.access.AccessDeniedException("프로젝트 조회 권한 범위를 벗어났습니다.");
        }
        return PageResponse.of(projectAssignmentFinder.searchByProjectId(request.toCondition(projectId), pageable)
            .map(item -> ProjectAssignmentResponse.from(
                    projectAssignmentFinder.findById(item.id()),
                    employeeFinder.findEmployeeDetail(item.employeeId()),
                    item.assignmentStatus()
            )));
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PatchMapping("/api/project-assignments/{id}")
    public ProjectAssignmentCreateResponse update(
            @PathVariable Long id,
            @RequestBody @Valid ProjectAssignmentUpdateApiRequest request,
            Authentication authentication
    ) {
        Long updatedId = projectAssignmentManager.update(currentActorResolver.resolve(authentication), id, request.toRequest())
                .getIdOrThrow();
        return ProjectAssignmentCreateResponse.of(updatedId);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PatchMapping("/api/project-assignments/{id}/end")
    public ProjectAssignmentCreateResponse end(
            @PathVariable Long id,
            @RequestBody @Valid ProjectAssignmentEndApiRequest request,
            Authentication authentication
    ) {
        Long endedId = projectAssignmentManager.end(currentActorResolver.resolve(authentication), id, request.toRequest())
                .getIdOrThrow();
        return ProjectAssignmentCreateResponse.of(endedId);
    }

    private String resolveAssignmentStatus(kr.co.abacus.abms.domain.projectassignment.ProjectAssignment assignment) {
        LocalDate today = LocalDate.now();
        if (assignment.getPeriod().startDate().isAfter(today)) {
            return "SCHEDULED";
        }
        if (assignment.getPeriod().endDate() == null || !assignment.getPeriod().endDate().isBefore(today)) {
            return "CURRENT";
        }
        return "ENDED";
    }
}
