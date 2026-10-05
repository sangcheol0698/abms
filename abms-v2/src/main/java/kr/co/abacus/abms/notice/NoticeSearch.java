package kr.co.abacus.abms.notice;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

/**
 * 공지 목록 검색 조건.
 *
 * @param q          제목·본문 검색어
 * @param status     게시 상태 (관리자만. 일반 사용자는 항상 게시 중만 본다)
 * @param unreadOnly 안 읽은 공지만
 */
public record NoticeSearch(@Nullable String q, @Nullable NoticeImportance importance, @Nullable Status status, boolean unreadOnly) {

    public static final NoticeSearch ALL = new NoticeSearch(null, null, null, false);

    public enum Status {
        ACTIVE("게시 중"), SCHEDULED("예약"), ENDED("종료");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public boolean isFiltered() {
        return keyword() != null || importance != null || status != null || unreadOnly;
    }

    public @Nullable String keyword() {
        return q == null || q.isBlank() ? null : q.strip();
    }

    /** LIKE 패턴 (%, _ 는 문자 그대로 찾는다) */
    @Nullable String likePattern() {
        String keyword = keyword();
        if (keyword == null) {
            return null;
        }
        String escaped = keyword.toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }

}
