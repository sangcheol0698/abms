package kr.co.abacus.abms.adapter.infrastructure.employee;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.application.employee.outbound.EmployeeExcelImporter;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeExcelException;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;

@Component
public class PoiEmployeeExcelImporter implements EmployeeExcelImporter {

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
    public List<EmployeeCreateCommand> importEmployees(InputStream inputStream, Map<String, Long> departmentCodeMap) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
                throw new EmployeeExcelException("유효한 데이터가 포함된 시트를 찾을 수 없습니다.");
            }

            List<EmployeeCreateCommand> commands = new ArrayList<>();

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                commands.add(toCreateCommand(row, departmentCodeMap));
            }

            return commands;
        } catch (IOException ex) {
            throw new EmployeeExcelException("엑셀 파일을 읽는 도중 오류가 발생했습니다.", ex);
        }
    }

    private EmployeeCreateCommand toCreateCommand(Row row, Map<String, Long> departmentCodeMap) {
        String teamCode = getCellValue(row, 0);
        String email = getCellValue(row, 1);
        String name = getCellValue(row, 2);
        String joinedDate = getCellValue(row, 3);
        String birthDate = getCellValue(row, 4);
        String positionDesc = getCellValue(row, 5);
        String typeDesc = getCellValue(row, 6);
        String gradeDesc = getCellValue(row, 7);
        String memo = getCellValue(row, 8);

        Long departmentId = departmentCodeMap.get(teamCode);
        if (departmentId == null) {
            throw new IllegalArgumentException("존재하지 않는 부서 코드입니다: " + teamCode);
        }

        return EmployeeCreateCommand.builder()
                .departmentId(departmentId)
                .email(email)
                .name(name)
                .joinDate(LocalDate.parse(joinedDate))
                .birthDate(LocalDate.parse(birthDate))
                .position(EmployeePosition.fromDescription(positionDesc))
                .type(EmployeeType.fromDescription(typeDesc))
                .grade(EmployeeGrade.fromDescription(gradeDesc))
                .avatar(EmployeeAvatar.AQUA_SPLASH)
                .memo(memo)
                .build();
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

}
