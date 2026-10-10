package kr.co.abacus.abms.web.employee;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.project.ProjectAssignment;

/**
 * 직원 상세 화면의 요약 지표. 투입 이력과 경력에서 계산한다.
 *
 * @param activeAssignments  기준일에 투입 중인 프로젝트 수
 * @param currentMonthMm     이번 달 투입 M/M 합계 (1.0 = 한 달 전부 투입)
 * @param yearMm             올해 누적 투입 M/M
 * @param monthlyMm          올해 1~12월 월별 투입 M/M
 * @param careerMonths       총 경력 개월 (경력 시작일 기준)
 * @param tenureMonths       근속 개월 (입사일 기준)
 * @param upcomingAssignments 기준일 이후 시작하는 투입 예정 수
 */
public record EmployeeInsight(
        int activeAssignments,
        BigDecimal currentMonthMm,
        BigDecimal yearMm,
        List<BigDecimal> monthlyMm,
        long careerMonths,
        long tenureMonths,
        int upcomingAssignments
) {

    public static EmployeeInsight of(Employee employee, List<ProjectAssignment> assignments, LocalDate today) {
        YearMonth thisMonth = YearMonth.from(today);
        List<BigDecimal> monthly = new ArrayList<>();
        BigDecimal year = BigDecimal.ZERO;
        for (int m = 1; m <= 12; m++) {
            YearMonth month = YearMonth.of(today.getYear(), m);
            BigDecimal sum = assignments.stream().map(a -> a.manMonth(month)).reduce(BigDecimal.ZERO, BigDecimal::add);
            monthly.add(sum);
            year = year.add(sum);
        }
        BigDecimal current = assignments.stream().map(a -> a.manMonth(thisMonth)).reduce(BigDecimal.ZERO, BigDecimal::add);
        int active = (int) assignments.stream().filter(a -> a.isActiveOn(today)).count();
        int upcoming = (int) assignments.stream().filter(a -> a.getPeriod().startDate().isAfter(today)).count();
        return new EmployeeInsight(active, current, year, List.copyOf(monthly),
                employee.careerMonths(today), employee.tenureMonths(today), upcoming);
    }

    /** 투입 가능 여부: 이번 달 투입이 1.0 M/M 미만이면 가용 인력으로 본다. */
    public boolean available() {
        return currentMonthMm.compareTo(BigDecimal.ONE) < 0;
    }

    /** 월별 막대 높이 비율 (0~100, 1.0 M/M = 100) */
    public int barPercent(int monthIndex) {
        BigDecimal value = monthlyMm.get(monthIndex);
        return Math.min(100, value.multiply(BigDecimal.valueOf(100)).intValue());
    }

    public static String years(long months) {
        long y = months / 12;
        long m = months % 12;
        if (y == 0) {
            return m + "개월";
        }
        return m == 0 ? y + "년" : y + "년 " + m + "개월";
    }

}
