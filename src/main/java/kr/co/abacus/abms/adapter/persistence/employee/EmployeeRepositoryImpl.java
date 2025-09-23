package kr.co.abacus.abms.adapter.persistence.employee;

import static kr.co.abacus.abms.domain.employee.QEmployee.*;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.*;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.employee.provided.EmployeeSearchRequest;
import kr.co.abacus.abms.application.employee.required.CustomEmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;

@RequiredArgsConstructor
@Repository
public class EmployeeRepositoryImpl implements CustomEmployeeRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Employee> search(EmployeeSearchRequest request, Pageable pageable) {
        List<Employee> content = queryFactory.select(employee)
            .from(employee)
            .where(
                containsName(request.name()),
                inPositions(request.positions()),
                inTypes(request.types()),
                inGrades(request.grades()),
                inDepartments(request.departmentIds()),
                inStatuses(request.statuses())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(employee.count())
            .from(employee)
            .where(
                containsName(request.name()),
                inPositions(request.positions()),
                inTypes(request.types()),
                inGrades(request.grades()),
                inDepartments(request.departmentIds()),
                inStatuses(request.statuses())
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private @Nullable BooleanExpression containsName(@Nullable String name) {
        return hasText(name) ? employee.name.containsIgnoreCase(name) : null;
    }

    private @Nullable BooleanExpression inPositions(@Nullable List<EmployeePosition> positions) {
        if (isEmpty(positions)) {
            return null;
        }
        return employee.position.in(positions);
    }

    private @Nullable BooleanExpression inTypes(@Nullable List<EmployeeType> types) {
        if (isEmpty(types)) {
            return null;
        }
        return employee.type.in(types);
    }

    private @Nullable BooleanExpression inGrades(@Nullable List<EmployeeGrade> grades) {
        if (isEmpty(grades)) {
            return null;
        }
        return employee.grade.in(grades);
    }

    private @Nullable BooleanExpression inDepartments(@Nullable List<UUID> departmentIds) {
        if (isEmpty(departmentIds)) {
            return null;
        }
        return employee.departmentId.in(departmentIds);
    }

    private @Nullable BooleanExpression inStatuses(List<EmployeeStatus> statuses) {
        if (isEmpty(statuses)) {
            return null;
        }
        return employee.status.in(statuses);
    }

}
