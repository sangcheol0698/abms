package kr.co.abacus.abms.adapter.persistence.employee;

import static kr.co.abacus.abms.domain.employee.QEmployee.*;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
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
        OrderSpecifier<?>[] orderSpecifiers = resolveSort(pageable);

        List<Employee> content = createBaseQuery(request)
            .orderBy(orderSpecifiers)
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
                inStatuses(request.statuses()),
                employee.deleted.isFalse()
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Employee> search(EmployeeSearchRequest request) {
        return createBaseQuery(request)
            .orderBy(defaultSort())
            .fetch();
    }

    private JPAQuery<Employee> createBaseQuery(EmployeeSearchRequest request) {
        // 공통 필터 조건만 구성하고 정렬은 search 메서드에서 최종 결정한다.
        return queryFactory.select(employee)
            .from(employee)
            .where(
                containsName(request.name()),
                inPositions(request.positions()),
                inTypes(request.types()),
                inGrades(request.grades()),
                inDepartments(request.departmentIds()),
                inStatuses(request.statuses()),
                employee.deleted.isFalse()
            );
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

    private OrderSpecifier<?>[] resolveSort(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            OrderSpecifier<?> mappedOrder = mapOrder(order);
            orderSpecifiers.add(mappedOrder);
        }

        return orderSpecifiers.toArray(new OrderSpecifier<?>[0]);
    }

    private OrderSpecifier<?> mapOrder(Sort.Order order) {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;

        return switch (order.getProperty()) {
            case "grade" -> gradeOrder(direction);
            case "position" -> positionOrder(direction);
            case "createdAt" -> new OrderSpecifier<>(direction, employee.createdAt);
            case "name" -> new OrderSpecifier<>(direction, employee.name);
            case "joinDate" -> new OrderSpecifier<>(direction, employee.joinDate);
            case "birthDate" -> new OrderSpecifier<>(direction, employee.birthDate);
            case "status" -> new OrderSpecifier<>(direction, employee.status);
            default -> defaultSort();
        };
    }

    private OrderSpecifier<Integer> gradeOrder(Order direction) {
        NumberExpression<Integer> gradeLevel = new CaseBuilder()
            .when(employee.grade.eq(EmployeeGrade.JUNIOR)).then(EmployeeGrade.JUNIOR.getLevel())
            .when(employee.grade.eq(EmployeeGrade.MID_LEVEL)).then(EmployeeGrade.MID_LEVEL.getLevel())
            .when(employee.grade.eq(EmployeeGrade.SENIOR)).then(EmployeeGrade.SENIOR.getLevel())
            .when(employee.grade.eq(EmployeeGrade.EXPERT)).then(EmployeeGrade.EXPERT.getLevel())
            .otherwise(0);

        return new OrderSpecifier<>(direction, gradeLevel);
    }

    private OrderSpecifier<?> positionOrder(Order direction) {
        NumberExpression<Integer> positionRank = new CaseBuilder()
            .when(employee.position.eq(EmployeePosition.ASSOCIATE)).then(EmployeePosition.ASSOCIATE.getRank())
            .when(employee.position.eq(EmployeePosition.STAFF)).then(EmployeePosition.STAFF.getRank())
            .when(employee.position.eq(EmployeePosition.LEADER)).then(EmployeePosition.LEADER.getRank())
            .when(employee.position.eq(EmployeePosition.MANAGER)).then(EmployeePosition.MANAGER.getRank())
            .when(employee.position.eq(EmployeePosition.SENIOR_MANAGER)).then(EmployeePosition.SENIOR_MANAGER.getRank())
            .when(employee.position.eq(EmployeePosition.DIRECTOR)).then(EmployeePosition.DIRECTOR.getRank())
            .when(employee.position.eq(EmployeePosition.TECHNICAL_DIRECTOR)).then(EmployeePosition.TECHNICAL_DIRECTOR.getRank())
            .when(employee.position.eq(EmployeePosition.MANAGING_DIRECTOR)).then(EmployeePosition.MANAGING_DIRECTOR.getRank())
            .when(employee.position.eq(EmployeePosition.VICE_PRESIDENT)).then(EmployeePosition.VICE_PRESIDENT.getRank())
            .when(employee.position.eq(EmployeePosition.PRESIDENT)).then(EmployeePosition.PRESIDENT.getRank())
            .otherwise(0);

        return new OrderSpecifier<>(direction, positionRank);
    }

    private OrderSpecifier<?> defaultSort() {
        return employee.createdAt.desc();
    }

}
