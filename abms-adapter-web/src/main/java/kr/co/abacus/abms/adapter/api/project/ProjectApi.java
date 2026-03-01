package kr.co.abacus.abms.adapter.api.project;

import jakarta.validation.Valid;

import kr.co.abacus.abms.adapter.api.common.FilenameBuilder;
import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.project.dto.*;
import kr.co.abacus.abms.application.department.DepartmentQueryService;
import kr.co.abacus.abms.application.party.PartyQueryService;
import kr.co.abacus.abms.application.project.ProjectExcelService;
import kr.co.abacus.abms.application.project.ProjectQueryService;
import kr.co.abacus.abms.application.project.dto.ProjectDetail;
import kr.co.abacus.abms.application.project.dto.ProjectExcelUploadResult;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.application.project.inbound.ProjectFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectManager;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ProjectApi {

    private final ProjectManager projectManager;
    private final ProjectFinder projectFinder;
    private final PartyQueryService partyQueryService;
    private final ProjectQueryService projectQueryService;
    private final ProjectExcelService projectExcelService;

    @PostMapping("/api/projects")
    public ProjectResponse create(@RequestBody ProjectCreateApiRequest request) {
        Project project = projectManager.create(request.toDomainRequest());
        String partyName = partyQueryService.getPartyName(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @GetMapping("/api/projects")
    public PageResponse<ProjectResponse> search(@Valid ProjectSearchCondition condition, Pageable pageable) {
        Page<ProjectSummary> projects = projectFinder.search(condition, pageable);

        return PageResponse.of(projects.map(project -> {
            String partyName = partyQueryService.getPartyName(project.partyId());
            return ProjectResponse.from(project, partyName);
        }));
    }

    @GetMapping("/api/projects/{id}")
    public ProjectDetailResponse find(@PathVariable Long id) {
        ProjectDetail detail = projectQueryService.findDetail(id);
        return ProjectDetailResponse.of(detail);
    }

    @PutMapping("/api/projects/{id}")
    public ProjectResponse update(@PathVariable Long id, @RequestBody ProjectUpdateApiRequest request) {
        Project project = projectManager.update(id, request.toDomainRequest());
        String partyName = partyQueryService.getPartyName(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @PatchMapping("/api/projects/{id}/complete")
    public ProjectResponse complete(@PathVariable Long id) {
        Project project = projectManager.complete(id);
        String partyName = partyQueryService.getPartyName(project.getPartyId());

        return ProjectResponse.from(project, partyName);
    }

    @PatchMapping("/api/projects/{id}/cancel")
    public ProjectResponse cancel(@PathVariable Long id) {
        Project project = projectManager.cancel(id);
        String partyName = partyQueryService.getPartyName(project.getPartyId());

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

    @GetMapping("/api/projects/excel/download")
    public ResponseEntity<Resource> downloadExcel(@Valid ProjectSearchCondition condition) {
        byte[] content = projectExcelService.download(condition);
        String filename = FilenameBuilder.build("projects");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(content));
    }

    @GetMapping("/api/projects/excel/sample")
    public ResponseEntity<Resource> downloadExcelSample() {
        byte[] content = projectExcelService.downloadSample();
        String filename = FilenameBuilder.build("projects_sample");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(content));
    }

    @PostMapping(value = "/api/projects/excel/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ProjectExcelUploadResponse uploadExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일을 선택하세요.");
        }
        try (InputStream inputStream = file.getInputStream()) {
            ProjectExcelUploadResult result = projectExcelService.upload(inputStream);
            return ProjectExcelUploadResponse.of(result);
        } catch (IOException ex) {
            throw new IllegalArgumentException("엑셀 파일을 읽는 중 오류가 발생했습니다.", ex);
        }
    }

}
