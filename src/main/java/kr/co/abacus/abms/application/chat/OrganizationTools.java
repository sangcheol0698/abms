package kr.co.abacus.abms.application.chat;

import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;

@Component
@RequiredArgsConstructor
public class OrganizationTools {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Tool(description = "부서명으로 부서 정보를 조회하는 도구입니다. 부서명, 부서코드, 부서유형, 부서장 정보를 반환합니다.")
    public @Nullable DepartmentInfo getDepartmentInfo(String departmentName) {
        return departmentRepository.findByName(departmentName)
            .map(dept -> {
                String leaderName = "부서장 없음";
                if (dept.getLeaderEmployeeId() != null) {
                    leaderName = employeeRepository.findById(dept.getLeaderEmployeeId())
                        .map(employee -> employee.getName())
                        .orElse("부서장 정보 없음");
                }
                return new DepartmentInfo(
                    dept.getName(),
                    dept.getCode(),
                    dept.getType().getDescription(),
                    leaderName
                );
            })
            .orElse(null);
    }

    @Tool(description = "특정 부서의 하위 부서 목록을 조회하는 도구입니다. 부서명, 부서코드를 반환합니다.")
    public @Nullable List<SubDepartmentInfo> getSubDepartments(String parentDepartmentName) {
        return departmentRepository.findByName(parentDepartmentName)
            .map(parent -> parent.getChildren().stream()
                .map(child -> new SubDepartmentInfo(
                    child.getName(),
                    child.getCode(),
                    child.getType().getDescription()
                ))
                .collect(Collectors.toList())
            )
            .orElse(null);
    }

    public record DepartmentInfo(
        String name,
        String code,
        String type,
        String leader
    ) {}

    public record SubDepartmentInfo(
        String name,
        String code,
        String type
    ) {}

}
