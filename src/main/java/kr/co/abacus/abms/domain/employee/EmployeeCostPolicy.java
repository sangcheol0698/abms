package kr.co.abacus.abms.domain.employee;

import static java.util.Objects.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_employee_cost_policy")
public class EmployeeCostPolicy  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apply_year", nullable = false, comment = "적용년도")
    private Integer applyYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, comment = "직원유형")
    private EmployeeType type;

    @Column(name = "direct_overhead_rate", nullable = false, comment = "제경비율")
    private Double directOverheadRate;

    @Column(name = "general_admin_rate", nullable = false, comment = "판관비율")
    private Double generalAdminRate;

    @Builder(access = AccessLevel.PRIVATE)
    private EmployeeCostPolicy(Integer applyYear, EmployeeType type, Double directOverheadRate, Double generalAdminRate) {
        this.applyYear = requireNonNull(applyYear);
        this.type = requireNonNull(type);
        this.directOverheadRate = requireNonNull(directOverheadRate);
        this.generalAdminRate = requireNonNull(generalAdminRate);
    }

    public static EmployeeCostPolicy create(Integer applyYear, EmployeeType type, Double directOverheadRate, Double generalAdminRate) {
        return EmployeeCostPolicy.builder()
            .applyYear(applyYear)
            .type(type)
            .directOverheadRate(directOverheadRate)
            .generalAdminRate(generalAdminRate)
            .build();
    }

}
