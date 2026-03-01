package kr.co.abacus.abms.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false, comment = "생성일자")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, comment = "수정일자")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false, comment = "생성자")
    @Nullable
    private String createdBy;

    @LastModifiedBy
    @Column(comment = "수정자")
    @Nullable
    private String updatedBy;

    @Column(nullable = false, comment = "삭제여부")
    private Boolean deleted = false;

    @Nullable
    @Column(comment = "삭제일자")
    private LocalDateTime deletedAt;

    @Nullable
    @Column(comment = "삭제자")
    private String deletedBy;

    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now(ZoneId.systemDefault());
        this.deletedBy = deletedBy;
    }

    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }

}
