package kr.co.abacus.abms.staffing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.staffing.AvailabilityService.Candidate;
import kr.co.abacus.abms.staffing.AvailabilityService.Criteria;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class AvailabilityServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);
    private static final YearMonth FROM = YearMonth.of(2026, 11);
    private static final YearMonth TO = YearMonth.of(2027, 1);

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee busy;
    private Employee half;
    private Employee idle;

    @BeforeEach
    void setUp() {
        Department team = fixtures.department("인력팀", null);
        busy = skilled(fixtures.employee(team, "꽉참"), "Java, ZZStaff");
        half = skilled(fixtures.employee(team, "한달"), "zzstaff, Kotlin");
        idle = skilled(fixtures.employee(team, "놀이"), "ZZStaff");
        Project project = fixtures.project(team, 100_000_000, TODAY, TODAY.plusYears(1));
        fixtures.assign(project, busy, FROM.atDay(1), TO.atEndOfMonth());
        fixtures.assign(project, half, FROM.atDay(1), FROM.atEndOfMonth());
    }

    private Employee skilled(Employee employee, String skills) {
        employee.updateOwnContact(null, skills);
        return employeeRepository.save(employee);
    }

    private List<Candidate> search(String skills, boolean onlyAvailable) {
        return availabilityService.search(new Criteria(FROM, TO, null, skills, null, null, onlyAvailable), TODAY);
    }

    @Test
    void 기간_평균_투입을_합산해_가용이_많은_순으로_보여준다() {
        List<Candidate> result = search("zzstaff", false);

        assertThat(result).extracting(c -> c.employee().getName()).containsExactly("놀이", "한달", "꽉참");
        Candidate h = result.get(1);
        assertThat(h.monthly()).extracting(BigDecimal::doubleValue).containsExactly(1.0, 0.0, 0.0);
        assertThat(h.free()).isEqualByComparingTo("0.67");
        assertThat(h.projects()).hasSize(1);
        assertThat(result.get(2).free()).isEqualByComparingTo("0");
    }

    @Test
    void 기술은_모두_가진_사람만_찾고_여유_있는_사람만_볼_수_있다() {
        assertThat(search("ZZSTAFF, java", false)).extracting(c -> c.employee().getName()).containsExactly("꽉참");
        assertThat(search("zzstaff", true)).extracting(c -> c.employee().getName()).containsExactly("놀이", "한달");
    }

    @Test
    void 기간은_최대_12개월이다() {
        assertThatThrownBy(() -> availabilityService.search(new Criteria(FROM, FROM.plusMonths(12), null, null, null, null, false), TODAY))
                .isInstanceOf(BusinessException.class);
    }

}
