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

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NaturalIdCache
@Entity
@Table(name = "employee", uniqueConstraints = {
    @UniqueConstraint(name = "UK_MEMBER_EMAIL_ADDRESS", columnNames = "email_address")
})
public class Employee extends AbstractEntity {

    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @Embedded
    @NaturalId
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

    @Column(name = "resignation_date")
    private LocalDate resignationDate;

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

        this.resignationDate = requireNonNull(resignationDate);

        this.status = EmployeeStatus.RESIGNED;
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

}
