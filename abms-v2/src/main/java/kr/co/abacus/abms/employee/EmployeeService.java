package kr.co.abacus.abms.employee;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.ClosedMonthGuard;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.department.DepartmentDeleting;
import kr.co.abacus.abms.department.DepartmentLeaderAssigning;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.department.DepartmentTree;
import kr.co.abacus.abms.security.AccessService;
import kr.co.abacus.abms.security.DataScope;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 직원 관리 유스케이스.
 * <p>
 * 직원 목록(디렉터리)은 로그인 사용자 누구나 볼 수 있고, 상세/변경은 권한 범위 안에서만 가능하다.
 */
@Service
@Transactional
public class EmployeeService {

    private static final Comparator<Employee> MEMBER_ORDER =
            Comparator.comparingInt((Employee e) -> e.getPosition().level()).reversed().thenComparing(Employee::getName);

    private final EmployeeRepository employeeRepository;
    private final PayrollRepository payrollRepository;
    private final PositionHistoryRepository positionHistoryRepository;
    private final DepartmentRepository departmentRepository;
    private final AccessService accessService;
    private final ClosedMonthGuard closedMonthGuard;
    private final ApplicationEventPublisher eventPublisher;

    public EmployeeService(EmployeeRepository employeeRepository, PayrollRepository payrollRepository,
                           PositionHistoryRepository positionHistoryRepository,
                           DepartmentRepository departmentRepository, AccessService accessService,
                           ClosedMonthGuard closedMonthGuard, ApplicationEventPublisher eventPublisher) {
        this.employeeRepository = employeeRepository;
        this.payrollRepository = payrollRepository;
        this.positionHistoryRepository = positionHistoryRepository;
        this.departmentRepository = departmentRepository;
        this.accessService = accessService;
        this.closedMonthGuard = closedMonthGuard;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Page<Employee> search(LoginUser user, EmployeeSearch search, Pageable pageable) {
        if (search.deleted() && !user.has(PermissionCode.EMPLOYEE_WRITE)) {
            throw new AccessDeniedException("삭제된 직원 조회 권한이 없습니다.");
        }
        Collection<Long> departmentIds = null;
        if (search.departmentId() != null) {
            departmentIds = new DepartmentTree(departmentRepository.findAll()).subtreeIds(search.departmentId());
        }
        return employeeRepository.findAll(search.toSpecification(departmentIds), pageable);
    }

    /** 범위 내 직원 전체 (CSV 내보내기용) */
    @Transactional(readOnly = true)
    public List<Employee> exportable(LoginUser user, EmployeeSearch search) {
        DataScope scope = accessService.scopeOf(user, PermissionCode.EMPLOYEE_EXPORT);
        if (scope.isNone()) {
            throw new AccessDeniedException("직원 내보내기 권한이 없습니다.");
        }
        Collection<Long> departmentIds = search.departmentId() == null ? null
                : new DepartmentTree(departmentRepository.findAll()).subtreeIds(search.departmentId());
        return employeeRepository.findAll(search.toSpecification(departmentIds)).stream()
                .filter(e -> scope.coversEmployee(e.id(), e.getDepartmentId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Employee get(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> NotFoundException.of("직원", id));
    }

    @Transactional(readOnly = true)
    public Employee getForRead(LoginUser user, Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> NotFoundException.of("직원", id));
        if (employee.isDeleted()) {
            checkFullWrite(user, employee.getDepartmentId());
        } else {
            accessService.checkEmployee(user, PermissionCode.EMPLOYEE_READ, employee);
        }
        return employee;
    }

    @Transactional(readOnly = true)
    public List<Employee> activeEmployees() {
        return employeeRepository.findAllByDeletedFalseOrderByNameAsc().stream()
                .filter(e -> !e.isResigned())
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean canRead(LoginUser user, Employee employee) {
        return accessService.canAccessEmployee(user, PermissionCode.EMPLOYEE_READ, employee);
    }

    /** 부서/전체 범위의 쓰기 권한 (SELF 제외) */
    @Transactional(readOnly = true)
    public boolean canFullWrite(LoginUser user, Long departmentId) {
        return accessService.scopeOf(user, PermissionCode.EMPLOYEE_WRITE).coversDepartment(departmentId);
    }

    @Transactional(readOnly = true)
    public boolean canWriteOwnProfile(LoginUser user, Employee employee) {
        return employee.id().equals(user.employeeId())
                && accessService.canAccessEmployee(user, PermissionCode.EMPLOYEE_WRITE, employee);
    }

    public Employee create(LoginUser user, EmployeeProfile profile, @Nullable Money annualSalary) {
        checkFullWrite(user, profile.departmentId());
        requireDepartment(profile.departmentId());
        if (employeeRepository.existsByEmail(profile.email().trim().toLowerCase())) {
            throw new BusinessException("이미 사용 중인 이메일입니다: " + profile.email());
        }
        Employee employee = employeeRepository.save(Employee.create(profile));
        positionHistoryRepository.save(PositionHistory.start(employee.id(), employee.getPosition(), employee.getGrade(), employee.getJoinDate()));
        if (annualSalary != null) {
            payrollRepository.save(Payroll.start(employee.id(), annualSalary, employee.getJoinDate()));
        }
        return employee;
    }

    public void update(LoginUser user, Long id, EmployeeProfile profile) {
        Employee employee = get(id);
        checkFullWrite(user, employee.getDepartmentId());
        checkFullWrite(user, profile.departmentId());
        requireDepartment(profile.departmentId());
        String email = profile.email().trim().toLowerCase();
        if (!email.equals(employee.getEmail()) && employeeRepository.existsByEmail(email)) {
            throw new BusinessException("이미 사용 중인 이메일입니다: " + profile.email());
        }
        checkJoinDateChangeOpen(employee.getJoinDate(), profile.joinDate());
        EmployeePosition before = employee.getPosition();
        EmployeeGrade beforeGrade = employee.getGrade();
        employee.update(profile);
        if (before != employee.getPosition() || beforeGrade != employee.getGrade()) {
            recordPositionChange(employee, LocalDate.now());
        }
    }

    public void updateOwnProfile(LoginUser user, Long id, String name, LocalDate birthDate) {
        Employee employee = get(id);
        if (!canWriteOwnProfile(user, employee) && !canFullWrite(user, employee.getDepartmentId())) {
            throw new AccessDeniedException("본인 정보만 수정할 수 있습니다.");
        }
        employee.updateOwnProfile(name, birthDate);
    }

    /** 본인 정보와 연락처·보유 기술을 함께 수정한다. */
    public void updateOwnProfile(LoginUser user, Long id, String name, LocalDate birthDate,
                                 @Nullable String phone, @Nullable String skills) {
        updateOwnProfile(user, id, name, birthDate);
        get(id).updateOwnContact(phone, skills);
    }

    public void resign(LoginUser user, Long id, LocalDate resignationDate) {
        Employee employee = getForWrite(user, id);
        checkEmploymentEndChangeOpen(resignationDate);
        employee.resign(resignationDate);
        payrollRepository.findOpen(id).ifPresent(p -> p.closeAt(resignationDate.isBefore(p.getPeriod().startDate()) ? p.getPeriod().startDate() : resignationDate));
        positionHistoryRepository.findOpen(id).ifPresent(h -> h.closeAt(resignationDate));
    }

    public void takeLeave(LoginUser user, Long id) {
        getForWrite(user, id).takeLeave();
    }

    public void activate(LoginUser user, Long id) {
        Employee employee = getForWrite(user, id);
        if (employee.getResignationDate() != null) {
            checkEmploymentEndChangeOpen(employee.getResignationDate());
        }
        employee.activate();
    }

    public void promote(LoginUser user, Long id, EmployeePosition position, @Nullable EmployeeGrade grade, LocalDate effectiveDate) {
        Employee employee = getForWrite(user, id);
        employee.promote(position, grade);
        recordPositionChange(employee, effectiveDate);
    }

    /** 삭제된 직원은 원가 집계에서 빠지므로, 투입 이력이 있는 직원은 삭제 대신 퇴사 처리해야 한다. */
    public void delete(LoginUser user, Long id) {
        Employee employee = getForWrite(user, id);
        eventPublisher.publishEvent(new EmployeeDeleting(id));
        checkEmploymentPeriodOpen(employee, "직원 삭제");
        employee.softDelete(user.accountId());
    }

    public void restore(LoginUser user, Long id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> NotFoundException.of("직원", id));
        checkFullWrite(user, employee.getDepartmentId());
        if (employeeRepository.existsByEmailAndDeletedFalse(employee.originalEmail())) {
            throw new BusinessException("같은 이메일의 직원이 이미 있어 복구할 수 없습니다: " + employee.originalEmail());
        }
        checkEmploymentPeriodOpen(employee, "직원 복구");
        employee.restore();
    }

    /** 새 연봉을 등록한다. 진행 중인 이전 연봉은 시작일 전날로 종료된다. */
    public void changeSalary(LoginUser user, Long id, Money annualSalary, LocalDate startDate) {
        Employee employee = getForWrite(user, id);
        if (startDate.isBefore(employee.getJoinDate())) {
            throw new BusinessException("연봉 적용일은 입사일 이후여야 합니다.");
        }
        closedMonthGuard.checkOpen(startDate, null, "연봉 변경");
        payrollRepository.findOpen(id).ifPresent(open -> {
            if (!startDate.isAfter(open.getPeriod().startDate())) {
                throw new BusinessException("새 연봉 적용일은 현재 연봉 적용일(" + open.getPeriod().startDate() + ") 이후여야 합니다.");
            }
            open.closeAt(startDate.minusDays(1));
        });
        payrollRepository.save(Payroll.start(id, annualSalary, startDate));
    }

    /** 부서의 소속 직원: 직위 높은 순 → 이름 */
    @Transactional(readOnly = true)
    public List<Employee> members(Long departmentId) {
        return employeeRepository.findAllByDepartmentIdAndDeletedFalse(departmentId).stream()
                .sorted(MEMBER_ORDER)
                .toList();
    }

    /** 여러 부서의 소속 직원을 한 번에 조회한다. 부서마다 members() 와 같은 순서(직위 높은 순 → 이름). */
    @Transactional(readOnly = true)
    public Map<Long, List<Employee>> membersByDepartment(Collection<Long> departmentIds) {
        Map<Long, List<Employee>> result = new HashMap<>();
        departmentIds.forEach(id -> result.put(id, new ArrayList<>()));
        if (departmentIds.isEmpty()) {
            return result;
        }
        employeeRepository.findAllByDepartmentIdInAndDeletedFalse(departmentIds).stream()
                .sorted(MEMBER_ORDER)
                .forEach(e -> result.get(e.getDepartmentId()).add(e));
        return result;
    }

    /** 소속 직원이 있는 부서는 삭제할 수 없다. 주관 프로젝트 확인보다 먼저 알린다. */
    @EventListener
    @Order(1)
    public void onDepartmentDeleting(DepartmentDeleting event) {
        if (!employeeRepository.findAllByDepartmentIdAndDeletedFalse(event.departmentId()).isEmpty()) {
            throw new BusinessException("소속 직원이 있는 부서는 삭제할 수 없습니다.");
        }
    }

    /** 퇴사한(또는 없는) 직원은 부서장으로 지정할 수 없다. */
    @EventListener
    public void onDepartmentLeaderAssigning(DepartmentLeaderAssigning event) {
        Employee leader = employeeRepository.findByIdAndDeletedFalse(event.employeeId())
                .orElseThrow(() -> NotFoundException.of("직원", event.employeeId()));
        if (leader.isResigned()) {
            throw new BusinessException("퇴사한 직원은 부서장으로 지정할 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<Payroll> payrolls(Long employeeId) {
        return payrollRepository.findAllByEmployeeIdOrderByPeriodStartDateDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public List<PositionHistory> positionHistories(Long employeeId) {
        return positionHistoryRepository.findAllByEmployeeIdOrderByPeriodStartDateDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public List<Employee> findAll(Set<Long> ids) {
        return ids.isEmpty() ? List.of() : employeeRepository.findAllByIdInAndDeletedFalse(ids);
    }

    private Employee getForWrite(LoginUser user, Long id) {
        Employee employee = get(id);
        checkFullWrite(user, employee.getDepartmentId());
        return employee;
    }

    private void checkFullWrite(LoginUser user, Long departmentId) {
        if (!canFullWrite(user, departmentId)) {
            throw new AccessDeniedException("해당 부서 직원에 대한 변경 권한이 없습니다.");
        }
    }

    private void requireDepartment(Long departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw NotFoundException.of("부서", departmentId);
        }
    }

    /** 급여가 있는 직원은 재직 기간 동안 전사 원가에 포함되므로, 그 기간에 마감된 월이 있으면 막는다. */
    private void checkEmploymentPeriodOpen(Employee employee, String subject) {
        if (!payrollRepository.findAllByEmployeeIdOrderByPeriodStartDateDesc(employee.id()).isEmpty()) {
            closedMonthGuard.checkOpen(employee.getJoinDate(), employee.getResignationDate(), subject);
        }
    }

    /** 입사일이 바뀌면 이전/이후 입사일 사이 월의 재직 여부(원가 포함 여부)가 달라진다. */
    private void checkJoinDateChangeOpen(LocalDate before, LocalDate after) {
        YearMonth beforeMonth = YearMonth.from(before);
        YearMonth afterMonth = YearMonth.from(after);
        if (beforeMonth.equals(afterMonth)) {
            return;
        }
        YearMonth earlier = beforeMonth.isBefore(afterMonth) ? beforeMonth : afterMonth;
        YearMonth later = beforeMonth.isBefore(afterMonth) ? afterMonth : beforeMonth;
        closedMonthGuard.checkOpen(earlier.atDay(1), later.minusMonths(1).atDay(1), "입사일 변경");
    }

    /** 퇴사일이 생기거나 없어지면 퇴사월 다음 달부터 원가 포함 여부가 달라진다. */
    private void checkEmploymentEndChangeOpen(LocalDate resignationDate) {
        closedMonthGuard.checkOpen(YearMonth.from(resignationDate).plusMonths(1).atDay(1), null, "퇴사 처리 변경");
    }

    private void recordPositionChange(Employee employee, LocalDate effectiveDate) {
        positionHistoryRepository.findOpen(employee.id()).ifPresent(h -> h.closeAt(effectiveDate.minusDays(1)));
        positionHistoryRepository.save(PositionHistory.start(employee.id(), employee.getPosition(), employee.getGrade(), effectiveDate));
    }

}
