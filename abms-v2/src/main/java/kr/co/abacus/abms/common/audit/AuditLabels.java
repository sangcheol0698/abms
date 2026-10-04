package kr.co.abacus.abms.common.audit;

import java.util.Map;

/**
 * 이력 화면의 속성 표시명. 엔티티별로 뜻이 다른 속성은 "엔티티.속성" 으로 덮어쓴다.
 */
final class AuditLabels {

    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("partyId", "협력사"), Map.entry("leadDepartmentId", "주관 부서"), Map.entry("code", "코드"),
            Map.entry("name", "이름"), Map.entry("description", "설명"), Map.entry("status", "상태"),
            Map.entry("contractAmount", "계약금액"), Map.entry("period", "기간"), Map.entry("projectId", "프로젝트"),
            Map.entry("sequence", "회차"), Map.entry("revenueDate", "매출일"), Map.entry("type", "유형"),
            Map.entry("amount", "금액"), Map.entry("issued", "발행 여부"), Map.entry("memo", "메모"),
            Map.entry("employeeId", "직원"), Map.entry("role", "역할"), Map.entry("departmentId", "부서"),
            Map.entry("email", "이메일"), Map.entry("phone", "연락처"), Map.entry("joinDate", "입사일"),
            Map.entry("careerStartDate", "경력 시작일"), Map.entry("birthDate", "생년월일"), Map.entry("position", "직급"),
            Map.entry("grade", "등급"), Map.entry("job", "직무"), Map.entry("skills", "보유 기술"),
            Map.entry("workType", "근무 형태"), Map.entry("avatar", "아바타"), Map.entry("resignationDate", "퇴사일"),
            Map.entry("annualSalary", "연봉"), Map.entry("partyType", "구분"), Map.entry("businessNumber", "사업자등록번호"),
            Map.entry("industry", "업종"), Map.entry("location", "주소"), Map.entry("website", "웹사이트"),
            Map.entry("ceoName", "대표자"), Map.entry("salesRepName", "영업 담당자"), Map.entry("salesRepPhone", "영업 담당자 연락처"),
            Map.entry("salesRepEmail", "영업 담당자 이메일"), Map.entry("parentId", "상위 부서"), Map.entry("leaderEmployeeId", "부서장"),
            Map.entry("siteId", "사업장"), Map.entry("siteType", "유형"), Map.entry("applyYear", "적용 연도"),
            Map.entry("overheadRate", "제경비율"), Map.entry("sgaRate", "판관비율"), Map.entry("groupType", "그룹 유형"),
            Map.entry("accountId", "계정"), Map.entry("permissionGroupId", "권한 그룹"), Map.entry("username", "아이디"),
            Map.entry("enabled", "사용 여부"), Map.entry("grants", "권한 구성"), Map.entry("permissionId", "권한"),
            Map.entry("scope", "범위"), Map.entry("workPlace", "수행 장소"), Map.entry("title", "부서·직책"), Map.entry("primary", "대표 담당자"), Map.entry("workLocation", "수행 장소 주소"), Map.entry("Department.description", "소개"), Map.entry("Party.phone", "대표번호"),
            Map.entry("Site.phone", "대표번호"), Map.entry("Party.name", "협력사명"), Map.entry("Project.name", "프로젝트명"),
            Map.entry("Department.name", "부서명"), Map.entry("Site.name", "사업장명"), Map.entry("PermissionGroup.name", "그룹명"));

    private AuditLabels() {
    }

    static String of(String entityType, String field) {
        return LABELS.getOrDefault(entityType + "." + field, LABELS.getOrDefault(field, field));
    }

}
