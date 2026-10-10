package kr.co.abacus.abms.security;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

/**
 * 로그인 사용자를 만들 때 필요한 직원 정보 조회. 직원 리포지토리가 구현한다.
 */
public interface LoginProfileLookup {

    /** 삭제되지 않은 직원의 로그인 정보 */
    Optional<LoginProfile> findLoginProfile(Long employeeId);

    record LoginProfile(Long employeeId, Long departmentId, String name, @Nullable String photoUrl, boolean resigned) {
    }

}
