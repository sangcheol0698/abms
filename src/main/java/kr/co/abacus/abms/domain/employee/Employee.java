package kr.co.abacus.abms.domain.employee;

import static java.util.Objects.*;
import static org.springframework.util.Assert.*;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.jspecify.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Email;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "employee", uniqueConstraints = {@UniqueConstraint(name = "UK_EMPLOYEE_EMAIL_ADDRESS", columnNames = "email_address")})
public class Employee extends AbstractEntity {

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @Embedded
    @AttributeOverride(name = "address", column = @Column(name = "email_address", nullable = false))
    private Email email;

    @Column(name = "join_date", nullable = false)
    private LocalDate joinDate;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false, length = 20)
    private EmployeePosition position;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private EmployeeType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmployeeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false)
    private EmployeeGrade grade;

    @Enumerated(EnumType.STRING)
    @Column(name = "avatar", nullable = false, length = 40)
    private EmployeeAvatar avatar;

    @Nullable
    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Nullable
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    public static Employee create(EmployeeCreateRequest request) {
        Employee employee = new Employee();

        employee.departmentId = requireNonNull(request.departmentId());
        employee.name = requireNonNull(request.name());
        employee.email = new Email(requireNonNull(request.email()));
        employee.joinDate = requireNonNull(request.joinDate());
        employee.birthDate = requireNonNull(request.birthDate());
        employee.position = requireNonNull(request.position());
        employee.type = requireNonNull(request.type());
        employee.grade = requireNonNull(request.grade());
        employee.avatar = requireNonNull(request.avatar());
        employee.memo = request.memo();

        employee.status = EmployeeStatus.ACTIVE;

        return employee;
    }

    public void resign(LocalDate resignationDate) {
        if (status == EmployeeStatus.RESIGNED) {
            throw new InvalidEmployeeStatusException("이미 퇴사한 직원입니다.");
        }
        if (!resignationDate.isAfter(joinDate)) {
            throw new InvalidEmployeeStatusException("퇴사일은 입사일 이후여야 합니다.");
        }

        this.resignationDate = requireNonNull(resignationDate);
        this.status = EmployeeStatus.RESIGNED;
    }

    public void takeLeave() {
        if (status != EmployeeStatus.ACTIVE) {
            throw new InvalidEmployeeStatusException("재직 중인 직원만 휴직 처리 할 수 있습니다.");
        }

        this.status = EmployeeStatus.ON_LEAVE;
    }

    public void activate() {
        if (status == EmployeeStatus.ACTIVE) {
            throw new InvalidEmployeeStatusException("이미 재직 중인 직원입니다.");
        }

        this.status = EmployeeStatus.ACTIVE;
        this.resignationDate = null;
    }

    public void updateInfo(EmployeeUpdateRequest request) {
        state(status != EmployeeStatus.RESIGNED, "퇴사한 직원은 정보를 수정할 수 없습니다.");

        this.departmentId = requireNonNull(request.departmentId());
        this.name = requireNonNull(request.name());
        this.email = new Email(requireNonNull(request.email()));
        this.joinDate = requireNonNull(request.joinDate());
        this.birthDate = requireNonNull(request.birthDate());
        this.position = requireNonNull(request.position());
        this.type = requireNonNull(request.type());
        this.grade = requireNonNull(request.grade());
        this.avatar = requireNonNull(request.avatar());
        this.memo = request.memo();
    }

    public void promote(EmployeePosition newPosition) {
        if (status == EmployeeStatus.RESIGNED) {
            throw new InvalidEmployeeStatusException("퇴사한 직원은 승진할 수 없습니다.");
        }

        if (newPosition.getRank() < this.position.getRank()) {
            throw new InvalidEmployeeStatusException("현재 직급보다 낮은 직급으로 변경할 수 없습니다.");
        }

        this.position = requireNonNull(newPosition);
    }

    @Override
    public void softDelete(String deletedBy) {
        state(!isDeleted(), "이미 삭제된 직원입니다.");

        super.softDelete(deletedBy);
        this.email = new Email("deleted." + System.currentTimeMillis() + "." + email.address());
    }

    @Override
    public void restore() {
        state(isDeleted(), "삭제되지 않은 직원은 복구할 수 없습니다.");

        super.restore();

        final String masked = this.email.address();
        final String prefix = "deleted.";

        if (masked.startsWith(prefix)) {
            int secondDotIndex = masked.indexOf('.', prefix.length());
            if (secondDotIndex > 0 && secondDotIndex + 1 < masked.length()) {
                String originalEmail = masked.substring(secondDotIndex + 1);
                this.email = new Email(originalEmail);
            }
        }
    }

}
