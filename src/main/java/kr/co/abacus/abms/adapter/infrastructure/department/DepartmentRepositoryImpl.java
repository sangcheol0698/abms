package kr.co.abacus.abms.adapter.infrastructure.department;

import static kr.co.abacus.abms.domain.department.QDepartment.*;
import static kr.co.abacus.abms.domain.employee.QEmployee.*;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.dto.DepartmentDetail;
import kr.co.abacus.abms.application.department.dto.DepartmentProjection;
import kr.co.abacus.abms.application.department.outbound.CustomDepartmentRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.QEmployee;

@RequiredArgsConstructor
@Repository
public class DepartmentRepositoryImpl implements CustomDepartmentRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Department> findByName(String name) {
        Department result = queryFactory
                .selectFrom(department)
                .where(
                        department.name.eq(name),
                        department.deleted.isFalse())
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<DepartmentProjection> findAllDepartmentProjections() {
        QEmployee subEmployee = new QEmployee("subEmployee");

        return queryFactory
                .select(Projections.constructor(DepartmentProjection.class,
                        department.id,
                        department.parent.id,
                        department.name,
                        department.code,
                        department.type,
                        employee.id,
                        employee.name,
                        employee.position,
                        JPAExpressions.select(subEmployee.count().intValue())
                                .from(subEmployee)
                                .where(
                                        subEmployee.departmentId.eq(department.id),
                                        subEmployee.deleted.isFalse())))
                .from(department)
                .leftJoin(employee).on(department.leaderEmployeeId.eq(employee.id))
                .where(department.deleted.isFalse())
                .fetch();
    }

    @Override
    public Optional<DepartmentDetail> findDetail(Long departmentId) {
        QEmployee subEmployee = new QEmployee("subEmployee");

        DepartmentDetail detail = queryFactory
                .select(Projections.constructor(DepartmentDetail.class,
                        department.id,
                        department.code,
                        department.name,
                        department.type,
                        employee.id,
                        employee.name,
                        department.parent.id,
                        department.parent.name,
                        JPAExpressions.select(subEmployee.count().intValue())
                                .from(subEmployee)
                                .where(
                                        subEmployee.departmentId.eq(department.id),
                                        subEmployee.deleted.isFalse())))
                .from(department)
                .leftJoin(employee).on(department.leaderEmployeeId.eq(employee.id), employee.deleted.isFalse())
                .leftJoin(department.parent)
                .where(
                        department.id.eq(departmentId),
                        department.deleted.isFalse())
                .fetchOne();

        return Optional.ofNullable(detail);
    }

}
