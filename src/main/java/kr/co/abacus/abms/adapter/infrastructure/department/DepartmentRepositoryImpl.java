package kr.co.abacus.abms.adapter.infrastructure.department;

import static kr.co.abacus.abms.domain.department.QDepartment.*;
import static kr.co.abacus.abms.domain.employee.QEmployee.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.dto.DepartmentLeaderInfo;
import kr.co.abacus.abms.application.department.dto.DepartmentProjection;
import kr.co.abacus.abms.application.department.dto.OrganizationChartInfo;
import kr.co.abacus.abms.application.department.outbound.CustomDepartmentRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.QEmployee;

@RequiredArgsConstructor
@Repository
public class DepartmentRepositoryImpl implements CustomDepartmentRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<UUID, Long> getDepartmentHeadcounts() {
        List<Tuple> results = queryFactory
            .select(department.id, employee.count())
            .from(department)
                .leftJoin(employee).on(employee.departmentId.eq(department.id))
            .where(
                department.deleted.isFalse(),
                employee.deleted.isFalse()
            )
            .groupBy(department.id)
            .fetch();

        return results.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(0, UUID.class),
                tuple -> tuple.get(1, Long.class)
            ));
    }

    @Override
    public Optional<Department> findByName(String name) {
        Department result = queryFactory
            .selectFrom(department)
            .where(
                department.name.eq(name),
                department.deleted.isFalse()
            )
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<OrganizationChartInfo> getOrganizationChart() {
        return queryFactory
            .select(Projections.constructor(
                OrganizationChartInfo.class,
                department.id,
                department.name,
                department.code,
                department.type,
                employee.id
            ))
            .from(department)
            .join(department.parent, department).fetchJoin()
            .join(employee).on(employee.id.eq(department.leaderEmployeeId))
            .where(department.deleted.isFalse())
            .fetch();
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
                Projections.constructor(DepartmentLeaderInfo.class,
                    employee.id,
                    employee.name,
                    employee.position,
                    employee.avatar
                ),
                JPAExpressions.select(subEmployee.count().intValue())
                    .from(subEmployee)
                    .where(
                        subEmployee.departmentId.eq(department.id),
                        subEmployee.deleted.isFalse()
                    )
            ))
            .from(department)
            .leftJoin(employee).on(department.leaderEmployeeId.eq(employee.id))
            .where(department.deleted.isFalse())
            .fetch();
    }

}
