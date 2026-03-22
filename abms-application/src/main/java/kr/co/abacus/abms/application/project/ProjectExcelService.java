package kr.co.abacus.abms.application.project;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPermissionSupport;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.dto.ProjectCreateCommand;
import kr.co.abacus.abms.application.project.dto.ProjectExcelUploadResult;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.inbound.ProjectManager;
import kr.co.abacus.abms.application.project.outbound.ProjectExcelExporter;
import kr.co.abacus.abms.application.project.outbound.ProjectExcelImporter;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectExcelException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProjectExcelService {

    private final ProjectRepository projectRepository;
    private final DepartmentRepository departmentRepository;
    private final PartyRepository partyRepository;
    private final ProjectManager projectManager;
    private final ProjectExcelExporter projectExcelExporter;
    private final ProjectExcelImporter projectExcelImporter;
    private final CurrentActorPermissionSupport permissionSupport;

    public byte[] download(ProjectSearchCondition condition, CurrentActor actor) {
        List<Project> projects = projectRepository.search(condition, actor);
        Map<Long, String> partyNames = loadPartyNameMap();
        return projectExcelExporter.export(projects, partyNames);
    }

    public byte[] downloadSample() {
        return projectExcelExporter.exportSample();
    }

    @Transactional
    public ProjectExcelUploadResult upload(InputStream inputStream, CurrentActor actor) {
        List<ProjectCreateCommand> commands = projectExcelImporter.importProjects(inputStream, this::getPartyIdByName);
        java.util.Set<kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope> uploadScopes = validateCanUpload(actor, commands);
        CurrentActor writeActor = createWriteActor(actor, uploadScopes);

        List<ProjectExcelUploadResult.ExcelFailure> excelFailures = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < commands.size(); i++) {
            try {
                projectManager.create(writeActor, commands.get(i));
                successCount++;
            } catch (Exception ex) {
                excelFailures.add(new ProjectExcelUploadResult.ExcelFailure(i + 2, resolveMessage(ex)));
            }
        }

        if (!excelFailures.isEmpty()) {
            String detailLines = excelFailures.stream()
                    .map(excelFailure -> {
                        String prefix = excelFailure.rowNumber() > 0 ? excelFailure.rowNumber() + "행: " : "";
                        return prefix + excelFailure.message();
                    })
                    .collect(Collectors.joining("\n"));

            String message = String.format(
                    "엑셀 업로드 실패: 총 %d건의 오류가 있습니다.%n%s",
                    excelFailures.size(),
                    detailLines
            );

            throw new ProjectExcelException(message);
        }

        return new ProjectExcelUploadResult(successCount, excelFailures);
    }

    private Long getPartyIdByName(String name) {
        return partyRepository.findByNameAndDeletedFalse(name)
                .map(Party::getId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 협력사입니다: " + name));
    }

    private Map<Long, String> loadPartyNameMap() {
        return partyRepository.findAllByDeletedFalse()
                .stream()
                .collect(Collectors.toMap(Party::getId, Party::getName));
    }

    private String resolveMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message;
    }

    private java.util.Set<kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope> validateCanUpload(
            CurrentActor actor,
            List<ProjectCreateCommand> commands
    ) {
        java.util.Set<kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope> scopes = permissionSupport.requirePermission(
                actor,
                "project.excel.upload",
                "프로젝트 엑셀 업로드 권한 범위를 벗어났습니다."
        );
        if (scopes.contains(kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope.ALL)) {
            return scopes;
        }

        java.util.Set<Long> allowedDepartmentIds = permissionSupport.resolveAllowedDepartmentIds(actor, scopes);

        boolean unauthorizedExists = commands.stream()
                .map(ProjectCreateCommand::leadDepartmentId)
                .anyMatch(leadDepartmentId -> !allowedDepartmentIds.contains(leadDepartmentId));
        if (unauthorizedExists) {
            throw new org.springframework.security.access.AccessDeniedException("프로젝트 엑셀 업로드 권한 범위를 벗어났습니다.");
        }

        return scopes;
    }

    private CurrentActor createWriteActor(
            CurrentActor actor,
            java.util.Set<kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope> scopes
    ) {
        return new CurrentActor(
                actor.accountId(),
                actor.username(),
                actor.employeeId(),
                actor.departmentId(),
                java.util.Map.of("project.write", scopes)
        );
    }

}
