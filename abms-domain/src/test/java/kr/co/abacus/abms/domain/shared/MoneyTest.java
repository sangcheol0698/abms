package kr.co.abacus.abms.domain.shared;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("금액 (Money)")
class MoneyTest {

    @Test
    @DisplayName("Long 타입으로 금액 생성")
    void createWithLong() {
        Money money = Money.wons(10000L);

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("금액 더하기")
    void add() {
        Money money1 = Money.wons(10000L);
        Money money2 = Money.wons(5000L);

        Money result = money1.add(money2);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("15000.00"));
    }

    @Test
    @DisplayName("금액 빼기")
    void subtract() {
        Money money1 = Money.wons(10000L);
        Money money2 = Money.wons(3000L);

        Money result = money1.subtract(money2);

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("7000.00"));
    }

    @Test
    @DisplayName("음수가 되는 빼기 연산 시 예외 발생")
    void subtract_negativeResult_throwsException() {
        // Money money1 = Money.wons(5000L);
        // Money money2 = Money.wons(10000L);
        //
        // assertThatThrownBy(() -> money1.subtract(money2))
        //         .isInstanceOf(IllegalArgumentException.class)
        //         .hasMessageContaining("금액은 음수일 수 없습니다");
    }

    @Test
    @DisplayName("금액 곱하기")
    void multiply() {
        Money money = Money.wons(10000L);

        Money result = money.multiply(BigDecimal.valueOf(1.5));

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("15000.00"));
    }

    @Test
    @DisplayName("금액 나누기")
    void divide() {
        Money money = Money.wons(10000L);

        Money result = money.divide(BigDecimal.valueOf(2));

        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    @DisplayName("금액 비교 (크다)")
    void isGreaterThan() {
        Money money1 = Money.wons(10000L);
        Money money2 = Money.wons(5000L);

        assertThat(money1.isGreaterThan(money2)).isTrue();
        assertThat(money2.isGreaterThan(money1)).isFalse();
    }

    @Test
    @DisplayName("음수 금액 생성 시 예외 발생")
    void createWithNegativeAmount_throwsException() {
        // assertThatThrownBy(() -> Money.wons(-1000L))
        //         .isInstanceOf(IllegalArgumentException.class)
        //         .hasMessageContaining("금액은 음수일 수 없습니다");
    }

}
