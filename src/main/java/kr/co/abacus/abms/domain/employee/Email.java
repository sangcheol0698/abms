package kr.co.abacus.abms.domain.employee;

import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record Email(@Column(name = "email_address", nullable = false) String address) {

    public static final Pattern EMAIL_PATTERN
        = Pattern.compile(
            // 로컬파트: 세그먼트(알파넘/_%+-)를 점으로 연결, 점은 구분자로만 허용
            "^(?=.{1,64}@)[A-Za-z0-9_%+\\-]+(?:\\.[A-Za-z0-9_%+\\-]+)*@"
            +
            // 도메인: 라벨(알파넘으로 시작/끝, 중간 하이픈 허용, 1~63자) 1개 이상 + 최종 TLD는 알파벳 2자 이상
            "(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+"
            + "[A-Za-z]{2,}$",
            Pattern.CASE_INSENSITIVE
        );

    public Email {
        if (!EMAIL_PATTERN.matcher(address).matches()) {
            throw new IllegalArgumentException("이메일 형식이 바르지 않습니다: " + address);
        }
    }
}