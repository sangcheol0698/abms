package kr.co.abacus.abms.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

import jakarta.persistence.Embeddable;

@Embeddable
public record Money(BigDecimal amount) {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public Money(BigDecimal amount) {
        // 260216: 적자(마이너스)로 인해 음수 방지 로직 제거
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    public static Money wons(BigDecimal amount) {
        return new Money(amount);
    }

    public static Money wons(Long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO);
    }

    public Money add(Money other) {
        return Money.wons(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        // 260216: 적자(마이너스)로 인해 음수 방지 로직 제거
        return new Money(this.amount.subtract(other.amount));
    }

    public Money multiply(BigDecimal factor) {
        if (factor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("곱셈 인자는 음수일 수 없습니다: " + factor);
        }
        return Money.wons(this.amount.multiply(factor));
    }

    public Money divide(BigDecimal divisor) {
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("나눗셈 인자는 양수여야 합니다: " + divisor);
        }
        return Money.wons(this.amount.divide(divisor, SCALE, ROUNDING_MODE));
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }

}
