package kr.co.abacus.abms.application.employee;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.web.employee.dto.EmployeeCreateRequest;
import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeExcelUploadResult;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.inbound.EmployeeManager;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeExcelException;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EmployeeExcelService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
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

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeManager employeeManager;

    public byte[] download(EmployeeSearchCondition request) {
        List<Employee> employees = employeeRepository.search(request);
        Map<UUID, String> departmentNames = loadDepartmentNameMap();

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

    public byte[] downloadSample() {
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

    @Transactional
    public EmployeeExcelUploadResult upload(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
                throw new EmployeeExcelException("유효한 데이터가 포함된 시트를 찾을 수 없습니다.");
            }

            Map<String, UUID> departmentCodeMap = loadDepartmentCodeMap();

            List<EmployeeExcelUploadResult.ExcelFailure> excelFailures = new ArrayList<>();
            int successCount = 0;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    EmployeeCreateRequest createRequest = toCreateRequest(row, departmentCodeMap);
                    // employeeManager.create(createRequest);
                    successCount++;
                } catch (Exception ex) {
                    excelFailures.add(new EmployeeExcelUploadResult.ExcelFailure(rowIndex + 1, resolveMessage(ex)));
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

                throw new EmployeeExcelException(message);
            }

            return new EmployeeExcelUploadResult(successCount, excelFailures);
        } catch (IOException ex) {
            throw new EmployeeExcelException("엑셀 파일을 읽는 도중 오류가 발생했습니다.", ex);
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

    private void writeEmployeeRow(Row row, Employee employee, Map<UUID, String> departmentNames) {
        int col = 0;
        String departmentName = Optional.of(employee.getDepartmentId())
            .map(id -> departmentNames.getOrDefault(id, id.toString()))
            .orElse("");

        row.createCell(col++).setCellValue(departmentName);
        row.createCell(col++).setCellValue(employee.getEmail().address());
        row.createCell(col++).setCellValue(employee.getName());
        row.createCell(col++).setCellValue(Optional.of(employee.getJoinDate()).map(LocalDate::toString).orElse(""));
        row.createCell(col++).setCellValue(Optional.of(employee.getBirthDate()).map(LocalDate::toString).orElse(""));
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

    private EmployeeCreateRequest toCreateRequest(Row row, Map<String, UUID> departmentByCode) {
        String departmentCodeRaw = getCellValue(row, 0);
        String email = getCellValue(row, 1);
        String name = getCellValue(row, 2);
        String joinDateRaw = getCellValue(row, 3);
        String birthDateRaw = getCellValue(row, 4);
        String positionRaw = getCellValue(row, 5);
        String typeRaw = getCellValue(row, 6);
        String gradeRaw = getCellValue(row, 7);
        String memo = getCellValue(row, 8);

        UUID departmentId = resolveDepartmentId(departmentCodeRaw, departmentByCode);
        LocalDate joinDate = parseDate(joinDateRaw, "입사일");
        LocalDate birthDate = parseDate(birthDateRaw, "생년월일");
        EmployeePosition position = parseEnum(positionRaw, EmployeePosition.class, "직책");
        EmployeeType type = parseEnum(typeRaw, EmployeeType.class, "근무 유형");
        EmployeeGrade grade = parseEnum(gradeRaw, EmployeeGrade.class, "등급");
        EmployeeAvatar avatar = randomAvatar();

        return new EmployeeCreateRequest(
            departmentId,
            email,
            name,
            joinDate,
            birthDate,
            position,
            type,
            grade,
            avatar,
            memo.isEmpty() ? null : memo
        );
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < EmployeeExcelService.UPLOAD_HEADERS.size(); i++) {
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
            case FORMULA -> cell.getRichStringCellValue().getString().trim();
            case BLANK, _NONE, ERROR -> "";
        };
    }

    private Map<UUID, String> loadDepartmentNameMap() {
        return departmentRepository.findAllByDeletedFalse()
            .stream()
            .collect(Collectors.toMap(Department::getId, Department::getName));
    }

    private Map<String, UUID> loadDepartmentCodeMap() {
        return departmentRepository.findAllByDeletedFalse()
            .stream()
            .collect(Collectors.toMap(Department::getCode, Department::getId));
    }

    private UUID resolveDepartmentId(String value, Map<String, UUID> departmentByCode) {
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("부서 코드는 필수입니다.");
        }
        UUID departmentId = departmentByCode.get(normalized);
        if (departmentId == null) {
            throw new IllegalArgumentException("존재하지 않는 부서 코드입니다: " + normalized);
        }
        return departmentId;
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 값이 비어 있습니다.");
        }
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(fieldName + " 값이 올바른 날짜 형식(yyyy-MM-dd)이 아닙니다.");
        }
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> enumType, String fieldName) {
        if (value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 값이 비어 있습니다.");
        }
        String normalized = value.trim();
        return EnumSet.allOf(enumType)
            .stream()
            .filter(candidate -> matchesEnum(candidate, normalized))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(fieldName + " 값이 올바르지 않습니다: " + value));
    }

    private boolean matchesEnum(Enum<?> candidate, String value) {
        if (candidate.name().equalsIgnoreCase(value.replace(' ', '_'))) {
            return true;
        }
        return switch (candidate) {
            case EmployeePosition position -> position.getDescription().equals(value);
            case EmployeeType type -> type.getDescription().equals(value);
            case EmployeeGrade grade -> grade.getDescription().equals(value);
            default -> false;
        };
    }

    private EmployeeAvatar randomAvatar() {
        EmployeeAvatar[] avatars = EmployeeAvatar.values();
        int index = ThreadLocalRandom.current().nextInt(avatars.length);
        return avatars[index];
    }

    private String resolveMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message;
    }

}
