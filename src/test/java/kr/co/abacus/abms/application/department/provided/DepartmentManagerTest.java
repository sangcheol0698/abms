package kr.co.abacus.abms.application.department.provided;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.provided.EmployeeManager;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.employee.EmployeeFixture;
import kr.co.abacus.abms.support.IntegrationTestBase;

class DepartmentManagerTest extends IntegrationTestBase {

    @Autowired
    private DepartmentManager departmentManager;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeManager employeeManager;

    private UUID departmentId;
    private UUID teamLeaderId;

    @BeforeEach
    void init() {
        Department rootDepartment = departmentRepository.save(DepartmentFixture.createRootDepartment());
        departmentId = rootDepartment.getId();

        teamLeaderId = employeeManager.create(EmployeeFixture.createEmployeeCreateRequest(
            "email@test.com",
            "김팀장",
            rootDepartment.getId()
        ));
    }

    @Test
    @DisplayName("팀의 팀장을 지정한다.")
    void assignTeamLeader() {
        Department department = departmentManager.assignTeamLeader(departmentId, teamLeaderId);

        assertThat(department.getLeaderEmployeeId()).isEqualTo(teamLeaderId);
    }

}