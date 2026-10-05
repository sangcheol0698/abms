package kr.co.abacus.abms.common.audit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionGroup;
import kr.co.abacus.abms.access.PermissionGroupRepository;
import kr.co.abacus.abms.account.Account;
import kr.co.abacus.abms.account.AccountRepository;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.party.PartyRepository;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectRepository;
import kr.co.abacus.abms.site.Site;
import kr.co.abacus.abms.site.SiteRepository;

/**
 * 변경 이력 조회. 참조 id(부서·직원·협력사 …)는 현재 이름으로 바꿔서 돌려준다. (삭제된 대상은 #id)
 */
@Service
@Transactional(readOnly = true)
public class AuditQueryService {

    /** 참조 속성 → 참조 대상 종류 */
    private static final Map<String, String> REFERENCES = Map.ofEntries(
            Map.entry("departmentId", "Department"), Map.entry("leadDepartmentId", "Department"), Map.entry("parentId", "Department"),
            Map.entry("employeeId", "Employee"), Map.entry("leaderEmployeeId", "Employee"), Map.entry("partyId", "Party"),
            Map.entry("siteId", "Site"), Map.entry("projectId", "Project"), Map.entry("permissionGroupId", "PermissionGroup"),
            Map.entry("accountId", "Account"));

    /** 내부 값이라 화면에 보이지 않는 속성 (기록 규칙을 바꾸기 전에 남은 이력 포함) */
    private static final Set<String> HIDDEN = Set.of("storedPath", "contentType", "ownerType", "ownerId", "photoPath");

    private final NamedParameterJdbcTemplate jdbc;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PartyRepository partyRepository;
    private final SiteRepository siteRepository;
    private final ProjectRepository projectRepository;
    private final PermissionGroupRepository permissionGroupRepository;
    private final AccountRepository accountRepository;

    public AuditQueryService(NamedParameterJdbcTemplate jdbc, DepartmentRepository departmentRepository, EmployeeRepository employeeRepository,
                             PartyRepository partyRepository, SiteRepository siteRepository, ProjectRepository projectRepository,
                             PermissionGroupRepository permissionGroupRepository, AccountRepository accountRepository) {
        this.jdbc = jdbc;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.partyRepository = partyRepository;
        this.siteRepository = siteRepository;
        this.projectRepository = projectRepository;
        this.permissionGroupRepository = permissionGroupRepository;
        this.accountRepository = accountRepository;
    }

    /** 대상과 그 하위 엔티티(예: 프로젝트의 매출 계획·투입 인력)의 최근 이력 */
    public List<AuditEntry> history(String entityType, Long entityId, int limit) {
        return load("""
                select * from tb_audit_log
                where (entity_type = :type and entity_id = :id) or (parent_type = :type and parent_id = :id)
                order by id desc limit :limit""", new MapSqlParameterSource().addValue("type", entityType).addValue("id", entityId)
                .addValue("limit", limit));
    }

    /** 전체 이력 (관리 화면). entityType 이 null 이면 모든 종류 */
    public List<AuditEntry> recent(@Nullable String entityType, int offset, int limit) {
        return load("select * from tb_audit_log where (:type is null or entity_type = :type) order by id desc limit :limit offset :offset",
                new MapSqlParameterSource().addValue("type", entityType).addValue("limit", limit).addValue("offset", offset));
    }

    public long count(@Nullable String entityType) {
        Long count = jdbc.queryForObject("select count(*) from tb_audit_log where (:type is null or entity_type = :type)",
                new MapSqlParameterSource().addValue("type", entityType), Long.class);
        return count == null ? 0 : count;
    }

    /** 이력이 있는 엔티티 종류: [entity_type, entity_label] */
    public Map<String, String> entityTypes() {
        Map<String, String> types = new java.util.LinkedHashMap<>();
        jdbc.query("select distinct entity_type, entity_label from tb_audit_log order by entity_label",
                rs -> {
                    types.put(rs.getString(1), rs.getString(2));
                });
        return types;
    }

    private List<AuditEntry> load(String sql, MapSqlParameterSource params) {
        List<Row> rows = jdbc.query(sql, params, (rs, i) -> Row.of(rs));
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<Long, List<RawChange>> changes = new HashMap<>();
        jdbc.query("select audit_log_id, field, before_value, after_value from tb_audit_log_change where audit_log_id in (:ids) order by id",
                new MapSqlParameterSource("ids", rows.stream().map(Row::id).toList()),
                rs -> {
                    if (!HIDDEN.contains(rs.getString(2))) {
                        changes.computeIfAbsent(rs.getLong(1), k -> new ArrayList<>())
                                .add(new RawChange(rs.getString(2), rs.getString(3), rs.getString(4)));
                    }
                });
        Names names = names(changes.values().stream().flatMap(List::stream).toList());
        return rows.stream()
                .map(row -> new AuditEntry(row.id(), row.entityType(), row.entityId(), row.entityLabel(), row.entityName(), row.action(),
                        row.actorName(), row.createdAt(), changes.getOrDefault(row.id(), List.of()).stream()
                        .map(c -> new AuditEntry.Change(c.field(), AuditLabels.of(row.entityType(), c.field()),
                                names.resolve(c.field(), c.before()), names.resolve(c.field(), c.after())))
                        .toList()))
                .toList();
    }

    private Names names(List<RawChange> changes) {
        Map<String, Set<Long>> ids = new HashMap<>();
        for (RawChange change : changes) {
            String kind = REFERENCES.get(change.field());
            if (kind != null) {
                for (String value : new String[]{change.before(), change.after()}) {
                    if (value != null && value.matches("\\d+")) {
                        ids.computeIfAbsent(kind, k -> new HashSet<>()).add(Long.valueOf(value));
                    }
                }
            }
        }
        Map<String, Map<Long, String>> names = new HashMap<>();
        ids.forEach((kind, set) -> names.put(kind, switch (kind) {
            case "Department" -> byId(departmentRepository.findAllById(set), Department::id, Department::getName);
            case "Employee" -> byId(employeeRepository.findAllById(set), Employee::id, Employee::getName);
            case "Party" -> byId(partyRepository.findAllById(set), Party::id, Party::getName);
            case "Site" -> byId(siteRepository.findAllById(set), Site::id, Site::getName);
            case "Project" -> byId(projectRepository.findAllById(set), Project::id, Project::getName);
            case "PermissionGroup" -> byId(permissionGroupRepository.findAllById(set), PermissionGroup::id, PermissionGroup::getName);
            case "Account" -> byId(accountRepository.findAllById(set), Account::id, Account::getUsername);
            default -> Map.of();
        }));
        return new Names(names);
    }

    private static <T> Map<Long, String> byId(Collection<T> items, Function<T, Long> id, Function<T, String> name) {
        return items.stream().collect(Collectors.toMap(id, name));
    }

    private record Names(Map<String, Map<Long, String>> names) {

        @Nullable String resolve(String field, @Nullable String value) {
            String kind = REFERENCES.get(field);
            if (kind == null || value == null || !value.matches("\\d+")) {
                return value;
            }
            return names.getOrDefault(kind, Map.of()).getOrDefault(Long.valueOf(value), "#" + value);
        }

    }

    private record RawChange(String field, @Nullable String before, @Nullable String after) {
    }

    private record Row(long id, String entityType, long entityId, String entityLabel, @Nullable String entityName, String action,
                       @Nullable String actorName, java.time.LocalDateTime createdAt) {

        static Row of(ResultSet rs) throws SQLException {
            return new Row(rs.getLong("id"), rs.getString("entity_type"), rs.getLong("entity_id"), rs.getString("entity_label"),
                    rs.getString("entity_name"), rs.getString("action"), rs.getString("actor_name"),
                    rs.getTimestamp("created_at").toLocalDateTime());
        }

    }

}
