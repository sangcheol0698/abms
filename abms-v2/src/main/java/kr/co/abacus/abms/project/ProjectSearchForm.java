package kr.co.abacus.abms.project;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.jspecify.annotations.Nullable;

/**
 * 프로젝트 목록 검색 파라미터.
 */
public record ProjectSearchForm(
        @Nullable String q,
        @Nullable ProjectStatus status,
        @Nullable Long partyId,
        @Nullable Long leadDepartmentId
) {

    public ProjectSearch toSearch() {
        return new ProjectSearch(q, status, partyId, leadDepartmentId, null);
    }

    public String exportQuery() {
        StringBuilder sb = new StringBuilder();
        append(sb, "q", q);
        append(sb, "status", status);
        append(sb, "partyId", partyId);
        append(sb, "leadDepartmentId", leadDepartmentId);
        return sb.toString();
    }

    private static void append(StringBuilder sb, String key, @Nullable Object value) {
        if (value == null || value.toString().isBlank()) {
            return;
        }
        sb.append(sb.isEmpty() ? "?" : "&").append(key).append('=').append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));
    }

}
