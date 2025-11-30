package kr.co.abacus.abms.domain.shared;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void createWithLong() {
        Money money = Money.wons(10000L);

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    void add() {
        Money money1 = Money.wons(10000L);
        Money money2 = Money.wons(5000L);

        Money result = money1.add(money2);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("15000.00"));
    }

    @Test
    void subtract() {
        Money money1 = Money.wons(10000L);
        Money money2 = Money.wons(3000L);

        Money result = money1.subtract(money2);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("7000.00"));
    }

    @Test
    void subtract_negativeResult_throwsException() {
        Money money1 = Money.wons(5000L);
        Money money2 = Money.wons(10000L);

        assertThatThrownBy(() -> money1.subtract(money2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("금액은 음수일 수 없습니다");
    }

    @Test
    void multiply() {
        Money money = Money.wons(10000L);

        Money result = money.multiply(BigDecimal.valueOf(1.5));

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("15000.00"));
    }

    @Test
    void divide() {
        Money money = Money.wons(10000L);

        Money result = money.divide(BigDecimal.valueOf(2));

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    void isGreaterThan() {
        Money money1 = Money.wons(10000L);
        Money money2 = Money.wons(5000L);

        assertThat(money1.isGreaterThan(money2)).isTrue();
        assertThat(money2.isGreaterThan(money1)).isFalse();
    }

    @Test
    void createWithNegativeAmount_throwsException() {
        assertThatThrownBy(() -> Money.wons(-1000L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("금액은 음수일 수 없습니다");
    }
}
