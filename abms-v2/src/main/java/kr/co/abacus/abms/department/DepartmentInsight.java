package kr.co.abacus.abms.department;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeJob;
import kr.co.abacus.abms.employee.EmployeePosition;
import kr.co.abacus.abms.employee.EmployeeStatus;
import kr.co.abacus.abms.employee.EmployeeType;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectStatus;

/**
 * 부서 상세 화면의 인원·운영 요약 (하위 부서 포함).
 *
 * @param headcount          하위 포함 재직·휴직 인원
 * @param activeCount        하위 포함 재직 인원
 * @param assignedCount      재직 인원 중 기준일에 프로젝트에 투입 중인 인원
 * @param positions          직급별 인원
 * @param types              고용유형별 인원
 * @param jobs               직무별 인원 (미지정 제외)
 * @param averageTenureMonths 평균 근속 개월
 * @param inProgressProjects 진행 중 주관 프로젝트 수
 * @param children           바로 아래 하위 부서와 하위 포함 인원
 */
public record DepartmentInsight(
        int headcount,
        int activeCount,
        int assignedCount,
        Map<EmployeePosition, Integer> positions,
        Map<EmployeeType, Integer> types,
        Map<EmployeeJob, Integer> jobs,
        long averageTenureMonths,
        int inProgressProjects,
        List<ChildDepartment> children
) {

    public static DepartmentInsight of(List<Employee> subtreeMembers, Set<Long> assignedEmployeeIds, List<Project> projects,
                                       List<ChildDepartment> children, LocalDate today) {
        List<Employee> current = subtreeMembers.stream().filter(e -> e.getStatus() != EmployeeStatus.RESIGNED).toList();
        List<Employee> active = current.stream().filter(e -> e.getStatus() == EmployeeStatus.ACTIVE).toList();
        Map<EmployeePosition, Integer> positions = new EnumMap<>(EmployeePosition.class);
        Map<EmployeeType, Integer> types = new EnumMap<>(EmployeeType.class);
        Map<EmployeeJob, Integer> jobs = new EnumMap<>(EmployeeJob.class);
        for (Employee e : current) {
            positions.merge(e.getPosition(), 1, Integer::sum);
            types.merge(e.getType(), 1, Integer::sum);
            if (e.getJob() != null) {
                jobs.merge(e.getJob(), 1, Integer::sum);
            }
        }
        long avgTenure = current.isEmpty() ? 0 : Math.round(current.stream().mapToLong(e -> e.tenureMonths(today)).average().orElse(0));
        return new DepartmentInsight(current.size(), active.size(),
                (int) active.stream().filter(e -> assignedEmployeeIds.contains(e.id())).count(),
                positions, types, jobs, avgTenure,
                (int) projects.stream().filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS).count(),
                List.copyOf(children));
    }

    /** 가동률: 재직 인원 중 투입 중인 비율 (%) */
    public int utilizationPercent() {
        return activeCount == 0 ? 0 : Math.round(assignedCount * 100f / activeCount);
    }

    /** 분포 막대 비율 (인원 대비 %) */
    public int percentOf(int count) {
        return headcount == 0 ? 0 : Math.round(count * 100f / headcount);
    }

    public record ChildDepartment(Department department, int headcount) {
    }

}
