package kr.co.abacus.abms.adapter.infrastructure.project;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.application.project.outbound.ProjectExcelImporter;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectExcelException;
import kr.co.abacus.abms.domain.project.ProjectStatus;

@Component
public class PoiProjectExcelImporter implements ProjectExcelImporter {

    private static final List<String> UPLOAD_HEADERS = List.of(
            "협력사 이름",
            "프로젝트 코드",
            "프로젝트명",
            "상태",
            "계약금액",
            "시작일",
            "종료일",
            "설명"
    );

    @Override
    public List<ProjectCreateRequest> importProjects(InputStream inputStream, Function<String, Long> partyLookup) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
                throw new ProjectExcelException("유효한 데이터가 포함된 시트를 찾을 수 없습니다.");
            }

            List<ProjectCreateRequest> requests = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                requests.add(toCreateRequest(row, partyLookup));
            }

            return requests;
        } catch (IOException ex) {
            throw new ProjectExcelException("엑셀 파일을 읽는 도중 오류가 발생했습니다.", ex);
        }
    }

    private ProjectCreateRequest toCreateRequest(Row row, Function<String, Long> partyLookup) {
        String partyName = getCellValue(row, 0);
        String leadDepartmentName = getCellValue(row, 1);
        String code = getCellValue(row, 2);
        String name = getCellValue(row, 3);
        String statusValue = getCellValue(row, 4);
        String contractAmount = getCellValue(row, 5);
        String startDate = getCellValue(row, 6);
        String endDate = getCellValue(row, 7);
        String description = getCellValue(row, 8);

        Long partyId = partyLookup.apply(partyName);
        if (partyId == null) {
            throw new IllegalArgumentException("존재하지 않는 협력사입니다: " + partyName);
        }

        return new ProjectCreateRequest(
                partyId,
                Long.parseLong(leadDepartmentName), // 임시
                code,
                name,
                description.isBlank() ? null : description,
                ProjectStatus.fromDescription(statusValue),
                parseAmount(contractAmount),
                LocalDate.parse(startDate),
                LocalDate.parse(endDate));
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < UPLOAD_HEADERS.size(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValue(row, i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return "";
        }
        CellType cellType = cell.getCellType();
        return switch (cellType) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                String raw = Double.toString(cell.getNumericCellValue());
                if (raw.endsWith(".0")) {
                    yield raw.substring(0, raw.length() - 2);
                }
                yield raw;
            }
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    yield "";
                }
            }
            case BLANK, _NONE, ERROR -> "";
        };
    }

    private Long parseAmount(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("계약금액을 입력하세요.");
        }
        String normalized = value.replace(",", "").replace("원", "").trim();
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("계약금액 형식이 올바르지 않습니다: " + value);
        }
    }

}
