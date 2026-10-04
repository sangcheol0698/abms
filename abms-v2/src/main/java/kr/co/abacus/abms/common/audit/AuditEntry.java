package kr.co.abacus.abms.common.audit;

import java.time.LocalDateTime;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * 화면에 보여줄 변경 이력 한 건. 참조 id 는 이름으로 바뀐 상태다.
 */
public record AuditEntry(
        long id,
        String entityType,
        long entityId,
        String entityLabel,
        @Nullable String entityName,
        String action,
        @Nullable String actorName,
        LocalDateTime createdAt,
        List<Change> changes
) {

    public String actionLabel() {
        return switch (action) {
            case "CREATE" -> "등록";
            case "DELETE" -> "삭제";
            case "RESTORE" -> "복구";
            default -> "수정";
        };
    }

    public String actor() {
        return actorName == null ? "시스템" : actorName;
    }

    /** 상세 화면 링크 (상세 화면이 있는 엔티티만) */
    public @Nullable String href() {
        return switch (entityType) {
            case "Project" -> "/projects/" + entityId;
            case "Employee" -> "/employees/" + entityId;
            case "Party" -> "/parties/" + entityId;
            case "Site" -> "/sites/" + entityId;
            case "Department" -> "/departments?selected=" + entityId;
            case "PermissionGroup" -> "/admin/permission-groups/" + entityId;
            default -> null;
        };
    }

    public record Change(String field, String label, @Nullable String before, @Nullable String after) {
    }

}
