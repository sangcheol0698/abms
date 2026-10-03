package kr.co.abacus.abms.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.common.domain.Money;

class MoneyTest {

    @Test
    void 원_단위로_반올림한다() {
        assertThat(Money.wons(new BigDecimal("1000.5")).amount()).isEqualByComparingTo("1001");
        assertThat(Money.wons(new BigDecimal("1000.4")).amount()).isEqualByComparingTo("1000");
    }

    @Test
    void 사칙연산() {
        Money a = Money.wons(120_000_000);
        assertThat(a.dividedBy(BigDecimal.valueOf(12))).isEqualTo(Money.wons(10_000_000));
        assertThat(a.times(new BigDecimal("0.5"))).isEqualTo(Money.wons(60_000_000));
        assertThat(a.minus(Money.wons(200_000_000)).isNegative()).isTrue();
    }

    @Test
    void 음수_곱셈과_0_나눗셈은_허용하지_않는다() {
        Money money = Money.wons(100);
        assertThatThrownBy(() -> money.times(BigDecimal.valueOf(-1))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> money.dividedBy(BigDecimal.ZERO)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 간략_표기() {
        assertThat(Money.wons(1_234_000_000).compact()).isEqualTo("12.3억");
        assertThat(Money.wons(45_000_000).compact()).isEqualTo("4,500만");
        assertThat(Money.wons(-300_000_000).compact()).isEqualTo("-3억");
        assertThat(Money.wons(9_999).compact()).isEqualTo("9,999");
    }

    @Test
    void 스케일이_달라도_같은_금액이면_같다() {
        assertThat(Money.wons(new BigDecimal("100.00"))).isEqualTo(Money.wons(100)).hasSameHashCodeAs(Money.wons(100));
    }

}
