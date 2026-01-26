package kr.co.abacus.abms.adapter.infrastructure.project;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.application.project.outbound.ProjectExcelExporter;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;

@Component
public class PoiProjectExcelExporter implements ProjectExcelExporter {

    private static final List<String> HEADERS = List.of(
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
    public byte[] export(List<Project> projects, Map<Long, String> partyNames) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Projects");
            createHeaderRow(workbook, sheet, HEADERS);

            int rowIndex = 1;
            for (Project project : projects) {
                Row row = sheet.createRow(rowIndex++);
                writeProjectRow(row, project, partyNames);
            }

            autosizeColumns(sheet, HEADERS);
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("엑셀 파일 생성 중 오류가 발생했습니다.", ex);
        }
    }

    @Override
    public byte[] exportSample() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Projects");
            createHeaderRow(workbook, sheet, HEADERS);

            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("네이버클라우드");
            example.createCell(1).setCellValue("PRJ-2025-001");
            example.createCell(2).setCellValue("프로젝트 엑셀 샘플");
            example.createCell(3).setCellValue(ProjectStatus.IN_PROGRESS.getDescription());
            example.createCell(4).setCellValue("120000000");
            example.createCell(5).setCellValue("2025-01-01");
            example.createCell(6).setCellValue("2025-12-31");
            example.createCell(7).setCellValue("설명은 선택입니다.");

            autosizeColumns(sheet, HEADERS);
            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("엑셀 샘플 생성 중 오류가 발생했습니다.", ex);
        }
    }

    private void createHeaderRow(Workbook workbook, Sheet sheet, List<String> headers) {
        Row header = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeProjectRow(Row row, Project project, Map<Long, String> partyNames) {
        int col = 0;
        String partyName = Optional.ofNullable(project.getPartyId())
                .map(id -> partyNames.getOrDefault(id, id.toString()))
                .orElse("");

        row.createCell(col++).setCellValue(partyName);
        row.createCell(col++).setCellValue(project.getCode());
        row.createCell(col++).setCellValue(project.getName());
        row.createCell(col++).setCellValue(project.getStatus().getDescription());
        row.createCell(col++).setCellValue(project.getContractAmount().amount().longValue());
        row.createCell(col++).setCellValue(
                Optional.ofNullable(project.getPeriod().startDate()).map(LocalDate::toString).orElse(""));
        row.createCell(col++).setCellValue(
                Optional.ofNullable(project.getPeriod().endDate()).map(LocalDate::toString).orElse(""));
        row.createCell(col).setCellValue(Optional.ofNullable(project.getDescription()).orElse(""));
    }

    private void autosizeColumns(Sheet sheet, List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(width + 1024, 10000));
        }
    }

}
