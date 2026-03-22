package kr.co.abacus.abms.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Money(BigDecimal amount) {

    private static final int SCALE = 0;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public Money(BigDecimal amount) {
        BigDecimal normalized = Objects.requireNonNull(amount, "amount는 null일 수 없습니다")
                .setScale(SCALE, ROUNDING_MODE);
        this.amount = normalized;
    }

    public static Money wons(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money wons(Long amount) {
        BigDecimal decimalAmount = BigDecimal.valueOf(amount);
        return new Money(decimalAmount);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        BigDecimal result = this.amount.subtract(other.amount);
        // validateNonNegative(result);
        return new Money(result);
    }

    public Money multiply(BigDecimal factor) {
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("곱셈 인자는 음수일 수 없습니다: " + factor);
        }
        return new Money(this.amount.multiply(factor));
    }

    public Money divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("나눗셈 인자는 양수여야 합니다: " + divisor);
        }
        return new Money(this.amount.divide(divisor, SCALE, ROUNDING_MODE));
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

}
