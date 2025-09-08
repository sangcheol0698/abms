package kr.co.abacus.abms.support;

import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentFixture;
import kr.co.abacus.abms.domain.department.DepartmentType;

/**
 * 통합 테스트를 위한 베이스 클래스
 *
 * 공통 설정:
 * - Spring Boot 테스트 컨텍스트
 * - 트랜잭션 관리
 * - 테스트용 부서 데이터 자동 생성
 * - 공통 유틸리티 메서드 제공
 */
@Transactional
@SpringBootTest
public abstract class IntegrationTestBase {

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected DepartmentRepository departmentRepository;

    // 테스트용 부서 데이터
    protected Department testCompany;
    protected Department testDivision;
    protected Department testTeam;

    @BeforeEach
    void setupTestData() {
        setupTestDepartments();
    }

    /**
     * 테스트용 부서 데이터를 생성하고 저장합니다.
     */
    private void setupTestDepartments() {
        // 회사 (루트) 생성
        testCompany = DepartmentFixture.createTestCompany();
        departmentRepository.save(testCompany);
        flush();

        // 본부 생성 (저장된 회사를 상위 부서로 사용)
        testDivision = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트본부", "TEST_DIV", DepartmentType.DIVISION),
            testCompany
        );
        departmentRepository.save(testDivision);
        flush();

        // 팀 생성 (저장된 본부를 상위 부서로 사용)
        testTeam = Department.create(
            DepartmentFixture.createDepartmentCreateRequest("테스트팀", "TEST_TEAM", DepartmentType.TEAM),
            testDivision
        );
        departmentRepository.save(testTeam);

        flushAndClear();
    }

    /**
     * 기본 테스트 부서 ID를 반환합니다.
     * Employee 생성 시 사용할 수 있습니다.
     */
    protected UUID getDefaultDepartmentId() {
        return testCompany.getId();
    }

    /**
     * 테스트용 본부 ID를 반환합니다.
     */
    protected UUID getTestDivisionId() {
        return testDivision.getId();
    }

    /**
     * 테스트용 팀 ID를 반환합니다.
     */
    protected UUID getTestTeamId() {
        return testTeam.getId();
    }

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