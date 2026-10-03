package kr.co.abacus.abms.common.domain;

import java.math.BigDecimal;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.jspecify.annotations.Nullable;

@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {

    @Override
    public @Nullable BigDecimal convertToDatabaseColumn(@Nullable Money money) {
        return money == null ? null : money.amount();
    }

    @Override
    public @Nullable Money convertToEntityAttribute(@Nullable BigDecimal value) {
        return value == null ? null : new Money(value);
    }

}
