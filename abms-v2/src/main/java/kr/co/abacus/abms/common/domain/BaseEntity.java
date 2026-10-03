package kr.co.abacus.abms.common.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 모든 엔티티의 공통 기반. 식별자, 감사(audit) 컬럼, 소프트 삭제 상태를 가진다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private @Nullable LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private @Nullable LocalDateTime updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private @Nullable Long createdBy;

    @LastModifiedBy
    private @Nullable Long updatedBy;

    @Column(nullable = false)
    private boolean deleted = false;

    private @Nullable LocalDateTime deletedAt;

    private @Nullable Long deletedBy;

    public @Nullable Long getId() {
        return id;
    }

    public Long id() {
        return Objects.requireNonNull(id, "아직 저장되지 않은 엔티티입니다.");
    }

    public @Nullable LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public @Nullable LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public @Nullable Long getCreatedBy() {
        return createdBy;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public @Nullable LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void softDelete(@Nullable Long deletedBy) {
        if (deleted) {
            throw new BusinessException("이미 삭제된 데이터입니다.");
        }
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    protected void restoreDeleted() {
        if (!deleted) {
            throw new BusinessException("삭제되지 않은 데이터는 복구할 수 없습니다.");
        }
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || org.hibernate.Hibernate.getClass(this) != org.hibernate.Hibernate.getClass(o)) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public final int hashCode() {
        return org.hibernate.Hibernate.getClass(this).hashCode();
    }

}
