package kr.co.abacus.abms.attachment;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 삭제한 첨부 파일 정리: 삭제 후 보관 기간이 지난 첨부의 파일 본문과 메타데이터를 지운다.
 * 변경 이력(tb_audit_log)에는 파일명이 그대로 남는다.
 */
@Service
public class AttachmentCleanupService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentCleanupService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final FileStorage fileStorage;

    public AttachmentCleanupService(NamedParameterJdbcTemplate jdbc, FileStorage fileStorage) {
        this.jdbc = jdbc;
        this.fileStorage = fileStorage;
    }

    /** deletedBefore 이전에 삭제된 첨부를 정리하고 정리한 건수를 돌려준다. 본문을 지우지 못한 건은 다음에 다시 시도한다. */
    public int purge(LocalDateTime deletedBefore) {
        List<Target> targets = jdbc.query("select id, stored_path from tb_attachment where deleted = true and deleted_at < :before",
                new MapSqlParameterSource("before", deletedBefore), (rs, i) -> new Target(rs.getLong(1), rs.getString(2)));
        int purged = 0;
        for (Target target : targets) {
            try {
                fileStorage.delete(target.storedPath());
            } catch (IOException | RuntimeException e) {
                log.warn("첨부 파일 본문 삭제 실패 (다음 정리 때 다시 시도): id={}, path={}", target.id(), target.storedPath(), e);
                continue;
            }
            purged += jdbc.update("delete from tb_attachment where id = :id and deleted = true", new MapSqlParameterSource("id", target.id()));
        }
        return purged;
    }

    private record Target(long id, String storedPath) {
    }

}
