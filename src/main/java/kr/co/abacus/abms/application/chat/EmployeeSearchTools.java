package kr.co.abacus.abms.application.chat;

import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.provided.EmployeeSearchRequest;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;

@Component
@RequiredArgsConstructor
public class EmployeeSearchTools {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Tool(description = "특정 부서에 속한 직원 목록을 조회하는 도구입니다. 부서명을 입력하면 해당 부서의 직원 이름, 직급, 등급, 재직상태를 반환합니다.")
    public @Nullable List<EmployeeInfo> findEmployeesByDepartment(String departmentName) {
        return departmentRepository.findByName(departmentName)
            .map(dept -> {
                EmployeeSearchRequest request = new EmployeeSearchRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(dept.getId())
                );
                return employeeRepository.search(request).stream()
                    .map(this::toEmployeeInfo)
                    .toList();
            })
            .orElse(null);
    }

    @Tool(description = "특정 직급의 직원 목록을 조회하는 도구입니다. 직급 한글명(예: 사원, 선임, 책임, 팀장, 수석, 이사 등)을 입력하면 해당 직급의 직원 이름, 부서명, 등급, 상태를 반환합니다.")
    public @Nullable List<EmployeeInfo> findEmployeesByPosition(String positionName) {
        EmployeePosition position = parsePosition(positionName);
        if (position == null) {
            return null;
        }

        EmployeeSearchRequest request = new EmployeeSearchRequest(
            null,
            List.of(position),
            null,
            null,
            null,
            null
        );
        return employeeRepository.search(request).stream()
            .map(this::toEmployeeInfo)
            .toList();
    }

    @Tool(description = "특정 등급의 직원 목록을 조회하는 도구입니다. 등급 한글명(초급, 중급, 고급, 특급)을 입력하면 해당 등급의 직원 이름, 부서명, 직급, 상태를 반환합니다.")
    public @Nullable List<EmployeeInfo> findEmployeesByGrade(String gradeName) {
        EmployeeGrade grade = parseGrade(gradeName);
        if (grade == null) {
            return null;
        }

        EmployeeSearchRequest request = new EmployeeSearchRequest(
            null,
            null,
            null,
            null,
            List.of(grade),
            null
        );
        return employeeRepository.search(request).stream()
            .map(this::toEmployeeInfo)
            .toList();
    }

    @Tool(description = "특정 재직상태의 직원 목록을 조회하는 도구입니다. 상태 한글명(재직, 휴직, 퇴사)을 입력하면 해당 상태의 직원 이름, 부서명, 직급, 등급을 반환합니다.")
    public @Nullable List<EmployeeInfo> findEmployeesByStatus(String statusName) {
        EmployeeStatus status = parseStatus(statusName);
        if (status == null) {
            return null;
        }

        EmployeeSearchRequest request = new EmployeeSearchRequest(
            null,
            null,
            null,
            List.of(status),
            null,
            null
        );
        return employeeRepository.search(request).stream()
            .map(this::toEmployeeInfo)
            .toList();
    }

    private EmployeeInfo toEmployeeInfo(Employee employee) {
        String departmentName = departmentRepository.findByIdAndDeletedFalse(employee.getDepartmentId())
            .map(dept -> dept.getName())
            .orElse("부서 없음");
        
        return new EmployeeInfo(
            employee.getName(),
            departmentName,
            employee.getPosition().getDescription(),
            employee.getGrade().getDescription(),
            employee.getStatus().getDescription()
        );
    }

    private @Nullable EmployeePosition parsePosition(String positionName) {
        return Arrays.stream(EmployeePosition.values())
            .filter(p -> p.getDescription().equals(positionName))
            .findFirst()
            .orElse(null);
    }

    private @Nullable EmployeeGrade parseGrade(String gradeName) {
        return Arrays.stream(EmployeeGrade.values())
            .filter(g -> g.getDescription().equals(gradeName))
            .findFirst()
            .orElse(null);
    }

    private @Nullable EmployeeStatus parseStatus(String statusName) {
        return Arrays.stream(EmployeeStatus.values())
            .filter(s -> s.getDescription().equals(statusName))
            .findFirst()
            .orElse(null);
    }

    public record EmployeeInfo(
        String name,
        String departmentName,
        String position,
        String grade,
        String status
    ) {}

}
