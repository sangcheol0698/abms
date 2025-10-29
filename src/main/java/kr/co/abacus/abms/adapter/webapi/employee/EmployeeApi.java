package kr.co.abacus.abms.adapter.webapi.employee;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeAvatarResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeExcelUploadResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeGradeResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeePositionResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeStatusResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeTypeResponse;
import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.employee.EmployeeExcelService;
import kr.co.abacus.abms.application.employee.dto.EmployeeExcelUploadResult;
import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeManager;
import kr.co.abacus.abms.application.employee.provided.EmployeeSearchRequest;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.employee.EmployeeUpdateRequest;

@RequiredArgsConstructor
@RestController
public class EmployeeApi {

    private static final String SYSTEM_DELETER = "SYSTEM";

    private final EmployeeManager employeeManager;
    private final EmployeeFinder employeeFinder;
    private final DepartmentFinder departmentFinder;
    private final EmployeeExcelService employeeExcelService;

    @PostMapping("/api/employees")
    public EmployeeCreateResponse create(@RequestBody @Valid EmployeeCreateRequest request) {
        Employee employee = employeeManager.create(request);

        return EmployeeCreateResponse.of(employee);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/employees/{id}")
    public void delete(@PathVariable UUID id) {
        employeeManager.delete(id, SYSTEM_DELETER);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/api/employees/{id}/restore")
    public void restore(@PathVariable UUID id) {
        employeeManager.restore(id);
    }

    @PutMapping("/api/employees/{id}")
    public EmployeeResponse update(@PathVariable UUID id, @RequestBody @Valid EmployeeUpdateRequest request) {
        Employee employee = employeeManager.updateInfo(id, request);
        Department department = departmentFinder.find(employee.getDepartmentId());

        return EmployeeResponse.of(employee, department);
    }

    @GetMapping("/api/employees/{id}")
    public EmployeeResponse find(@PathVariable UUID id) {
        Employee employee = employeeFinder.find(id);

        Department department = departmentFinder.find(employee.getDepartmentId());

        return EmployeeResponse.of(employee, department);
    }

    @GetMapping("/api/employees")
    public Page<EmployeeResponse> search(@Valid EmployeeSearchRequest request, Pageable pageable) {
        Page<Employee> employees = employeeFinder.search(request, pageable);
        List<Department> departments = departmentFinder.findAll(); // TODO: 최적화 방안 고려

        return employees.map(employee -> {
            Department department = getDepartment(employee, departments);
            return EmployeeResponse.of(employee, department);
        });
    }

    @GetMapping("/api/employees/grades")
    public List<EmployeeGradeResponse> getEmployeeGrades() {
        return Arrays.stream(EmployeeGrade.values())
            .map(EmployeeGradeResponse::of)
            .toList();
    }

    @GetMapping("/api/employees/positions")
    public List<EmployeePositionResponse> getEmployeePositions() {
        return Arrays.stream(EmployeePosition.values())
            .map(EmployeePositionResponse::of)
            .toList();
    }

    @GetMapping("/api/employees/types")
    public List<EmployeeTypeResponse> getEmployeeTypes() {
        return Arrays.stream(EmployeeType.values())
            .map(EmployeeTypeResponse::of)
            .toList();
    }

    @GetMapping("/api/employees/statuses")
    public List<EmployeeStatusResponse> getEmployeeStatuses() {
        return Arrays.stream(EmployeeStatus.values())
            .map(EmployeeStatusResponse::of)
            .toList();
    }

    @GetMapping("/api/employees/avatars")
    public List<EmployeeAvatarResponse> getEmployeeAvatars() {
        return Arrays.stream(EmployeeAvatar.values())
            .map(EmployeeAvatarResponse::of)
            .toList();
    }

    @GetMapping("/api/employees/excel/download")
    public ResponseEntity<Resource> downloadExcel(@Valid EmployeeSearchRequest request) {
        byte[] content = employeeExcelService.download(request);
        String filename = buildFilename("employees");
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .body(new ByteArrayResource(content));
    }

    @GetMapping("/api/employees/excel/sample")
    public ResponseEntity<Resource> downloadExcelSample() {
        byte[] content = employeeExcelService.downloadSample();
        String filename = buildFilename("employees_sample");
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .body(new ByteArrayResource(content));
    }

    @PostMapping(value = "/api/employees/excel/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EmployeeExcelUploadResponse uploadExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일을 선택하세요.");
        }
        try (InputStream inputStream = file.getInputStream()) {
            EmployeeExcelUploadResult result = employeeExcelService.upload(inputStream);
            return toResponse(result);
        } catch (IOException ex) {
            throw new IllegalArgumentException("엑셀 파일을 읽는 중 오류가 발생했습니다.", ex);
        }
    }

    @PatchMapping("/api/employees/{id}/resign")
    public void resign(@PathVariable UUID id, @RequestParam LocalDate resignationDate) {
        employeeManager.resign(id, resignationDate);
    }

    @PatchMapping("/api/employees/{id}/take-leave")
    public void takeLeave(@PathVariable UUID id) {
        employeeManager.takeLeave(id);
    }

    @PatchMapping("/api/employees/{id}/activate")
    public void activate(@PathVariable UUID id) {
        employeeManager.activate(id);
    }

    private String buildFilename(String prefix) {
        String timestamp = LocalDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return prefix + "_" + timestamp + ".xlsx";
    }

    private EmployeeExcelUploadResponse toResponse(EmployeeExcelUploadResult result) {
        List<EmployeeExcelUploadResponse.Failure> failures = result.excelFailures().stream()
            .map(failure -> new EmployeeExcelUploadResponse.Failure(failure.rowNumber(), failure.message()))
            .toList();
        return new EmployeeExcelUploadResponse(result.successCount(), failures);
    }

    private Department getDepartment(Employee employee, List<Department> departments) {
        return departments.stream()
            .filter(d -> d.getId().equals(employee.getDepartmentId()))
            .findFirst()
            .orElseThrow();
    }

}
