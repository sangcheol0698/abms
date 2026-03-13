package kr.co.abacus.abms.adapter.api.project;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kr.co.abacus.abms.adapter.api.project.dto.ProjectRevenuePlanResponse;
import kr.co.abacus.abms.application.auth.inbound.AuthFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanManager;
import kr.co.abacus.abms.application.project.ProjectQueryService;
import kr.co.abacus.abms.application.project.authorization.ProjectReadAuthorizationService;
import kr.co.abacus.abms.application.project.authorization.ProjectWriteAuthorizationService;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class ProjectRevenuePlanApi {

    private final ProjectRevenuePlanManager projectRevenuePlanManager;
    private final ProjectRevenuePlanFinder projectRevenuePlanFinder;
    private final ProjectQueryService projectQueryService;
    private final AuthFinder authFinder;
    private final ProjectReadAuthorizationService projectReadAuthorizationService;
    private final ProjectWriteAuthorizationService projectWriteAuthorizationService;

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.read')")
    @GetMapping("/api/projectRevenuePlans/{projectId}")
    public List<ProjectRevenuePlanResponse> findByProjectId(@PathVariable Long projectId, Authentication authentication) {
        var detail = projectQueryService.findDetail(projectId);
        projectReadAuthorizationService.assertCanRead(resolveAccountId(authentication), detail.projectId(), detail.leadDepartmentId());
        return projectRevenuePlanFinder.findByProjectId(projectId).stream()
                .map(ProjectRevenuePlanResponse::from)
                .toList();
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PostMapping("/api/projectRevenuePlans")
    public ProjectRevenuePlanResponse create(@RequestBody ProjectRevenuePlanCreateRequest request, Authentication authentication) {
        projectWriteAuthorizationService.assertCanManage(resolveAccountId(authentication), request.projectId());
        ProjectRevenuePlan projectRevenuePlan = projectRevenuePlanManager.create(request);
        return ProjectRevenuePlanResponse.from(projectRevenuePlan);
    }

    private Long resolveAccountId(Authentication authentication) {
        return authFinder.getCurrentAccountId(authentication.getName());
    }

}
