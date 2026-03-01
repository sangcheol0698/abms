package kr.co.abacus.abms.application.employee;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.application.employee.dto.EmployeeExcelUploadResult;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.inbound.EmployeeManager;
import kr.co.abacus.abms.application.employee.outbound.EmployeeExcelExporter;
import kr.co.abacus.abms.application.employee.outbound.EmployeeExcelImporter;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeExcelException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EmployeeExcelService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeManager employeeManager;
    private final EmployeeExcelExporter employeeExcelExporter;
    private final EmployeeExcelImporter employeeExcelImporter;

    public byte[] download(EmployeeSearchCondition condition) {
        List<Employee> employees = employeeRepository.search(condition);

        Map<Long, String> departmentNames = loadDepartmentNameMap();

        return employeeExcelExporter.export(employees, departmentNames);
    }

    public byte[] downloadSample() {
        return employeeExcelExporter.exportSample();
    }

    @Transactional
    public EmployeeExcelUploadResult upload(InputStream inputStream) {
        Map<String, Long> departmentCodeMap = loadDepartmentCodeMap();
        List<EmployeeCreateCommand> commands = employeeExcelImporter.importEmployees(inputStream, departmentCodeMap);

        List<EmployeeExcelUploadResult.ExcelFailure> excelFailures = new ArrayList<>();
        int successCount = 0;

        for (int i = 0; i < commands.size(); i++) {
            try {
                employeeManager.create(commands.get(i));
                successCount++;
            } catch (Exception ex) {
                // commands에는 이미 필터링된 데이터만 들어있지만, 
                // 행 번호를 정확히 알기 어렵다는 단점이 있음. 
                // Importer가 행 번호를 포함한 결과를 반환하도록 개선하는 것이 좋음.
                // 일단 기존 로직과 최대한 유사하게 유지하기 위해 successCount와 excelFailures 관리
                excelFailures.add(new EmployeeExcelUploadResult.ExcelFailure(i + 2, resolveMessage(ex)));
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
    }

    private Map<Long, String> loadDepartmentNameMap() {
        return departmentRepository.findAllByDeletedFalse()
                .stream()
                .collect(Collectors.toMap(Department::getId, Department::getName));
    }

    private Map<String, Long> loadDepartmentCodeMap() {
        return departmentRepository.findAllByDeletedFalse()
                .stream()
                .collect(Collectors.toMap(Department::getCode, Department::getId));
    }

    private String resolveMessage(Exception ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message;
    }

}
