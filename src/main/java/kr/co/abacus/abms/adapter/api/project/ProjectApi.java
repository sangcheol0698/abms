package kr.co.abacus.abms.adapter.api.project;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectCreateApiRequest;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectStatusResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectUpdateApiRequest;
import kr.co.abacus.abms.application.party.PartyQueryService;
import kr.co.abacus.abms.application.project.inbound.ProjectFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectManager;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;

@RequiredArgsConstructor
@RestController
public class ProjectApi {

    private final ProjectManager projectManager;
    private final ProjectFinder projectFinder;
    private final PartyQueryService partyQueryService;

    @PostMapping("/api/projects")
    public ProjectResponse create(@RequestBody ProjectCreateApiRequest request) {
        Project project = projectManager.create(request.toDomainRequest());
        String partyName = partyQueryService.getPartyId(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @GetMapping("/api/projects")
    public PageResponse<ProjectResponse> list(Pageable pageable) {
        Page<Project> projects = projectFinder.findAll(pageable);

        return PageResponse.of(projects.map(project -> {
            String partyName = partyQueryService.getPartyId(project.getPartyId());
            return ProjectResponse.from(project, partyName);
        }));
    }

    @GetMapping("/api/projects/{id}")
    public ProjectResponse find(@PathVariable Long id) {
        Project project = projectFinder.find(id);
        String partyName = partyQueryService.getPartyId(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @PutMapping("/api/projects/{id}")
    public ProjectResponse update(@PathVariable Long id, @RequestBody ProjectUpdateApiRequest request) {
        Project project = projectManager.update(id, request.toDomainRequest());
        String partyName = partyQueryService.getPartyId(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @PatchMapping("/api/projects/{id}/complete")
    public ProjectResponse complete(@PathVariable Long id) {
        Project project = projectManager.complete(id);
        String partyName = partyQueryService.getPartyId(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @PatchMapping("/api/projects/{id}/cancel")
    public ProjectResponse cancel(@PathVariable Long id) {
        Project project = projectManager.cancel(id);
        String partyName = partyQueryService.getPartyId(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @DeleteMapping("/api/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        projectManager.delete(id);
    }

    @GetMapping("/api/projects/statuses")
    public List<ProjectStatusResponse> getProjectStatuses() {
        return Arrays.stream(ProjectStatus.values())
                .map(ProjectStatusResponse::of)
                .toList();
    }

}
