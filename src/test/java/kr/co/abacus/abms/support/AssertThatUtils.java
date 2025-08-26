package kr.co.abacus.abms.support;

import java.util.function.Consumer;

import org.assertj.core.api.AssertProvider;
import org.assertj.core.api.Assertions;
import org.springframework.test.json.JsonPathValueAssert;

public class AssertThatUtils {

    public static Consumer<AssertProvider<JsonPathValueAssert>> notNull() {
        return value -> Assertions.assertThat(value).isNotNull();
    }

    public static Consumer<AssertProvider<JsonPathValueAssert>> equalsTo(Object object) {
        return value -> Assertions.assertThat(value).isEqualTo(object);
    }

}
