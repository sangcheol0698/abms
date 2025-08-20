package kr.co.abacus.abms.domain.employee;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void valid_emails_should_pass() {
        String[] valids = new String[] {
            "user@example.com",
            "user.name+tag@example.co.kr",
            "USER@EXAMPLE.COM",
            "u-nder.score+plus%percent@example-domain.co",
            "a.b.c@example.com",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@example.com" // 64자 로컬파트
        };
        for (String v : valids) {
            assertThatCode(() -> new Email(v)).doesNotThrowAnyException();
        }
    }

    @Test
    void invalid_emails_should_fail() {
        String[] invalids = new String[] {
            "plainaddress",                // @ 없음
            "user..name@example.com",      // 로컬파트 연속 점
            "user@example..com",           // 도메인 연속 점
            "user@-example.com",           // 도메인 라벨 하이픈 시작
            "user@example-.com",           // 도메인 라벨 하이픈 끝
            "user@example.c",              // TLD 1자
            "user@exa_mple.com",           // 도메인에 밑줄
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@example.com" // 65자 로컬파트
        };
        for (String v : invalids) {
            assertThrows(IllegalArgumentException.class, () -> new Email(v), v);
        }
    }
}