package kr.co.abacus.abms.project;

import java.time.LocalDate;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.project.Project.ProjectInfo;

/**
 * 프로젝트 생성/수정 폼.
 */
public record ProjectForm(
        @Size(max = 50, message = "50자 이하로 입력하세요.") @Nullable String code,
        @NotBlank(message = "프로젝트명을 입력하세요.") @Size(max = 100, message = "100자 이하로 입력하세요.") @Nullable String name,
        @NotNull(message = "협력사를 선택하세요.") @Nullable Long partyId,
        @NotNull(message = "주관 부서를 선택하세요.") @Nullable Long leadDepartmentId,
        @NotNull(message = "상태를 선택하세요.") @Nullable ProjectStatus status,
        @NotNull(message = "계약금액을 입력하세요.") @PositiveOrZero(message = "계약금액은 0 이상이어야 합니다.") @Nullable Long contractAmount,
        @NotNull(message = "시작일을 입력하세요.") @Nullable LocalDate startDate,
        @NotNull(message = "종료일을 입력하세요.") @Nullable LocalDate endDate,
        @Size(max = 1000, message = "1000자 이하로 입력하세요.") @Nullable String description
) {

    public static ProjectForm empty(String suggestedCode) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        return new ProjectForm(suggestedCode, null, null, null, ProjectStatus.SCHEDULED, null, start, start.plusMonths(6).minusDays(1), null);
    }

    public static ProjectForm of(Project p) {
        return new ProjectForm(p.getCode(), p.getName(), p.getPartyId(), p.getLeadDepartmentId(), p.getStatus(),
                p.getContractAmount().longValue(), p.getPeriod().startDate(), p.getPeriod().endDate(), p.getDescription());
    }

    @AssertTrue(message = "종료일은 시작일 이후여야 합니다.")
    public boolean isPeriodValid() {
        return startDate == null || endDate == null || !endDate.isBefore(startDate);
    }

    public ProjectInfo toInfo() {
        return new ProjectInfo(partyId, leadDepartmentId, name, description, status, Money.wons(contractAmount),
                new Period(startDate, endDate));
    }

}
