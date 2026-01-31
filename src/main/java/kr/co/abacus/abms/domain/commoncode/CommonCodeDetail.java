package kr.co.abacus.abms.domain.commoncode;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "common_code_detail")
public class CommonCodeDetail extends BaseEntity {

    @EmbeddedId
    private CommonCodeDetailId id;

    @MapsId("groupCode")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_code")
    private CommonCodeGroup group;

    @Column(nullable = false)
    private String name;

    private int sortOrder;

    @Builder
    private CommonCodeDetail(CommonCodeDetailId id, String name, int sortOrder) {
        this.id = id;
        this.name = name;
        this.sortOrder = sortOrder;
    }

    protected void setGroup(CommonCodeGroup group) {
        this.group = group;
        if (this.id != null) {
            this.id.updateGroupCode(group.getGroupCode());
        }
    }
}
