package kr.co.abacus.abms.adapter.infrastructure.employee;

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

import kr.co.abacus.abms.application.employee.outbound.EmployeeExcelExporter;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;

@Component
public class PoiEmployeeExcelExporter implements EmployeeExcelExporter {

    private static final List<String> DOWNLOAD_HEADERS = List.of(
            "부서 이름",
            "이메일",
            "이름",
            "입사일",
            "생년월일",
            "직책",
            "근무 유형",
            "등급",
            "메모"
    );

    private static final List<String> UPLOAD_HEADERS = List.of(
            "부서 코드",
            "이메일",
            "이름",
            "입사일",
            "생년월일",
            "직책",
            "근무 유형",
            "등급",
            "메모"
    );

    @Override
    public byte[] export(List<Employee> employees, Map<Long, String> departmentNames) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Employees");
            createHeaderRow(workbook, sheet, DOWNLOAD_HEADERS);

            int rowIndex = 1;
            for (Employee employee : employees) {
                Row row = sheet.createRow(rowIndex++);
                writeEmployeeRow(row, employee, departmentNames);
            }

            autosizeColumns(sheet, DOWNLOAD_HEADERS);
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

            Sheet sheet = workbook.createSheet("Employees");
            createHeaderRow(workbook, sheet, UPLOAD_HEADERS);

            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("예: TEAM-FE");
            example.createCell(1).setCellValue("employee@example.com");
            example.createCell(2).setCellValue("홍길동");
            example.createCell(3).setCellValue("2025-01-01");
            example.createCell(4).setCellValue("1990-05-10");
            example.createCell(5).setCellValue(EmployeePosition.ASSOCIATE.getDescription());
            example.createCell(6).setCellValue(EmployeeType.FULL_TIME.getDescription());
            example.createCell(7).setCellValue(EmployeeGrade.JUNIOR.getDescription());
            example.createCell(8).setCellValue("메모는 선택입니다.");

            autosizeColumns(sheet, UPLOAD_HEADERS);
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

    private void writeEmployeeRow(Row row, Employee employee, Map<Long, String> departmentNames) {
        int col = 0;
        String departmentName = Optional.ofNullable(employee.getDepartmentId())
                .map(id -> departmentNames.getOrDefault(id, id.toString()))
                .orElse("");

        row.createCell(col++).setCellValue(departmentName);
        row.createCell(col++).setCellValue(employee.getEmail().address());
        row.createCell(col++).setCellValue(employee.getName());
        row.createCell(col++).setCellValue(Optional.ofNullable(employee.getJoinDate()).map(LocalDate::toString).orElse(""));
        row.createCell(col++).setCellValue(Optional.ofNullable(employee.getBirthDate()).map(LocalDate::toString).orElse(""));
        row.createCell(col++).setCellValue(employee.getPosition().getDescription());
        row.createCell(col++).setCellValue(employee.getType().getDescription());
        row.createCell(col++).setCellValue(employee.getGrade().getDescription());
        row.createCell(col).setCellValue(Optional.ofNullable(employee.getMemo()).orElse(""));
    }

    private void autosizeColumns(Sheet sheet, List<String> headers) {
        for (int i = 0; i < headers.size(); i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.min(width + 1024, 10000));
        }
    }

}
