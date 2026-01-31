package kr.co.abacus.abms.domain.commoncode;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "common_code_group")
public class CommonCodeGroup extends BaseEntity {

    @Id
    @Column(name = "group_code", nullable = false, length = 50)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 50)
    private String groupName;

    @Column(name = "description", length = 500)
    private String description;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommonCodeDetail> details = new ArrayList<>();

    @Builder
    private CommonCodeGroup(String groupCode, String groupName, String description) {
        this.groupCode = groupCode;
        this.groupName = groupName;
        this.description = description;
    }

    @Override
    public void softDelete(String deletedBy) {
        super.softDelete(deletedBy);
        this.details.forEach(detail -> detail.softDelete(deletedBy));
    }

    public void addDetail(CommonCodeDetail detail) {
        this.details.add(detail);
        detail.setGroup(this);
    }

}
