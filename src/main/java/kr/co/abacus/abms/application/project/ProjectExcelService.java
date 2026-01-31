package kr.co.abacus.abms.application.project;

import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.dto.ProjectExcelUploadResult;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.inbound.ProjectManager;
import kr.co.abacus.abms.application.project.outbound.ProjectExcelExporter;
import kr.co.abacus.abms.application.project.outbound.ProjectExcelImporter;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
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
    private final PartyRepository partyRepository;
    private final ProjectManager projectManager;
    private final ProjectExcelExporter projectExcelExporter;
    private final ProjectExcelImporter projectExcelImporter;

    public byte[] download(ProjectSearchCondition condition) {
        List<Project> projects = projectRepository.search(condition);
        Map<Long, String> partyNames = loadPartyNameMap();
        return projectExcelExporter.export(projects, partyNames);
    }

    public byte[] downloadSample() {
        return projectExcelExporter.exportSample();
    }

    @Transactional
    public ProjectExcelUploadResult upload(InputStream inputStream) {
        List<ProjectCreateRequest> requests = projectExcelImporter.importProjects(inputStream, this::getPartyIdByName);

        List<ProjectExcelUploadResult.ExcelFailure> excelFailures = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < requests.size(); i++) {
            try {
                projectManager.create(requests.get(i));
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

}
