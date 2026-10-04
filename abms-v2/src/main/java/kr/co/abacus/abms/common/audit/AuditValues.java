package kr.co.abacus.abms.common.audit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.Labeled;
import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;

/**
 * 이력에 저장할 값의 문자열 표현. 참조 id(부서·직원 등)는 숫자 그대로 저장하고 화면에서 이름으로 바꾼다.
 */
final class AuditValues {

    private static final int MAX_LENGTH = 500;

    private AuditValues() {
    }

    static @Nullable String format(@Nullable Object value) {
        String text = switch (value) {
            case null -> null;
            case Money money -> money.amount().stripTrailingZeros().toPlainString();
            case Period period -> period.startDate() + " ~ " + (period.endDate() == null ? "" : period.endDate());
            case Location location -> location.isEmpty() ? null : location.fullAddress();
            case Labeled labeled -> labeled.label();
            case BigDecimal decimal -> decimal.stripTrailingZeros().toPlainString();
            case Boolean bool -> bool ? "예" : "아니오";
            case LocalDate date -> date.toString();
            case LocalDateTime dateTime -> dateTime.withNano(0).toString().replace('T', ' ');
            case String string -> string.isBlank() ? null : string;
            default -> value.toString();
        };
        if (text != null && text.length() > MAX_LENGTH) {
            return text.substring(0, MAX_LENGTH - 1) + "…";
        }
        return text;
    }

}
