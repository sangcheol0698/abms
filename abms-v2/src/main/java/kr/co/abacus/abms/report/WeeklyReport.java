package kr.co.abacus.abms.report;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * 주간 운영 보고서 초안. 시스템 데이터 스냅샷을 바탕으로 생성되며 사용자가 편집할 수 있다.
 */
@Entity
@Table(name = "tb_weekly_report")
@SQLRestriction("deleted = false")
public class WeeklyReport extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private LocalDate weekStart;

    @Column(nullable = false)
    private LocalDate weekEnd;

    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Generator generator;

    @Column(nullable = false)
    private boolean edited;

    @Column(nullable = false)
    private Long authorAccountId;

    protected WeeklyReport() {
    }

    public static WeeklyReport create(Long authorAccountId, LocalDate weekStart, String content, Generator generator) {
        WeeklyReport report = new WeeklyReport();
        report.authorAccountId = authorAccountId;
        report.weekStart = weekStart;
        report.weekEnd = weekStart.plusDays(6);
        report.title = weekStart.getYear() + "년 " + weekStart.getMonthValue() + "월 " + weekOfMonth(weekStart) + "주차 주간 보고";
        report.content = content;
        report.generator = generator;
        report.edited = false;
        return report;
    }

    public void edit(String title, String content) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("제목을 입력하세요.");
        }
        if (content == null || content.isBlank()) {
            throw new BusinessException("내용을 입력하세요.");
        }
        this.title = title.strip();
        this.content = content;
        this.edited = true;
    }

    private static int weekOfMonth(LocalDate date) {
        return (date.getDayOfMonth() - 1) / 7 + 1;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public LocalDate getWeekEnd() {
        return weekEnd;
    }

    public String getContent() {
        return content;
    }

    public Generator getGenerator() {
        return generator;
    }

    public boolean isEdited() {
        return edited;
    }

    public Long getAuthorAccountId() {
        return authorAccountId;
    }

    public enum Generator {
        AI, TEMPLATE
    }

}
