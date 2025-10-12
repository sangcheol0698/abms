package kr.co.abacus.abms.adapter.persistence.department;

import static kr.co.abacus.abms.domain.department.QDepartment.*;
import static kr.co.abacus.abms.domain.employee.QEmployee.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.required.CustomDepartmentRepository;

@RequiredArgsConstructor
@Repository
public class DepartmentRepositoryImpl implements CustomDepartmentRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Map<UUID, Long> getDepartmentHeadcounts() {
        List<Tuple> results = queryFactory
            .select(department.id, employee.count())
            .from(department)
            .leftJoin(employee).on(employee.departmentId.eq(department.id)
                    .and(employee.deleted.isFalse()))
            .where(department.deleted.isFalse())
            .groupBy(department.id)
            .fetch();

        return results.stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(0, UUID.class),
                tuple -> tuple.get(1, Long.class)
            ));
    }

}
