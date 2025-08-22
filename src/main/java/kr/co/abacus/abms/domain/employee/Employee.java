package kr.co.abacus.abms.domain.employee;

import static java.util.Objects.*;
import static org.springframework.util.Assert.*;

import java.time.LocalDate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "employee", uniqueConstraints = {
    @UniqueConstraint(name = "UK_MEMBER_EMAIL_ADDRESS", columnNames = "email_address")
})
public class Employee extends AbstractEntity {

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
    @Column(name = "position", nullable = false)
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

    @Nullable
    @Column(name = "resignation_date")
    private LocalDate resignationDate;

    @Nullable
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    public static Employee create(EmployeeCreateRequest request) {
        Employee employee = new Employee();

        employee.name = requireNonNull(request.name());
        employee.email = new Email(requireNonNull(request.email()));
        employee.joinDate = requireNonNull(request.joinDate());
        employee.birthDate = requireNonNull(request.birthDate());
        employee.position = requireNonNull(request.position());
        employee.type = requireNonNull(request.type());
        employee.grade = requireNonNull(request.grade());
        employee.memo = request.memo();

        employee.status = EmployeeStatus.ACTIVE;

        return employee;
    }

    public void resign(LocalDate resignationDate) {
        state(status != EmployeeStatus.RESIGNED, "이미 퇴사한 직원입니다.");
        isTrue(resignationDate.isAfter(joinDate), "퇴사일은 입사일 이후여야 합니다.");

        this.resignationDate = requireNonNull(resignationDate);
        this.status = EmployeeStatus.RESIGNED;
    }

    public void takeLeave() {
        state(status == EmployeeStatus.ACTIVE, "재직 중인 직원만 휴직 처리 할 수 있습니다.");

        this.status = EmployeeStatus.ON_LEAVE;
    }

    public void updateInfo(EmployeeUpdateRequest request) {
        state(status != EmployeeStatus.RESIGNED, "퇴사한 직원은 정보를 수정할 수 없습니다.");

        this.name = requireNonNull(request.name());
        this.email = new Email(requireNonNull(request.email()));
        this.joinDate = requireNonNull(request.joinDate());
        this.birthDate = requireNonNull(request.birthDate());
        this.position = requireNonNull(request.position());
        this.type = requireNonNull(request.type());
        this.grade = requireNonNull(request.grade());
        this.memo = request.memo();
    }

    public void activate() {
        state(status != EmployeeStatus.ACTIVE, "이미 재직 중인 직원입니다.");

        this.status = EmployeeStatus.ACTIVE;
        this.resignationDate = null;
    }

}
