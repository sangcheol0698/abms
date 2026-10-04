package kr.co.abacus.abms.access;

import java.util.Arrays;
import java.util.Optional;

/**
 * 시스템이 인식하는 권한 코드. {@code tb_permission.code}와 1:1로 대응한다.
 */
public enum PermissionCode {

    DASHBOARD_READ("dashboard.read", "대시보드/손익 조회"),
    EMPLOYEE_READ("employee.read", "직원 상세 조회"),
    EMPLOYEE_WRITE("employee.write", "직원 생성 및 변경"),
    EMPLOYEE_EXPORT("employee.excel.download", "직원 목록 내보내기"),
    DEPARTMENT_WRITE("department.write", "부서 관리"),
    PARTY_READ("party.read", "협력사 조회"),
    PARTY_WRITE("party.write", "협력사 관리"),
    PROJECT_READ("project.read", "프로젝트 조회"),
    PROJECT_WRITE("project.write", "프로젝트 관리"),
    PROJECT_EXPORT("project.excel.download", "프로젝트 목록 내보내기"),
    SUMMARY_MANAGE("summary.manage", "손익 집계 관리"),
    REPORT_READ("report.read", "주간 보고서"),
    ACCOUNT_MANAGE("account.manage", "계정 관리"),
    PERMISSION_GROUP_MANAGE("permission.group.manage", "권한 그룹 관리");

    private final String code;
    private final String label;

    PermissionCode(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /** 화면 표시명 (tb_permission.name 과 같다) */
    public String label() {
        return label;
    }

    public String code() {
        return code;
    }

    public static Optional<PermissionCode> fromCode(String code) {
        return Arrays.stream(values()).filter(p -> p.code.equals(code)).findFirst();
    }

}
