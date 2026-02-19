package kr.co.abacus.abms.domain.commoncode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@SuppressWarnings("NullAway.Init")
public class CommonCodeDetailId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String groupCode;
    private String code;

    public CommonCodeDetailId(String groupCode, String code) {
        this.groupCode = Objects.requireNonNull(groupCode);
        this.code = Objects.requireNonNull(code);
    }

    protected void updateGroupCode(String groupCode) {
        this.groupCode = Objects.requireNonNull(groupCode);
    }

}
