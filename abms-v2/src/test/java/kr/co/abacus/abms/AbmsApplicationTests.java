package kr.co.abacus.abms;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class AbmsApplicationTests {

    @Test
    void contextLoads() {
        // Flyway 마이그레이션 + Hibernate 스키마 검증(ddl-auto=validate)이 통과하면 성공
    }

}
