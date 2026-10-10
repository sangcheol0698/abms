package kr.co.abacus.abms.common.audit;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.common.audit.Auditable.AuditRef;

/**
 * {@link Auditable} 엔티티의 등록·수정·(소프트)삭제를 tb_audit_log 에 기록한다.
 * 같은 트랜잭션·같은 커넥션(JdbcTemplate)으로 쓰므로 업무 변경이 롤백되면 이력도 함께 롤백된다.
 */
@Component
public class AuditEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    /** 감사 컬럼·보안 정보·로그인 때마다 바뀌는 값은 이력에서 뺀다. */
    private static final Set<String> IGNORED = Set.of("createdAt", "updatedAt", "createdBy", "updatedBy", "deleted", "deletedAt",
            "deletedBy", "password", "passwordChangedAt", "loginFailCount", "lastLoginAt",
            // 첨부 파일: 저장 위치·형식·소유자는 내부 값이거나 상위 이력으로 드러난다.
            "storedPath", "contentType", "ownerType", "ownerId",
            // 프로필 사진은 경로 대신 별도 항목(photo)으로 기록한다.
            "photoPath");

    private final EntityManagerFactory entityManagerFactory;
    private final JdbcTemplate jdbcTemplate;

    public AuditEventListener(EntityManagerFactory entityManagerFactory, JdbcTemplate jdbcTemplate) {
        this.entityManagerFactory = entityManagerFactory;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void register() {
        EventListenerRegistry registry = entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getServiceRegistry().getService(EventListenerRegistry.class);
        registry.appendListeners(EventType.POST_INSERT, this);
        registry.appendListeners(EventType.POST_UPDATE, this);
        registry.appendListeners(EventType.POST_DELETE, this);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        if (!(event.getEntity() instanceof Auditable auditable)) {
            return;
        }
        String[] names = event.getPersister().getPropertyNames();
        Object[] state = event.getState();
        List<Change> changes = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            String after = AuditValues.format(state[i]);
            if (!IGNORED.contains(names[i]) && after != null) {
                changes.add(new Change(names[i], null, after));
            }
        }
        write(auditable, (Long) event.getId(), "CREATE", changes);
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (!(event.getEntity() instanceof Auditable auditable) || event.getOldState() == null) {
            return;
        }
        String[] names = event.getPersister().getPropertyNames();
        Object[] before = event.getOldState();
        Object[] after = event.getState();
        String action = "UPDATE";
        List<Change> changes = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            if ("deleted".equals(names[i]) && !Objects.equals(before[i], after[i])) {
                action = Boolean.TRUE.equals(after[i]) ? "DELETE" : "RESTORE";
            }
            if (IGNORED.contains(names[i])) {
                continue;
            }
            String from = AuditValues.format(before[i]);
            String to = AuditValues.format(after[i]);
            if (!Objects.equals(from, to)) {
                changes.add(new Change(names[i], from, to));
            }
        }
        if ("UPDATE".equals(action) && changes.isEmpty()) {
            return;
        }
        write(auditable, (Long) event.getId(), action, changes);
    }

    /** 하드 삭제(권한 그룹 멤버 해제, 원가 정책 삭제 등): 삭제 직전 값을 남긴다. */
    @Override
    public void onPostDelete(PostDeleteEvent event) {
        if (!(event.getEntity() instanceof Auditable auditable)) {
            return;
        }
        String[] names = event.getPersister().getPropertyNames();
        Object[] state = event.getDeletedState();
        List<Change> changes = new ArrayList<>();
        for (int i = 0; state != null && i < names.length; i++) {
            String before = AuditValues.format(state[i]);
            if (!IGNORED.contains(names[i]) && before != null) {
                changes.add(new Change(names[i], before, null));
            }
        }
        write(auditable, (Long) event.getId(), "DELETE", changes);
    }

    /**
     * 엔티티 속성으로 드러나지 않는 변경(예: 권한 그룹의 권한 구성)을 직접 기록한다.
     * field 는 화면 표시용 속성명으로 쓰인다.
     */
    public void record(Auditable auditable, Long id, String field, @Nullable String before, @Nullable String after) {
        if (Objects.equals(before, after)) {
            return;
        }
        write(auditable, id, "UPDATE", List.of(new Change(field, AuditValues.format(before), AuditValues.format(after))));
    }

    private void write(Auditable auditable, Long id, String action, List<Change> changes) {
        AuditRef parent = auditable.auditParent();
        AuditActor actor = currentActor();
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into tb_audit_log (entity_type, entity_id, entity_label, entity_name, parent_type, parent_id, action,
                                              actor_account_id, actor_name, created_at)
                    values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entityType(auditable));
            ps.setLong(2, id);
            ps.setString(3, auditable.auditLabel());
            ps.setString(4, truncate(auditable.auditName(), 200));
            ps.setString(5, parent == null ? null : parent.type());
            ps.setObject(6, parent == null ? null : parent.id());
            ps.setString(7, action);
            ps.setObject(8, actor == null ? null : actor.accountId());
            ps.setString(9, actor == null ? null : truncate(actor.name(), 50));
            ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
        }, key);
        long logId = Objects.requireNonNull(key.getKey()).longValue();
        if (!changes.isEmpty()) {
            jdbcTemplate.batchUpdate("insert into tb_audit_log_change (audit_log_id, field, before_value, after_value) values (?, ?, ?, ?)",
                    changes, changes.size(), (ps, change) -> {
                        ps.setLong(1, logId);
                        ps.setString(2, change.field());
                        ps.setString(3, change.before());
                        ps.setString(4, change.after());
                    });
        }
    }

    static String entityType(Object entity) {
        return org.hibernate.Hibernate.getClassLazy(entity).getSimpleName();
    }

    private static @Nullable String truncate(@Nullable String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }

    private static @Nullable AuditActor currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof AuditActor actor ? actor : null;
    }

    private record Change(String field, @Nullable String before, @Nullable String after) {
    }

}
