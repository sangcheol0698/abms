package kr.co.abacus.abms.common.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * 원 단위 금액. 소수점은 반올림해 정수 원으로 정규화한다.
 */
public record Money(BigDecimal amount) implements Comparable<Money>, java.io.Serializable {

    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money {
        amount = Objects.requireNonNull(amount, "금액은 null일 수 없습니다.").setScale(0, ROUNDING);
    }

    public static Money wons(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public static Money wons(BigDecimal amount) {
        return new Money(amount);
    }

    public Money plus(Money other) {
        return new Money(amount.add(other.amount));
    }

    public Money minus(Money other) {
        return new Money(amount.subtract(other.amount));
    }

    public Money times(BigDecimal factor) {
        if (factor.signum() < 0) {
            throw new IllegalArgumentException("곱셈 인자는 음수일 수 없습니다: " + factor);
        }
        return new Money(amount.multiply(factor));
    }

    public Money dividedBy(BigDecimal divisor) {
        if (divisor.signum() <= 0) {
            throw new IllegalArgumentException("나눗셈 인자는 양수여야 합니다: " + divisor);
        }
        return new Money(amount.divide(divisor, 0, ROUNDING));
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public long longValue() {
        return amount.longValueExact();
    }

    /** 1,234,567 형식 */
    public String formatted() {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(amount);
    }

    /** 억/만 단위의 간략 표기 (예: 12.3억, 4,500만) */
    public String compact() {
        BigDecimal abs = amount.abs();
        String sign = amount.signum() < 0 ? "-" : "";
        BigDecimal eok = BigDecimal.valueOf(100_000_000L);
        BigDecimal man = BigDecimal.valueOf(10_000L);
        if (abs.compareTo(eok) >= 0) {
            return sign + abs.divide(eok, 1, ROUNDING).stripTrailingZeros().toPlainString() + "억";
        }
        if (abs.compareTo(man) >= 0) {
            return sign + NumberFormat.getNumberInstance(Locale.KOREA).format(abs.divide(man, 0, ROUNDING)) + "만";
        }
        return sign + NumberFormat.getNumberInstance(Locale.KOREA).format(abs);
    }

    @Override
    public int compareTo(Money o) {
        return amount.compareTo(o.amount);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money other && amount.compareTo(other.amount) == 0;
    }

    @Override
    public int hashCode() {
        return amount.stripTrailingZeros().hashCode();
    }

    @Override
    public String toString() {
        return formatted() + "원";
    }

}
