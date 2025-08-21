package kr.co.abacus.abms.domain.employee;

import static java.util.Objects.*;

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
    @AttributeOverride(name = "email", column = @Column(name = "email_address", nullable = false))
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

    @Column(name = "leave_date")
    private LocalDate leaveDate;

    public static Employee create(EmployeeCreateRequest request) {
        Employee employee = new Employee();

        employee.name = requireNonNull(request.name());
        employee.email = new Email(requireNonNull(request.email()));
        employee.joinDate = requireNonNull(request.joinDate());
        employee.birthDate = requireNonNull(request.birthDate());
        employee.position = requireNonNull(request.position());
        employee.type = requireNonNull(request.type());
        employee.status = requireNonNull(request.status());
        employee.grade = requireNonNull(request.grade());

        return employee;
    }

}
