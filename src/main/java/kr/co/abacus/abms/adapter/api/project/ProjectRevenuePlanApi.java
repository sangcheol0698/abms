package kr.co.abacus.abms.adapter.api.project;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kr.co.abacus.abms.adapter.api.project.dto.ProjectRevenuePlanResponse;
import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanManager;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class ProjectRevenuePlanApi {

    private final ProjectRevenuePlanManager projectRevenuePlanManager;
    private final ProjectRevenuePlanFinder projectRevenuePlanFinder;

    @GetMapping("/api/projectRevenuePlans/{projectId}")
    public List<ProjectRevenuePlanResponse> findByProjectId(@PathVariable Long projectId) {
        return projectRevenuePlanFinder.findByProjectId(projectId).stream()
            .map(ProjectRevenuePlanResponse::from)
            .toList();
    }

    @PostMapping("/api/projectRevenuePlans")
    public ProjectRevenuePlanResponse create(@RequestBody ProjectRevenuePlanCreateRequest request) {
        ProjectRevenuePlan projectRevenuePlan = projectRevenuePlanManager.create(request);
        return ProjectRevenuePlanResponse.from(projectRevenuePlan);
    }

}
