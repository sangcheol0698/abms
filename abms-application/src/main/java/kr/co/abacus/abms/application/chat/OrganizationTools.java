package kr.co.abacus.abms.application.chat;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationTools {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    @Setter
    private @Nullable Consumer<String> toolCallNotifier;

    @Tool(description = "부서명으로 부서 정보를 조회하는 도구입니다. 부서명, 부서코드, 부서유형, 부서장 정보와 상세 페이지 링크를 반환합니다.")
    public @Nullable DepartmentInfo getDepartmentInfo(String departmentName) {
        if (toolCallNotifier != null) {
            toolCallNotifier.accept("getDepartmentInfo");
        }

        return departmentRepository.findByName(departmentName)
                .map(dept -> {
                    String leaderName = "부서장 없음";
                    if (dept.getLeaderEmployeeId() != null) {
                        leaderName = employeeRepository.findById(dept.getLeaderEmployeeId())
                                .map(Employee::getName)
                                .orElse("부서장 정보 없음");
                    }
                    return new DepartmentInfo(
                            dept.getIdOrThrow(),
                            dept.getName(),
                            dept.getCode(),
                            dept.getType().getDescription(),
                            leaderName,
                            "/departments/" + dept.getIdOrThrow());
                })
                .orElse(null);
    }

    @Tool(description = "특정 부서의 하위 부서 목록을 조회하는 도구입니다. 부서명, 부서코드, 링크를 반환합니다.")
    public @Nullable List<SubDepartmentInfo> getSubDepartments(String parentDepartmentName) {
        if (toolCallNotifier != null) {
            toolCallNotifier.accept("getSubDepartments");
        }

        return departmentRepository.findByName(parentDepartmentName)
                .map(parent -> parent.getChildren().stream()
                        .map(child -> new SubDepartmentInfo(
                                child.getIdOrThrow(),
                                child.getName(),
                                child.getCode(),
                                child.getType().getDescription(),
                                "/departments/" + child.getIdOrThrow()))
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    @Tool(description = "특정 부서의 소속 직원 목록을 조회하는 도구입니다. 최대 10명까지 반환하며, 직원 이름, 직급, 링크를 포함합니다.")
    public @Nullable List<DepartmentMember> getDepartmentMembers(String departmentName) {
        if (toolCallNotifier != null) {
            toolCallNotifier.accept("getDepartmentMembers");
        }

        return departmentRepository.findByName(departmentName)
                .map(dept -> {
                    var employees = employeeRepository.findAllByDepartmentIdAndDeletedFalse(
                            dept.getIdOrThrow(), PageRequest.of(0, 10));
                    return employees.stream()
                            .map(emp -> new DepartmentMember(
                                    emp.getIdOrThrow(),
                                    emp.getName(),
                                    emp.getPosition().getDescription(),
                                    emp.getStatus().getDescription(),
                                    "/employees/" + emp.getIdOrThrow()))
                            .collect(Collectors.toList());
                })
                .orElse(null);
    }

    @Tool(description = "전체 부서 목록을 조회하는 도구입니다. 부서명, 부서코드, 부서유형, 링크를 반환합니다.")
    public List<SimpleDepartmentInfo> getAllDepartments() {
        if (toolCallNotifier != null) {
            toolCallNotifier.accept("getAllDepartments");
        }

        return departmentRepository.findAllByDeletedFalse().stream()
                .map(dept -> new SimpleDepartmentInfo(
                        dept.getIdOrThrow(),
                        dept.getName(),
                        dept.getCode(),
                        dept.getType().getDescription(),
                        "/departments/" + dept.getIdOrThrow()))
                .collect(Collectors.toList());
    }

    @Tool(description = "조직 전체 통계를 조회하는 도구입니다. 총 직원수, 총 부서수, 재직/휴직/퇴직 직원수를 반환합니다.")
    public OrganizationStats getOrganizationStats() {
        if (toolCallNotifier != null) {
            toolCallNotifier.accept("getOrganizationStats");
        }

        List<Department> allDepts = departmentRepository.findAllByDeletedFalse();
        List<Long> deptIds = allDepts.stream().map(Department::getIdOrThrow).toList();
        List<Employee> allEmployees = employeeRepository.findAllByDepartmentIdInAndDeletedFalse(deptIds);

        long activeCount = allEmployees.stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ACTIVE).count();
        long onLeaveCount = allEmployees.stream()
                .filter(e -> e.getStatus() == EmployeeStatus.ON_LEAVE).count();
        long resignedCount = allEmployees.stream()
                .filter(e -> e.getStatus() == EmployeeStatus.RESIGNED).count();

        return new OrganizationStats(
                allEmployees.size(),
                allDepts.size(),
                activeCount,
                onLeaveCount,
                resignedCount);
    }

    // Records
    public record DepartmentInfo(
            Long id, String name, String code, String type, String leader, String link) {

    }

    public record SubDepartmentInfo(
            Long id, String name, String code, String type, String link) {

    }

    public record DepartmentMember(
            Long id, String name, String position, String status, String link) {

    }

    public record SimpleDepartmentInfo(
            Long id, String name, String code, String type, String link) {

    }

    public record OrganizationStats(
            long totalEmployees, long totalDepartments,
            long activeEmployees, long onLeaveEmployees, long resignedEmployees) {

    }

}
