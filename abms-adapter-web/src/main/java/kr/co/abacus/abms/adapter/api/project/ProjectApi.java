package kr.co.abacus.abms.adapter.api.project;

import jakarta.validation.Valid;

import kr.co.abacus.abms.adapter.api.common.FilenameBuilder;
import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectCreateApiRequest;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectCreateResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectDetailResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectExcelUploadResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectStatusResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectUpdateApiRequest;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectUpdateResponse;
import kr.co.abacus.abms.application.project.ProjectExcelService;
import kr.co.abacus.abms.application.project.ProjectQueryService;
import kr.co.abacus.abms.application.project.dto.ProjectDetail;
import kr.co.abacus.abms.application.project.dto.ProjectExcelUploadResult;
import kr.co.abacus.abms.application.project.dto.ProjectOverviewSummary;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.application.project.inbound.ProjectFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectManager;
import kr.co.abacus.abms.domain.project.ProjectStatus;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final ProjectQueryService projectQueryService;
    private final ProjectExcelService projectExcelService;

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PostMapping("/api/projects")
    public ProjectCreateResponse create(@RequestBody ProjectCreateApiRequest request) {
        Long projectId = projectManager.create(request.toCommand());
        return ProjectCreateResponse.of(projectId);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.read')")
    @GetMapping("/api/projects")
    public PageResponse<ProjectResponse> search(@Valid ProjectSearchCondition condition, Pageable pageable) {
        Page<ProjectSummary> projects = projectFinder.search(condition, pageable);

        return PageResponse.of(projects.map(ProjectResponse::from));
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.read')")
    @GetMapping("/api/projects/summary")
    public ProjectOverviewSummary getOverviewSummary(@Valid ProjectSearchCondition condition) {
        return projectFinder.getOverviewSummary(condition);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.read')")
    @GetMapping("/api/projects/{id}")
    public ProjectDetailResponse find(@PathVariable Long id) {
        ProjectDetail detail = projectQueryService.findDetail(id);
        return ProjectDetailResponse.of(detail);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PutMapping("/api/projects/{id}")
    public ProjectUpdateResponse update(@PathVariable Long id, @RequestBody ProjectUpdateApiRequest request) {
        Long projectId = projectManager.update(id, request.toCommand());
        return ProjectUpdateResponse.of(projectId);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PatchMapping("/api/projects/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void complete(@PathVariable Long id) {
        projectManager.complete(id);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @PatchMapping("/api/projects/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        projectManager.cancel(id);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.write')")
    @DeleteMapping("/api/projects/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        projectManager.delete(id);
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.read')")
    @GetMapping("/api/projects/statuses")
    public List<ProjectStatusResponse> getProjectStatuses() {
        return Arrays.stream(ProjectStatus.values())
                .map(ProjectStatusResponse::of)
                .toList();
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.excel.download')")
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

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'project.excel.upload')")
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
