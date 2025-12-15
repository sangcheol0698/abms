package kr.co.abacus.abms.application.chat;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;

@Component
@RequiredArgsConstructor
public class EmployeeInfoTools {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Tool(description = "부서(직원)의 정보를 이름으로 조회하는 도구입니다. 이름, 부서명, 이메일, 직급, 등급, 상태, 입사일, 생년월일을 반환합니다.")
    public @Nullable EmployeeInfo getEmployeeInfo(String name) {
        return employeeRepository.findByName(name)
            .map(employee -> {
                String departmentName = departmentRepository.findByIdAndDeletedFalse(employee.getDepartmentId())
                    .map(dept -> dept.getName())
                    .orElse("부서 없음");
                
                return new EmployeeInfo(
                    employee.getName(),
                    departmentName,
                    employee.getEmail().address(),
                    employee.getPosition().getDescription(),
                    employee.getGrade().getDescription(),
                    employee.getStatus().getDescription(),
                    employee.getJoinDate().toString(),
                    employee.getBirthDate().toString()
                );
            })
            .orElse(null);
    }

    public record EmployeeInfo(
        String name,
        String departmentName,
        String email,
        String position,
        String grade,
        String status,
        String joinDate,
        String birthDate
    ) {}

}
