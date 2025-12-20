package kr.co.abacus.abms.application.department.outbound;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.department.dto.DepartmentDetail;
import kr.co.abacus.abms.application.department.dto.DepartmentProjection;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.support.IntegrationTestBase;

class DepartmentRepositoryTest extends IntegrationTestBase {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void find() {
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1));

        List<DepartmentProjection> projections = departmentRepository.findAllDepartmentProjections();
        DepartmentProjection root = projections.getFirst();

        assertThat(root.departmentCode()).isEqualTo("COMP001");
    }

    @Test
    @DisplayName("부서의 기본 정보를 조회한다.")
    void findDetail() {
        // given
        Department company = createDepartment("COMP001", "ABC Corp", DepartmentType.COMPANY, null, null);
        Department division = createDepartment("DIV001", "ABC Corp", DepartmentType.DIVISION, null, company);
        Department team1 = createDepartment("TEAM001", "ABC Corp", DepartmentType.TEAM, null, division);
        departmentRepository.saveAll(List.of(company, division, team1));
        flushAndClear();

        DepartmentDetail departmentDetail = departmentRepository.findDetail(team1.getId()).orElseThrow();

        assertThat(departmentDetail.name()).isEqualTo("ABC Corp");
    }

    private Department createDepartment(String code, String name, DepartmentType type,
                                        @Nullable Long leaderEmployeeId, @Nullable Department parent) {
        return Department.create(
            code,
            name,
            type,
            leaderEmployeeId,
            parent);
    }

}