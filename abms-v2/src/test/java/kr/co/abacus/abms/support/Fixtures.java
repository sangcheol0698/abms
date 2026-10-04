package kr.co.abacus.abms.support;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.department.DepartmentType;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeGrade;
import kr.co.abacus.abms.employee.EmployeePosition;
import kr.co.abacus.abms.employee.EmployeeProfile;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.employee.EmployeeType;
import kr.co.abacus.abms.employee.Payroll;
import kr.co.abacus.abms.employee.PayrollRepository;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.party.PartyRepository;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectAssignment;
import kr.co.abacus.abms.project.ProjectAssignmentRepository;
import kr.co.abacus.abms.project.ProjectRepository;
import kr.co.abacus.abms.project.ProjectRevenuePlan;
import kr.co.abacus.abms.project.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.project.ProjectStatus;
import kr.co.abacus.abms.project.RevenueType;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 통합 테스트용 데이터 생성 도우미.
 */
@Component
public class Fixtures {

    private static final AtomicInteger SEQ = new AtomicInteger();

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;
    private final PartyRepository partyRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRevenuePlanRepository revenuePlanRepository;
    private final ProjectAssignmentRepository assignmentRepository;

    public Fixtures(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository,
                    PayrollRepository payrollRepository, PartyRepository partyRepository, ProjectRepository projectRepository,
                    ProjectRevenuePlanRepository revenuePlanRepository, ProjectAssignmentRepository assignmentRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.payrollRepository = payrollRepository;
        this.partyRepository = partyRepository;
        this.projectRepository = projectRepository;
        this.revenuePlanRepository = revenuePlanRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public Department department(String name, @Nullable Department parent) {
        return departmentRepository.save(Department.create("D" + SEQ.incrementAndGet(), name, DepartmentType.TEAM,
                parent == null ? null : parent.id()));
    }

    public Employee employee(Department department, String name) {
        return employee(department, name, EmployeeType.FULL_TIME);
    }

    public Employee employee(Department department, String name, EmployeeType type) {
        return employee(department, name, type, LocalDate.of(2020, 1, 1));
    }

    public Employee employee(Department department, String name, EmployeeType type, LocalDate joinDate) {
        return employeeRepository.save(Employee.create(new EmployeeProfile(department.id(), name,
                "e" + SEQ.incrementAndGet() + "@test.co", joinDate, LocalDate.of(1990, 1, 1),
                EmployeePosition.SENIOR_ASSOCIATE, type, EmployeeGrade.MID_LEVEL, null)));
    }

    public Payroll payroll(Employee employee, long annualSalary, LocalDate startDate) {
        return payrollRepository.save(Payroll.start(employee.id(), Money.wons(annualSalary), startDate));
    }

    public Party party(String name) {
        return partyRepository.save(Party.create(new Party.PartyInfo(name)));
    }

    public Project project(Department leadDepartment, long contractAmount, LocalDate start, LocalDate end) {
        Party party = party("협력사" + SEQ.incrementAndGet());
        return projectRepository.save(Project.create("P-" + SEQ.incrementAndGet(), new Project.ProjectInfo(party.id(),
                leadDepartment.id(), "프로젝트" + SEQ.get(), null, ProjectStatus.IN_PROGRESS, Money.wons(contractAmount),
                new Period(start, end))));
    }

    public ProjectRevenuePlan revenue(Project project, int sequence, LocalDate date, long amount, boolean issued) {
        ProjectRevenuePlan plan = ProjectRevenuePlan.create(project.id(),
                new ProjectRevenuePlan.RevenuePlanInfo(sequence, date, RevenueType.INTERMEDIATE_PAYMENT, Money.wons(amount), null));
        if (issued) {
            plan.issue();
        }
        return revenuePlanRepository.save(plan);
    }

    public ProjectAssignment assign(Project project, Employee employee, LocalDate start, @Nullable LocalDate end) {
        return assignmentRepository.save(ProjectAssignment.assign(project, employee, null, new Period(start, end)));
    }

    public static LoginUser user(Employee employee, Map<PermissionCode, Set<PermissionScope>> grants) {
        return new LoginUser(900_000L + employee.id(), employee.id(), employee.getDepartmentId(), employee.getEmail(),
                employee.getName(), employee.photoUrl(), "{noop}x", true, false, grants);
    }

    public static LoginUser admin(Employee employee) {
        Map<PermissionCode, Set<PermissionScope>> grants = new EnumMap<>(PermissionCode.class);
        for (PermissionCode code : PermissionCode.values()) {
            grants.put(code, EnumSet.of(PermissionScope.ALL));
        }
        return user(employee, grants);
    }

    public static Map<PermissionCode, Set<PermissionScope>> grants(PermissionScope scope, PermissionCode... codes) {
        Map<PermissionCode, Set<PermissionScope>> grants = new EnumMap<>(PermissionCode.class);
        for (PermissionCode code : codes) {
            grants.put(code, EnumSet.of(scope));
        }
        return grants;
    }

}
