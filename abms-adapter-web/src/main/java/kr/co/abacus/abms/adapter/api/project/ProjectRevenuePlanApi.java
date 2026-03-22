package kr.co.abacus.abms.adapter.api.project;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kr.co.abacus.abms.adapter.api.project.dto.ProjectRevenuePlanResponse;
import kr.co.abacus.abms.adapter.security.CurrentActorResolver;
import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanManager;
import kr.co.abacus.abms.application.project.ProjectQueryService;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanUpdateRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class ProjectRevenuePlanApi {

    private final ProjectRevenuePlanManager projectRevenuePlanManager;
    private final ProjectRevenuePlanFinder projectRevenuePlanFinder;
    private final ProjectQueryService projectQueryService;
    private final CurrentActorResolver currentActorResolver;

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.read')")
    @GetMapping("/api/projectRevenuePlans/{projectId}")
    public List<ProjectRevenuePlanResponse> findByProjectId(@PathVariable Long projectId, Authentication authentication) {
        projectQueryService.findDetail(projectId);
        if (!projectQueryService.canRead(projectId, currentActorResolver.resolve(authentication))) {
            throw new org.springframework.security.access.AccessDeniedException("프로젝트 조회 권한 범위를 벗어났습니다.");
        }
        return projectRevenuePlanFinder.findByProjectId(projectId).stream()
                .map(ProjectRevenuePlanResponse::from)
                .toList();
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PostMapping("/api/projectRevenuePlans")
    public ProjectRevenuePlanResponse create(@RequestBody ProjectRevenuePlanCreateRequest request, Authentication authentication) {
        ProjectRevenuePlan projectRevenuePlan = projectRevenuePlanManager.create(currentActorResolver.resolve(authentication), request);
        return ProjectRevenuePlanResponse.from(projectRevenuePlan);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PutMapping("/api/projectRevenuePlans/{projectId}/{sequence}")
    public ProjectRevenuePlanResponse update(
            @PathVariable Long projectId,
            @PathVariable Integer sequence,
            @RequestBody ProjectRevenuePlanUpdateRequest request,
            Authentication authentication
    ) {
        ProjectRevenuePlan projectRevenuePlan = projectRevenuePlanManager.update(
                currentActorResolver.resolve(authentication),
                projectId,
                sequence,
                request
        );
        return ProjectRevenuePlanResponse.from(projectRevenuePlan);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PatchMapping("/api/projectRevenuePlans/{projectId}/{sequence}/issue")
    public ProjectRevenuePlanResponse issue(
            @PathVariable Long projectId,
            @PathVariable Integer sequence,
            Authentication authentication
    ) {
        ProjectRevenuePlan projectRevenuePlan = projectRevenuePlanManager.issue(
                currentActorResolver.resolve(authentication),
                projectId,
                sequence
        );
        return ProjectRevenuePlanResponse.from(projectRevenuePlan);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PatchMapping("/api/projectRevenuePlans/{projectId}/{sequence}/cancel")
    public ProjectRevenuePlanResponse cancel(
            @PathVariable Long projectId,
            @PathVariable Integer sequence,
            Authentication authentication
    ) {
        ProjectRevenuePlan projectRevenuePlan = projectRevenuePlanManager.cancel(
                currentActorResolver.resolve(authentication),
                projectId,
                sequence
        );
        return ProjectRevenuePlanResponse.from(projectRevenuePlan);
    }
}
