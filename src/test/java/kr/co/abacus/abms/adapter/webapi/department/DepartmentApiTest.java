package kr.co.abacus.abms.adapter.webapi.department;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

class DepartmentApiTest extends ApiIntegrationTestBase {

    @Autowired
    private DepartmentRepository departmentRepository;

    private UUID companyId;

    @BeforeEach
    void setUpDepartments() {
        Department company = DepartmentFixture.createTestCompany();
        departmentRepository.save(company);
        Department division = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION),
            company
        );
        departmentRepository.save(division);
        Department team1 = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM),
            division
        );
        Department team2 = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀2", "TEST_TEAM2", DepartmentType.TEAM),
            division
        );
        departmentRepository.save(team1);
        departmentRepository.save(team2);
        flushAndClear();

        companyId = company.getId();
    }

    @Test
    void getOrganizationChart() {
        MvcTestResult result = mvcTester.get().uri("/api/departments/organization-chart").exchange();

        assertThat(result).apply(print()).hasStatusOk();
    }

}