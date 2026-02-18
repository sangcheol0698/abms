package kr.co.abacus.abms.application.chat;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class EmployeeInfoTools {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Setter
    private @Nullable Consumer<String> toolCallNotifier;

    @Tool(description = "직원의 정보를 이름으로 조회하는 도구입니다. 이름, 부서명, 이메일, 직급, 등급, 상태, 입사일, 생년월일, 그리고 상세 페이지 링크를 반환합니다.")
    public @Nullable EmployeeInfo getEmployeeInfo(String name) {
        // Notify tool call
        if (toolCallNotifier != null) {
            toolCallNotifier.accept("getEmployeeInfo");
        }

        return employeeRepository.findByName(name)
                .map(employee -> {
                    String departmentName = departmentRepository
                            .findByIdAndDeletedFalse(employee.getDepartmentId())
                            .map(dept -> dept.getName())
                            .orElse("부서 없음");

                    return new EmployeeInfo(
                            employee.getId(),
                            employee.getName(),
                            departmentName,
                            employee.getEmail().address(),
                            employee.getPosition().getDescription(),
                            employee.getGrade().getDescription(),
                            employee.getStatus().getDescription(),
                            employee.getJoinDate().toString(),
                            employee.getBirthDate().toString(),
                            "/employees/" + employee.getId()
                    );
                })
                .orElse(null);
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
