package kr.co.abacus.abms.domain.shared;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Nested
    @DisplayName("유효한 이메일 테스트")
    class ValidEmailTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "test@example.com",
                "user.name@domain.co.kr",
                "firstname.lastname@example.com",
                "user+tag@example.com",
                "user.name+tag@example.co.uk",
                "test123@test.org",
                "user@test.io",
                "a@b.co",
                "test_user@example.com",
                "user-name@domain.com",
                "123@example.com",
                "test&me@example.com",
                "user*name@example.com"
        })
        @DisplayName("유효한 이메일 주소로 Email 객체 생성")
        void shouldCreateEmailWithValidAddress(String validEmail) {
            // when & then
            assertThatCode(() -> new Email(validEmail))
                    .doesNotThrowAnyException();

            Email email = new Email(validEmail);
            assertThat(email.address()).isEqualTo(validEmail);
        }

    }

    @Nested
    @DisplayName("유효하지 않은 이메일 테스트")
    class InvalidEmailTest {

        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "   ",
                "invalid-email",
                "@example.com",
                "user@",
                "user@@example.com",
                "user@.com",
                "user@com",
                "user.@example.com",
                ".user@example.com",
                "user..name@example.com",
                "user name@example.com",
                "user@example..com",
                "user@example.",
                "user@example.c",
                "user@example.toolongdomain",
                "user@-example.com",
                "user@example-.com"
        })
        @DisplayName("유효하지 않은 이메일 주소로 생성 시 예외 발생")
        void shouldThrowExceptionWithInvalidAddress(String invalidEmail) {
            assertThatThrownBy(() -> new Email(invalidEmail))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("null 이메일 주소로 생성 시 예외 발생")
        void shouldThrowExceptionWithNullAddress() {
            assertThatThrownBy(() -> new Email(null))
                    .isInstanceOf(NullPointerException.class);
        }

    }

    @Nested
    @DisplayName("Email 객체 동등성 테스트")
    class EmailEqualityTest {

        @Test
        @DisplayName("같은 주소를 가진 Email 객체는 동등하다")
        void shouldBeEqualWithSameAddress() {
            String address = "test@example.com";

            Email email1 = new Email(address);
            Email email2 = new Email(address);

            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("다른 주소를 가진 Email 객체는 동등하지 않다")
        void shouldNotBeEqualWithDifferentAddress() {
            Email email1 = new Email("test1@example.com");
            Email email2 = new Email("test2@example.com");

            assertThat(email1).isNotEqualTo(email2);
        }

    }

}