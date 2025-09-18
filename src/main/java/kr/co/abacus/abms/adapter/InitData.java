package kr.co.abacus.abms.adapter;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentCreateRequest;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;

@Profile({"local", "default"})
@Component
public class InitData {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public InitData(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @PostConstruct
    public void init() {
        Employee employee = employeeRepository.save(
            Employee.create(new EmployeeCreateRequest(
                UUID.randomUUID(),  // 도메인 테스트에서는 랜덤 UUID 사용
                "test@email.com",
                "박상철",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.MANAGER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                "This is a memo for the employee."
            ))
        );

        Department root = departmentRepository.save(
            Department.createRoot(new DepartmentCreateRequest(
                "TEST_CODE1", "테스트회사", DepartmentType.COMPANY, employee.getId()
            ))
        );

        departmentRepository.save(
            Department.create(new DepartmentCreateRequest(
                "TEST_CODE2", "테스트본부", DepartmentType.DIVISION, employee.getId()), root
            )
        );
    }

}
