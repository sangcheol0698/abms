package kr.co.abacus.abms.domain.commoncode;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class CommonCodeDetailId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String groupCode;
    private String code;

    public CommonCodeDetailId(String groupCode, String code) {
        this.groupCode = groupCode;
        this.code = code;
    }

    protected void updateGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }
}
