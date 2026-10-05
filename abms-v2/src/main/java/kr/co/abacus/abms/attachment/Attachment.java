package kr.co.abacus.abms.attachment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.audit.Auditable;
import kr.co.abacus.abms.common.audit.Auditable.AuditRef;
import kr.co.abacus.abms.common.domain.BaseEntity;

/**
 * 첨부 파일 메타데이터. 파일 본문은 {@link FileStorage} 에 storedPath 로 저장된다.
 */
@Entity
@Table(name = "tb_attachment")
@SQLRestriction("deleted = false")
public class Attachment extends BaseEntity implements Auditable {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttachmentOwner ownerType;

    @Column(nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttachmentCategory category;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false, length = 200)
    private String storedPath;

    @Column(length = 100)
    private @Nullable String contentType;

    @Column(nullable = false)
    private long size;

    protected Attachment() {
    }

    public static Attachment of(AttachmentOwner ownerType, Long ownerId, AttachmentCategory category, String originalName,
                                String storedPath, @Nullable String contentType, long size) {
        Attachment attachment = new Attachment();
        attachment.ownerType = ownerType;
        attachment.ownerId = ownerId;
        attachment.category = category;
        attachment.originalName = originalName;
        attachment.storedPath = storedPath;
        attachment.contentType = contentType;
        attachment.size = size;
        return attachment;
    }

    public AttachmentOwner getOwnerType() {
        return ownerType;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public AttachmentCategory getCategory() {
        return category;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getStoredPath() {
        return storedPath;
    }

    public @Nullable String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    /** 사람이 읽기 쉬운 크기 (예: 1.2 MB) */
    public String sizeLabel() {
        if (size < 1024) {
            return size + " B";
        }
        if (size < 1024 * 1024) {
            return String.format("%.0f KB", size / 1024.0);
        }
        return String.format("%.1f MB", size / (1024.0 * 1024));
    }

    public String extension() {
        int dot = originalName.lastIndexOf('.');
        return dot < 0 ? "" : originalName.substring(dot + 1).toLowerCase(java.util.Locale.ROOT);
    }

    /** 브라우저에서 바로 볼 수 있는 형식이면 그 MIME 형식. 업로드 때 받은 형식이 아니라 확장자로 정한다. */
    public java.util.Optional<String> previewType() {
        return java.util.Optional.ofNullable(switch (extension()) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            default -> null;
        });
    }

    public boolean isImage() {
        return previewType().map(t -> t.startsWith("image/")).orElse(false);
    }

    @Override
    public String auditLabel() {
        return "첨부 파일";
    }

    @Override
    public String auditName() {
        return originalName;
    }

    @Override
    public AuditRef auditParent() {
        return new AuditRef(ownerType.auditType(), ownerId);
    }

}
