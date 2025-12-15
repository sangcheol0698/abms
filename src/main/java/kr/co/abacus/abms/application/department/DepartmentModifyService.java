package kr.co.abacus.abms.application.department;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.department.provided.DepartmentManager;
import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.inbound.EmployeeFinder;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;

@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DepartmentModifyService implements DepartmentManager {

    private final DepartmentRepository departmentRepository;
    private final DepartmentFinder departmentFinder;
    private final EmployeeFinder employeeFinder;

    @Override
    @Transactional
    public Department assignTeamLeader(UUID departmentId, UUID leaderEmployeeId) {
        Department department = departmentFinder.find(departmentId);
        Employee employee = employeeFinder.find(leaderEmployeeId);

        department.assignTeamLeader(employee.getId());

        departmentRepository.save(department);

        return department;
    }

}
