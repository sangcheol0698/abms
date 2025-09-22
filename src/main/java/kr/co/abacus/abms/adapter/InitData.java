package kr.co.abacus.abms.adapter;

import java.time.LocalDate;

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
        Department root = departmentRepository.save(Department.createRoot(
            new DepartmentCreateRequest("TEST_CODE3", "테스트부서", DepartmentType.DIVISION, null)
        ));

        Department division = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEST_CODE4", "테스트부서2", DepartmentType.DIVISION, null), root
        ));

        Department team = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEST_CODE5", "테스트팀", DepartmentType.TEAM, null), division
        ));

        Department subDepartment = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEST_CODE6", "테스트부서3", DepartmentType.DIVISION, null), team
        ));

        Employee employee = employeeRepository.save(
            Employee.create(new EmployeeCreateRequest(
                team.getId(),  // 도메인 테스트에서는 랜덤 UUID 사용
                "test@email.com",
                "이상진",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.MANAGER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                "This is a memo for the employee."
            ))
        );

        Employee employee2 = employeeRepository.save(
            Employee.create(new EmployeeCreateRequest(
                team.getId(),  // 도메인 테스트에서는 랜덤 UUID 사용
                "test2@email.com",
                "박상철",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.LEADER,
                EmployeeType.FREELANCER,
                EmployeeGrade.EXPERT,
                "This is a memo for the employee."
            ))
        );

        Employee employee3 = employeeRepository.save(
            Employee.create(new EmployeeCreateRequest(
                division.getId(),
                "test3@email.com",
                "오혜영",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                "This is a memo for the employee."
            ))
        );

        Employee employee4 = employeeRepository.save(
            Employee.create(new EmployeeCreateRequest(
                team.getId(),
                "test4@email.com",
                "홍길동",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.MANAGER,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                "This is a memo for the employee."
            ))
        );

        Employee employee5 = employeeRepository.save(
            Employee.create(new EmployeeCreateRequest(
                root.getId(),
                "test5@email.com",
                "안사장",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.PRESIDENT,
                EmployeeType.FULL_TIME,
                EmployeeGrade.SENIOR,
                "This is a memo for the employee."
            ))
        );

        Employee employee6 = employeeRepository.save(
            Employee.create(new EmployeeCreateRequest(
                root.getId(),
                "test6@email.com",
                "박이사",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.DIRECTOR,
                EmployeeType.FULL_TIME,
                EmployeeGrade.EXPERT,
                "This is a memo for the employee."
            ))
        );

        employeeRepository.save(employee);
        employeeRepository.save(employee2);
        employeeRepository.save(employee3);
        employeeRepository.save(employee4);
        employeeRepository.save(employee5);
    }

}
