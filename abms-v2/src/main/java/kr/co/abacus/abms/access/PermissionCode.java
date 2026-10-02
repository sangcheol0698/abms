package kr.co.abacus.abms.access;

import java.util.Arrays;
import java.util.Optional;

/**
 * 시스템이 인식하는 권한 코드. {@code tb_permission.code}와 1:1로 대응한다.
 */
public enum PermissionCode {

    DASHBOARD_READ("dashboard.read"),
    EMPLOYEE_READ("employee.read"),
    EMPLOYEE_WRITE("employee.write"),
    EMPLOYEE_EXPORT("employee.excel.download"),
    DEPARTMENT_WRITE("department.write"),
    PARTY_READ("party.read"),
    PARTY_WRITE("party.write"),
    PROJECT_READ("project.read"),
    PROJECT_WRITE("project.write"),
    PROJECT_EXPORT("project.excel.download"),
    SUMMARY_MANAGE("summary.manage"),
    REPORT_READ("report.read"),
    ACCOUNT_MANAGE("account.manage"),
    PERMISSION_GROUP_MANAGE("permission.group.manage");

    private final String code;

    PermissionCode(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Optional<PermissionCode> fromCode(String code) {
        return Arrays.stream(values()).filter(p -> p.code.equals(code)).findFirst();
    }

}
