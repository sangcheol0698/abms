package kr.co.abacus.abms.adapter.api.employee;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

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

import kr.co.abacus.abms.adapter.api.common.EnumResponse;
import kr.co.abacus.abms.adapter.api.common.FilenameBuilder;
import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeCreateRequest;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeDetailResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeExcelUploadResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeePositionUpdateRequest;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeSearchResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeUpdateRequest;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeUpdateResponse;
import kr.co.abacus.abms.application.employee.EmployeeExcelService;
import kr.co.abacus.abms.application.employee.dto.EmployeeDetail;
import kr.co.abacus.abms.application.employee.dto.EmployeeExcelUploadResult;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.application.employee.inbound.EmployeeFinder;
import kr.co.abacus.abms.application.employee.inbound.EmployeeManager;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;

@RequiredArgsConstructor
@RestController
public class EmployeeApi {

    private static final String SYSTEM_DELETER = "SYSTEM"; // TODO: 추후 인증/인가 기능 도입 시 수정 필요

    private final EmployeeManager employeeManager;
    private final EmployeeFinder employeeFinder;
    private final EmployeeExcelService employeeExcelService;

    @PostMapping("/api/employees")
    public EmployeeCreateResponse create(@RequestBody @Valid EmployeeCreateRequest request) {
        Long employeeId = employeeManager.create(request.toCommand());

        return EmployeeCreateResponse.of(employeeId);
    }

    @GetMapping("/api/employees/{id}")
    public EmployeeDetailResponse findEmployeeDetail(@PathVariable Long id) {
        EmployeeDetail detail = employeeFinder.findEmployeeDetail(id);

        return EmployeeDetailResponse.of(detail);
    }

    @GetMapping("/api/employees")
    public PageResponse<EmployeeSearchResponse> search(@Valid EmployeeSearchCondition condition, Pageable pageable) {
        Page<EmployeeSummary> employeeSummaries = employeeFinder.search(condition, pageable);

        Page<EmployeeSearchResponse> responses = employeeSummaries.map(EmployeeSearchResponse::of);

        return PageResponse.of(responses);
    }

    @PutMapping("/api/employees/{id}")
    public EmployeeUpdateResponse update(@PathVariable Long id, @RequestBody @Valid EmployeeUpdateRequest request) {
        Long employeeId = employeeManager.updateInfo(id, request.toCommand());

        return EmployeeUpdateResponse.of(employeeId);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/employees/{id}")
    public void delete(@PathVariable Long id) {
        employeeManager.delete(id, SYSTEM_DELETER);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/api/employees/{id}/restore")
    public void restore(@PathVariable Long id) {
        employeeManager.restore(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/api/employees/{id}/resign")
    public void resign(@PathVariable Long id, @RequestParam LocalDate resignationDate) {
        employeeManager.resign(id, resignationDate);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/api/employees/{id}/take-leave")
    public void takeLeave(@PathVariable Long id) {
        employeeManager.takeLeave(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/api/employees/{id}/activate")
    public void activate(@PathVariable Long id) {
        employeeManager.activate(id);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/api/employees/{id}/promote")
    public void promote(@PathVariable Long id, @RequestBody EmployeePositionUpdateRequest request) {
        employeeManager.promote(id, request.position());
    }

    @GetMapping("/api/employees/positions")
    public List<EnumResponse> getEmployeePositions() {
        return Arrays.stream(EmployeePosition.values())
                .map(position -> new EnumResponse(position.name(), position.getDescription()))
                .toList();
    }

    @GetMapping("/api/employees/grades")
    public List<EnumResponse> getEmployeeGrades() {
        return Arrays.stream(EmployeeGrade.values())
                .map(grade -> new EnumResponse(grade.name(), grade.getDescription()))
                .toList();
    }

    @GetMapping("/api/employees/types")
    public List<EnumResponse> getEmployeeTypes() {
        return Arrays.stream(EmployeeType.values())
                .map(type -> new EnumResponse(type.name(), type.getDescription()))
                .toList();
    }

    @GetMapping("/api/employees/statuses")
    public List<EnumResponse> getEmployeeStatuses() {
        return Arrays.stream(EmployeeStatus.values())
                .map(status -> new EnumResponse(status.name(), status.getDescription()))
                .toList();
    }

    @GetMapping("/api/employees/avatars")
    public List<EnumResponse> getEmployeeAvatars() {
        return Arrays.stream(EmployeeAvatar.values())
                .map(avatar -> new EnumResponse(avatar.name(), avatar.getDescription()))
                .toList();
    }

    @GetMapping("/api/employees/excel/download")
    public ResponseEntity<Resource> downloadExcel(@Valid EmployeeSearchCondition request) {
        byte[] content = employeeExcelService.download(request);
        String filename = FilenameBuilder.build("employees");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(content));
    }

    @GetMapping("/api/employees/excel/sample")
    public ResponseEntity<Resource> downloadExcelSample() {
        byte[] content = employeeExcelService.downloadSample();
        String filename = FilenameBuilder.build("employees_sample");
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
            return EmployeeExcelUploadResponse.of(result);
        } catch (IOException ex) {
            throw new IllegalArgumentException("엑셀 파일을 읽는 중 오류가 발생했습니다.", ex);
        }
    }

}
