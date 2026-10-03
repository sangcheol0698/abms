package kr.co.abacus.abms.employee;

import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * 직원 Aggregate Root.
 * <p>
 * 삭제 후 복구가 가능해야 하므로 다른 엔티티와 달리 삭제 행을 자동으로 숨기지 않는다.
 * 조회 시 {@code deleted} 조건을 명시적으로 건다.
 */
@Entity
@Table(name = "tb_employee")
public class Employee extends BaseEntity {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final String DELETED_EMAIL_PREFIX = "deleted.";

    @Column(nullable = false)
    private Long departmentId;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDate joinDate;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmployeePosition position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeGrade grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EmployeeAvatar avatar;

    private @Nullable LocalDate resignationDate;

    @Column(columnDefinition = "TEXT")
    private @Nullable String memo;

    protected Employee() {
    }

    public static Employee create(EmployeeProfile profile) {
        Employee employee = new Employee();
        employee.apply(profile);
        employee.status = EmployeeStatus.ACTIVE;
        return employee;
    }

    public void update(EmployeeProfile profile) {
        requireNotResigned("퇴사한 직원은 정보를 수정할 수 없습니다.");
        apply(profile);
    }

    /** 본인 정보 수정 (SELF 권한): 이름, 생년월일, 아바타만 변경 가능 */
    public void updateOwnProfile(String name, LocalDate birthDate, EmployeeAvatar avatar) {
        requireNotResigned("퇴사한 직원은 정보를 수정할 수 없습니다.");
        this.name = requireText(name, "이름");
        this.birthDate = Objects.requireNonNull(birthDate);
        this.avatar = Objects.requireNonNull(avatar);
    }

    private void apply(EmployeeProfile p) {
        this.departmentId = Objects.requireNonNull(p.departmentId(), "부서는 필수입니다.");
        this.name = requireText(p.name(), "이름");
        this.email = normalizeEmail(p.email());
        this.joinDate = Objects.requireNonNull(p.joinDate(), "입사일은 필수입니다.");
        this.birthDate = Objects.requireNonNull(p.birthDate(), "생년월일은 필수입니다.");
        this.position = Objects.requireNonNull(p.position());
        this.type = Objects.requireNonNull(p.type());
        this.grade = Objects.requireNonNull(p.grade());
        this.avatar = Objects.requireNonNull(p.avatar());
        this.memo = p.memo() == null || p.memo().isBlank() ? null : p.memo().trim();
    }

    public void resign(LocalDate resignationDate) {
        if (status == EmployeeStatus.RESIGNED) {
            throw new BusinessException("이미 퇴사한 직원입니다.");
        }
        if (!resignationDate.isAfter(joinDate)) {
            throw new BusinessException("퇴사일은 입사일 이후여야 합니다.");
        }
        this.resignationDate = resignationDate;
        this.status = EmployeeStatus.RESIGNED;
    }

    public void takeLeave() {
        if (status != EmployeeStatus.ACTIVE) {
            throw new BusinessException("재직 중인 직원만 휴직 처리할 수 있습니다.");
        }
        this.status = EmployeeStatus.ON_LEAVE;
    }

    public void activate() {
        if (status == EmployeeStatus.ACTIVE) {
            throw new BusinessException("이미 재직 중인 직원입니다.");
        }
        this.status = EmployeeStatus.ACTIVE;
        this.resignationDate = null;
    }

    public void promote(EmployeePosition newPosition, @Nullable EmployeeGrade newGrade) {
        requireNotResigned("퇴사한 직원은 승진할 수 없습니다.");
        if (newPosition.level() < position.level()) {
            throw new BusinessException("현재 직급보다 낮은 직급으로 변경할 수 없습니다.");
        }
        EmployeeGrade targetGrade = newGrade != null ? newGrade : grade;
        if (position == newPosition && grade == targetGrade) {
            throw new BusinessException("동일한 직급과 등급으로는 승진할 수 없습니다.");
        }
        this.position = newPosition;
        this.grade = targetGrade;
    }

    /** 삭제 시 이메일을 마스킹해 같은 이메일로 신규 등록이 가능하도록 한다. */
    @Override
    public void softDelete(@Nullable Long deletedBy) {
        super.softDelete(deletedBy);
        this.email = DELETED_EMAIL_PREFIX + System.currentTimeMillis() + "." + email;
    }

    public void restore() {
        restoreDeleted();
        if (email.startsWith(DELETED_EMAIL_PREFIX)) {
            int secondDot = email.indexOf('.', DELETED_EMAIL_PREFIX.length());
            if (secondDot > 0 && secondDot + 1 < email.length()) {
                this.email = email.substring(secondDot + 1);
            }
        }
    }

    /** 복구 시 원래 이메일 (마스킹 해제 결과) */
    public String originalEmail() {
        if (isDeleted() && email.startsWith(DELETED_EMAIL_PREFIX)) {
            int secondDot = email.indexOf('.', DELETED_EMAIL_PREFIX.length());
            if (secondDot > 0) {
                return email.substring(secondDot + 1);
            }
        }
        return email;
    }

    public boolean isResigned() {
        return status == EmployeeStatus.RESIGNED;
    }

    private void requireNotResigned(String message) {
        if (status == EmployeeStatus.RESIGNED) {
            throw new BusinessException(message);
        }
    }

    private static String requireText(@Nullable String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(field + "은(는) 필수입니다.");
        }
        return value.trim();
    }

    private static String normalizeEmail(@Nullable String value) {
        String email = requireText(value, "이메일").toLowerCase();
        if (!EMAIL.matcher(email).matches()) {
            throw new BusinessException("이메일 형식이 올바르지 않습니다: " + value);
        }
        return email;
    }

    public String initial() {
        return name.isEmpty() ? "?" : name.substring(0, 1);
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public EmployeePosition getPosition() {
        return position;
    }

    public EmployeeType getType() {
        return type;
    }

    public EmployeeStatus getStatus() {
        return status;
    }

    public EmployeeGrade getGrade() {
        return grade;
    }

    public EmployeeAvatar getAvatar() {
        return avatar;
    }

    public @Nullable LocalDate getResignationDate() {
        return resignationDate;
    }

    public @Nullable String getMemo() {
        return memo;
    }

}
