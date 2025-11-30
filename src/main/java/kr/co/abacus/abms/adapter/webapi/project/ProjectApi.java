package kr.co.abacus.abms.adapter.webapi.project;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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

import kr.co.abacus.abms.adapter.webapi.PageResponse;
import kr.co.abacus.abms.adapter.webapi.project.dto.ProjectCreateApiRequest;
import kr.co.abacus.abms.adapter.webapi.project.dto.ProjectResponse;
import kr.co.abacus.abms.adapter.webapi.project.dto.ProjectStatusResponse;
import kr.co.abacus.abms.adapter.webapi.project.dto.ProjectUpdateApiRequest;
import kr.co.abacus.abms.application.party.PartyQueryService;
import kr.co.abacus.abms.application.project.provided.ProjectFinder;
import kr.co.abacus.abms.application.project.provided.ProjectManager;
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
    public ProjectResponse find(@PathVariable UUID id) {
        Project project = projectFinder.find(id);
        String partyName = partyQueryService.getPartyId(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @PutMapping("/api/projects/{id}")
    public ProjectResponse update(@PathVariable UUID id, @RequestBody ProjectUpdateApiRequest request) {
        Project project = projectManager.update(id, request.toDomainRequest());
        String partyName = partyQueryService.getPartyId(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @PatchMapping("/api/projects/{id}/complete")
    public ProjectResponse complete(@PathVariable UUID id) {
        Project project = projectManager.complete(id);
        String partyName = partyQueryService.getPartyId(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @PatchMapping("/api/projects/{id}/cancel")
    public ProjectResponse cancel(@PathVariable UUID id) {
        Project project = projectManager.cancel(id);
        String partyName = partyQueryService.getPartyId(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @DeleteMapping("/api/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") UUID id) {
        projectManager.delete(id);
    }

    @GetMapping("/api/projects/statuses")
    public List<ProjectStatusResponse> getProjectStatuses() {
        return Arrays.stream(ProjectStatus.values())
                .map(ProjectStatusResponse::of)
                .toList();
    }

}
