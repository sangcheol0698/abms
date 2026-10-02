package kr.co.abacus.abms.employee;

import org.jspecify.annotations.Nullable;

/**
 * 직원 목록 검색 파라미터.
 */
public record EmployeeSearchForm(
        @Nullable String q,
        @Nullable Long departmentId,
        @Nullable EmployeeStatus status,
        @Nullable EmployeeType type,
        @Nullable EmployeePosition position,
        @Nullable Boolean deleted
) {

    public EmployeeSearch toSearch() {
        return new EmployeeSearch(q, departmentId, status, type, position, Boolean.TRUE.equals(deleted));
    }

    public String exportQuery() {
        StringBuilder sb = new StringBuilder();
        append(sb, "q", q);
        append(sb, "departmentId", departmentId);
        append(sb, "status", status);
        append(sb, "type", type);
        append(sb, "position", position);
        return sb.toString();
    }

    private static void append(StringBuilder sb, String key, @Nullable Object value) {
        if (value == null || value.toString().isBlank()) {
            return;
        }
        sb.append(sb.isEmpty() ? "?" : "&").append(key).append('=')
                .append(java.net.URLEncoder.encode(value.toString(), java.nio.charset.StandardCharsets.UTF_8));
    }

}
