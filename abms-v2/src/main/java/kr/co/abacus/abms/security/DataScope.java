package kr.co.abacus.abms.security;

import java.util.Set;

/**
 * 특정 권한으로 접근 가능한 데이터 범위. 여러 범위가 부여되면 합집합이 된다.
 *
 * @param all           전체 접근
 * @param departmentIds 접근 가능한 부서 (직원의 소속 부서, 프로젝트의 주관 부서 기준)
 * @param employeeIds   개별 접근 가능한 직원 (SELF)
 * @param projectIds    개별 접근 가능한 프로젝트 (CURRENT_PARTICIPATION)
 */
public record DataScope(boolean all, Set<Long> departmentIds, Set<Long> employeeIds, Set<Long> projectIds) {

    public static final DataScope NONE = new DataScope(false, Set.of(), Set.of(), Set.of());
    public static final DataScope ALL = new DataScope(true, Set.of(), Set.of(), Set.of());

    public boolean isNone() {
        return !all && departmentIds.isEmpty() && employeeIds.isEmpty() && projectIds.isEmpty();
    }

    public boolean coversEmployee(Long employeeId, Long departmentId) {
        return all || departmentIds.contains(departmentId) || employeeIds.contains(employeeId);
    }

    /** SELF 범위를 제외하고 부서/전체 범위로만 접근 가능한지 (직원 정보 전체 수정 여부 판단) */
    public boolean coversDepartment(Long departmentId) {
        return all || departmentIds.contains(departmentId);
    }

    public boolean coversProject(Long projectId, Long leadDepartmentId) {
        return all || departmentIds.contains(leadDepartmentId) || projectIds.contains(projectId);
    }

}
