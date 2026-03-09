package kr.co.abacus.abms.domain.commoncode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_common_code_group")
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
    public void softDelete(@Nullable Long deletedBy) {
        super.softDelete(deletedBy);
        this.details.forEach(detail -> detail.softDelete(deletedBy));
    }

    public CommonCodeDetail addDetail(String code, String name, int sortOrder) {
        validateDuplicateCode(code);
        CommonCodeDetail detail = CommonCodeDetail.create(groupCode, code, name, sortOrder);
        this.details.add(detail);
        detail.setGroup(this);
        return detail;
    }

    private void validateDuplicateCode(String code) {
        String targetCode = Objects.requireNonNull(code);
        boolean duplicated = details.stream()
                .map(CommonCodeDetail::getId)
                .map(CommonCodeDetailId::getCode)
                .anyMatch(targetCode::equals);
        if (duplicated) {
            throw new IllegalArgumentException("이미 존재하는 공통코드입니다. groupCode=" + groupCode + ", code=" + targetCode);
        }
    }

}
