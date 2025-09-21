package kr.co.abacus.abms.domain.shared;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void createFailWhenNegativeAmount() {
        assertThatThrownBy(() -> Money.wons(new BigDecimal("-100.00")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("금액은 음수일 수 없습니다");
    }

    @Test
    void zero() {
        Money money = Money.zero();

        assertThat(money.amount()).isEqualByComparingTo("0");
    }

    @Test
    void add() {
        Money money1 = Money.wons(new BigDecimal("100.00"));
        Money money2 = Money.wons(new BigDecimal("50.50"));

        Money result = money1.add(money2);

        assertThat(result.amount()).isEqualByComparingTo("150.50");
    }

    @Test
    void subtract() {
        Money money1 = Money.wons(new BigDecimal("100.00"));
        Money money2 = Money.wons(new BigDecimal("50.50"));

        Money result = money1.subtract(money2);

        assertThat(result.amount()).isEqualByComparingTo("49.50");
    }

    @Test
    void multiply() {
        Money money = Money.wons(new BigDecimal("100.00"));
        BigDecimal factor = new BigDecimal("1.5");

        Money result = money.multiply(factor);

        assertThat(result.amount()).isEqualByComparingTo("150.00");
    }

    @Test
    void divide() {
        Money money = Money.wons(new BigDecimal("100.00"));
        BigDecimal divisor = new BigDecimal("4");

        Money result = money.divide(divisor);

        assertThat(result.amount()).isEqualByComparingTo("25.00");
    }

    @Test
    void divideRoundingHalfUp() {
        Money money = Money.wons(new BigDecimal("100.00"));
        BigDecimal divisor = new BigDecimal("3");

        Money result = money.divide(divisor);

        assertThat(result.amount()).isEqualByComparingTo("33.33");
    }

    @Test
    void isGreaterThan() {
        Money money1 = Money.wons(new BigDecimal("100.00"));
        Money money2 = Money.wons(new BigDecimal("50.50"));

        assertThat(money1.isGreaterThan(money2)).isTrue();
    }

}