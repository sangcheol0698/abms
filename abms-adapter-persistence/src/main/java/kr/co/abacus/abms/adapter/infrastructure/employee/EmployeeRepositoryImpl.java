package kr.co.abacus.abms.adapter.infrastructure.employee;

import static kr.co.abacus.abms.domain.department.QDepartment.*;
import static kr.co.abacus.abms.domain.employee.QEmployee.*;
import static org.springframework.util.StringUtils.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.employee.dto.EmployeeDetail;
import kr.co.abacus.abms.application.employee.dto.EmployeeOverviewSummary;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.application.employee.outbound.CustomEmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

@RequiredArgsConstructor
@Repository
public class EmployeeRepositoryImpl implements CustomEmployeeRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<EmployeeSummary> search(EmployeeSearchCondition condition, Pageable pageable) {
        OrderSpecifier<?>[] orderSpecifiers = resolveSort(pageable);

        List<EmployeeSummary> content = queryFactory
                .select(Projections.constructor(EmployeeSummary.class,
                        department.id,
                        department.name,
                        employee.id,
                        employee.name,
                        employee.email,
                        employee.joinDate,
                        employee.position,
                        employee.status,
                        employee.grade,
                        employee.type,
                        employee.avatar))
                .from(employee)
                .join(department).on(employee.departmentId.eq(department.id))
                .where(
                        containsName(condition.name()),
                        inPositions(condition.positions()),
                        inTypes(condition.types()),
                        inGrades(condition.grades()),
                        inDepartments(condition.departmentIds()),
                        inStatuses(condition.statuses()),
                        employee.deleted.isFalse())
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(employee.count())
                .from(employee)
                .join(department).on(employee.departmentId.eq(department.id))
                .where(
                        containsName(condition.name()),
                        inPositions(condition.positions()),
                        inTypes(condition.types()),
                        inGrades(condition.grades()),
                        inDepartments(condition.departmentIds()),
                        inStatuses(condition.statuses()),
                        employee.deleted.isFalse());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public EmployeeOverviewSummary summarize(EmployeeSearchCondition condition) {
        return summarize(condition, null);
    }

    @Override
    public EmployeeOverviewSummary summarize(EmployeeSearchCondition condition, @Nullable CurrentActor actor) {
        return new EmployeeOverviewSummary(
                countEmployees(condition, actor, null),
                countEmployees(condition, actor, employee.status.eq(EmployeeStatus.ACTIVE)),
                countEmployees(condition, actor, employee.status.eq(EmployeeStatus.ON_LEAVE)),
                countEmployees(condition, actor, employee.type.eq(EmployeeType.FULL_TIME)),
                countEmployees(condition, actor, employee.type.eq(EmployeeType.FREELANCER)),
                countEmployees(condition, actor, employee.type.eq(EmployeeType.OUTSOURCING)),
                countEmployees(condition, actor, employee.type.eq(EmployeeType.PART_TIME)));
    }

    @Override
    public List<Employee> search(EmployeeSearchCondition request) {
        return queryFactory.select(employee)
                .from(employee)
                .where(
                        containsName(request.name()),
                        inPositions(request.positions()),
                        inTypes(request.types()),
                        inGrades(request.grades()),
                        inDepartments(request.departmentIds()),
                        inStatuses(request.statuses()),
                        employee.deleted.isFalse())
                .orderBy(defaultSort())
                .fetch();
    }

    @Override
    public List<Employee> search(EmployeeSearchCondition request, CurrentActor actor) {
        return queryFactory.select(employee)
                .from(employee)
                .where(
                        containsName(request.name()),
                        inPositions(request.positions()),
                        inTypes(request.types()),
                        inGrades(request.grades()),
                        inDepartments(request.departmentIds()),
                        inStatuses(request.statuses()),
                        accessCondition(actor, EMPLOYEE_EXCEL_DOWNLOAD_PERMISSION_CODE),
                        employee.deleted.isFalse())
                .orderBy(defaultSort())
                .fetch();
    }

    @Override
    public @Nullable EmployeeDetail findEmployeeDetail(Long id) {
        return queryFactory
                .select(Projections.constructor(EmployeeDetail.class,
                        department.id,
                        department.name,
                        employee.id,
                        employee.name,
                        employee.email,
                        employee.joinDate,
                        employee.birthDate,
                        employee.position,
                        employee.status,
                        employee.grade,
                        employee.type,
                        employee.avatar,
                        employee.memo))
                .from(employee)
                .join(department).on(employee.departmentId.eq(department.id))
                .where(
                        employee.id.eq(id),
                        employee.deleted.isFalse())
                .fetchOne();
    }

    @Override
    public @Nullable EmployeeDetail findEmployeeDetail(Long id, CurrentActor actor) {
        return queryFactory
                .select(Projections.constructor(EmployeeDetail.class,
                        department.id,
                        department.name,
                        employee.id,
                        employee.name,
                        employee.email,
                        employee.joinDate,
                        employee.birthDate,
                        employee.position,
                        employee.status,
                        employee.grade,
                        employee.type,
                        employee.avatar,
                        employee.memo))
                .from(employee)
                .join(department).on(employee.departmentId.eq(department.id))
                .where(
                        employee.id.eq(id),
                        accessCondition(actor, EMPLOYEE_READ_PERMISSION_CODE),
                        employee.deleted.isFalse())
                .fetchOne();
    }

    private static final String EMPLOYEE_READ_PERMISSION_CODE = "employee.read";
    private static final String EMPLOYEE_EXCEL_DOWNLOAD_PERMISSION_CODE = "employee.excel.download";
    private @Nullable BooleanExpression containsName(@Nullable String name) {
        return hasText(name) ? employee.name.containsIgnoreCase(name) : null;
    }

    private @Nullable BooleanExpression inPositions(@Nullable List<EmployeePosition> positions) {
        if (ObjectUtils.isEmpty(positions)) {
            return null;
        }
        return employee.position.in(positions);
    }

    private @Nullable BooleanExpression inTypes(@Nullable List<EmployeeType> types) {
        if (ObjectUtils.isEmpty(types)) {
            return null;
        }
        return employee.type.in(types);
    }

    private @Nullable BooleanExpression inGrades(@Nullable List<EmployeeGrade> grades) {
        if (ObjectUtils.isEmpty(grades)) {
            return null;
        }
        return employee.grade.in(grades);
    }

    private @Nullable BooleanExpression inDepartments(@Nullable List<Long> departmentIds) {
        if (ObjectUtils.isEmpty(departmentIds)) {
            return null;
        }
        return employee.departmentId.in(departmentIds);
    }

    private @Nullable BooleanExpression inStatuses(@Nullable List<EmployeeStatus> statuses) {
        if (ObjectUtils.isEmpty(statuses)) {
            return null;
        }
        return employee.status.in(statuses);
    }

    private @Nullable BooleanExpression accessCondition(@Nullable CurrentActor actor, String permissionCode) {
        if (actor == null || !actor.hasPermission(permissionCode)) {
            return actor == null ? null : employee.id.isNull();
        }
        Set<PermissionScope> scopes = actor.scopesOf(permissionCode);
        if (scopes.contains(PermissionScope.ALL)) {
            return null;
        }
        LinkedHashSet<Long> departmentIds = new LinkedHashSet<>();
        if (actor.departmentId() != null && scopes.contains(PermissionScope.OWN_DEPARTMENT)) {
            departmentIds.add(actor.departmentId());
        }
        if (actor.departmentId() != null && scopes.contains(PermissionScope.OWN_DEPARTMENT_TREE)) {
            departmentIds.addAll(resolveDepartmentTree(actor.departmentId()));
        }
        BooleanExpression departmentCondition = departmentIds.isEmpty()
                ? null
                : employee.departmentId.in(departmentIds);
        BooleanExpression selfCondition = scopes.contains(PermissionScope.SELF) && actor.employeeId() != null
                ? employee.id.eq(actor.employeeId())
                : null;
        if (departmentCondition != null && selfCondition != null) {
            return departmentCondition.or(selfCondition);
        }
        if (departmentCondition != null) {
            return departmentCondition;
        }
        if (selfCondition != null) {
            return selfCondition;
        }
        return employee.id.isNull();
    }

    private LinkedHashSet<Long> resolveDepartmentTree(Long rootDepartmentId) {
        Map<Long, List<Long>> childrenByParentId = queryFactory.select(department.id, department.parent.id)
                .from(department)
                .where(department.deleted.isFalse(), department.parent.id.isNotNull())
                .fetch()
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        tuple -> tuple.get(department.parent.id),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.mapping(tuple -> tuple.get(department.id),
                                java.util.stream.Collectors.toList())
                ));
        LinkedHashSet<Long> departmentIds = new LinkedHashSet<>();
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(rootDepartmentId);
        while (!queue.isEmpty()) {
            Long departmentId = queue.poll();
            if (!departmentIds.add(departmentId)) {
                continue;
            }
            queue.addAll(childrenByParentId.getOrDefault(departmentId, List.of()));
        }
        return departmentIds;
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
                .when(employee.position.eq(EmployeePosition.ASSOCIATE)).then(EmployeePosition.ASSOCIATE.getLevel())
                .when(employee.position.eq(EmployeePosition.SENIOR_ASSOCIATE)).then(EmployeePosition.SENIOR_ASSOCIATE.getLevel())
                .when(employee.position.eq(EmployeePosition.PRINCIPAL)).then(EmployeePosition.PRINCIPAL.getLevel())
                .when(employee.position.eq(EmployeePosition.TEAM_LEADER)).then(EmployeePosition.TEAM_LEADER.getLevel())
                .when(employee.position.eq(EmployeePosition.CHIEF)).then(EmployeePosition.CHIEF.getLevel())
                .when(employee.position.eq(EmployeePosition.DIRECTOR)).then(EmployeePosition.DIRECTOR.getLevel())
                .when(employee.position.eq(EmployeePosition.MANAGING_DIRECTOR)).then(EmployeePosition.MANAGING_DIRECTOR.getLevel())
                .when(employee.position.eq(EmployeePosition.TECHNICAL_DIRECTOR)).then(EmployeePosition.TECHNICAL_DIRECTOR.getLevel())
                .when(employee.position.eq(EmployeePosition.VICE_PRESIDENT)).then(EmployeePosition.VICE_PRESIDENT.getLevel())
                .when(employee.position.eq(EmployeePosition.PRESIDENT)).then(EmployeePosition.PRESIDENT.getLevel())
                .when(employee.position.eq(EmployeePosition.CHAIRMAN)).then(EmployeePosition.CHAIRMAN.getLevel())
                .otherwise(0);

        return new OrderSpecifier<>(direction, positionRank);
    }

    private OrderSpecifier<?> defaultSort() {
        return employee.createdAt.desc();
    }

    private long countEmployees(
            EmployeeSearchCondition condition,
            @Nullable CurrentActor actor,
            @Nullable BooleanExpression extraCondition
    ) {
        Long value = queryFactory
                .select(employee.count())
                .from(employee)
                .where(
                        containsName(condition.name()),
                        inPositions(condition.positions()),
                        inTypes(condition.types()),
                        inGrades(condition.grades()),
                        inDepartments(condition.departmentIds()),
                        inStatuses(condition.statuses()),
                        accessCondition(actor, EMPLOYEE_READ_PERMISSION_CODE),
                        extraCondition,
                        employee.deleted.isFalse())
                .fetchOne();
        return value != null ? value : 0L;
    }

}
