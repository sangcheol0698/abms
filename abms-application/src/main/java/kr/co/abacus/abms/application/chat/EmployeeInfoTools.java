package kr.co.abacus.abms.application.chat;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeInfoTools {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Setter
    private @Nullable Consumer<String> toolCallNotifier;

    @Tool(description = "직원의 정보를 이름으로 조회하는 도구입니다. 이름, 부서명, 이메일, 직급, 등급, 상태, 입사일, 생년월일, 그리고 상세 페이지 링크를 반환합니다.")
    public @Nullable EmployeeInfo getEmployeeInfo(String name) {
        notifyToolCall("getEmployeeInfo");
        return resolveEmployeeInfo(name, null);
    }

    @Tool(description = "직원의 정보를 이름과 부서명으로 조회하는 도구입니다. 동명이인 구분이 필요할 때 사용합니다.")
    public @Nullable EmployeeInfo getEmployeeInfoByDepartment(String name, String departmentName) {
        notifyToolCall("getEmployeeInfoByDepartment");
        return resolveEmployeeInfo(name, departmentName);
    }

    @Tool(description = "직원의 정보를 직원 ID로 정확히 조회하는 도구입니다. 동명이인 재확인 후 상세 조회할 때 사용합니다.")
    public @Nullable EmployeeInfo getEmployeeInfoById(Long employeeId) {
        notifyToolCall("getEmployeeInfoById");
        return employeeRepository.findByIdAndDeletedFalse(employeeId)
                .map(this::toEmployeeInfo)
                .orElse(null);
    }

    private void notifyToolCall(String toolName) {
        if (toolCallNotifier != null) {
            toolCallNotifier.accept(toolName);
        }
    }

    private @Nullable EmployeeInfo resolveEmployeeInfo(String name, @Nullable String departmentName) {
        EmployeeSearchCondition condition = new EmployeeSearchCondition(name, null, null, null, null, null);
        List<Employee> candidates = employeeRepository.search(condition).stream()
                .filter(employee -> employee.getName().equalsIgnoreCase(name))
                .toList();

        if (candidates.isEmpty()) {
            return null;
        }

        List<Employee> filteredCandidates = candidates;
        if (departmentName != null && !departmentName.isBlank()) {
            String normalizedDepartment = departmentName.trim().toLowerCase(Locale.ROOT);
            filteredCandidates = candidates.stream()
                    .filter(employee -> departmentRepository.findByIdAndDeletedFalse(employee.getDepartmentId())
                            .map(dept -> dept.getName().toLowerCase(Locale.ROOT).contains(normalizedDepartment))
                            .orElse(false))
                    .toList();
        }

        if (filteredCandidates.size() != 1) {
            return null;
        }

        return toEmployeeInfo(filteredCandidates.getFirst());
    }

    private EmployeeInfo toEmployeeInfo(Employee employee) {
        String departmentName = departmentRepository
                .findByIdAndDeletedFalse(employee.getDepartmentId())
                .map(dept -> dept.getName())
                .orElse("부서 없음");

        return new EmployeeInfo(
                employee.getIdOrThrow(),
                employee.getName(),
                departmentName,
                employee.getEmail().address(),
                employee.getPosition().getDescription(),
                employee.getGrade().getDescription(),
                employee.getStatus().getDescription(),
                employee.getJoinDate().toString(),
                employee.getBirthDate().toString(),
                "/employees/" + employee.getIdOrThrow());
    }

    public record EmployeeInfo(
            Long id,
            String name,
            String departmentName,
            String email,
            String position,
            String grade,
            String status,
            String joinDate,
            String birthDate,
            String link) {

    }

}
