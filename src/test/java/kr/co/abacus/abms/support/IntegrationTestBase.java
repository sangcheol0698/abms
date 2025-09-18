package kr.co.abacus.abms.support;

import jakarta.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 테스트를 위한 베이스 클래스
 *
 * 공통 설정:
 * - Spring Boot 테스트 컨텍스트
 * - 트랜잭션 관리
 * - 공통 유틸리티 메서드 제공
 */
@ActiveProfiles({"test"})
@Transactional
@SpringBootTest
public abstract class IntegrationTestBase {

    @Autowired
    protected EntityManager entityManager;

    /**
     * EntityManager flush 및 clear를 수행합니다.
     * DB 반영 후 영속성 컨텍스트를 초기화할 때 사용합니다.
     */
    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * EntityManager flush를 수행합니다.
     * 변경사항을 DB에 반영할 때 사용합니다.
     */
    protected void flush() {
        entityManager.flush();
    }

    /**
     * EntityManager clear를 수행합니다.
     * 영속성 컨텍스트를 초기화할 때 사용합니다.
     */
    protected void clear() {
        entityManager.clear();
    }

}