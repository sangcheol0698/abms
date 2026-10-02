package kr.co.abacus.abms.common.web;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.Labeled;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.employee.EmployeeStatus;
import kr.co.abacus.abms.employee.EmployeeType;
import kr.co.abacus.abms.notification.NotificationType;
import kr.co.abacus.abms.project.ProjectStatus;

/**
 * JTE 템플릿에서 쓰는 표시 형식/스타일 도우미.
 */
public final class Ui {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy년 M월");

    private Ui() {
    }

    public static String date(@Nullable LocalDate date) {
        return date == null ? "-" : DATE.format(date);
    }

    public static String dateTime(@Nullable LocalDateTime dateTime) {
        return dateTime == null ? "-" : DATE_TIME.format(dateTime);
    }

    public static String month(YearMonth month) {
        return MONTH.format(month);
    }

    public static String period(LocalDate start, @Nullable LocalDate end) {
        return date(start) + " ~ " + (end == null ? "진행 중" : date(end));
    }

    /** 폼 입력값 (null → 빈 문자열) */
    public static String value(@Nullable Object value) {
        return value == null ? "" : value.toString();
    }

    public static String won(@Nullable Money money) {
        return money == null ? "-" : money.formatted();
    }

    public static String percent(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString() + "%";
    }

    /** 양수/음수 금액 색상 */
    public static String signClass(Money money) {
        int sign = money.amount().signum();
        return sign < 0 ? "negative" : sign > 0 ? "positive" : "";
    }

    public static String relative(@Nullable LocalDateTime time) {
        if (time == null) {
            return "";
        }
        java.time.Duration d = java.time.Duration.between(time, LocalDateTime.now());
        if (d.toMinutes() < 1) {
            return "방금 전";
        }
        if (d.toHours() < 1) {
            return d.toMinutes() + "분 전";
        }
        if (d.toDays() < 1) {
            return d.toHours() + "시간 전";
        }
        if (d.toDays() < 7) {
            return d.toDays() + "일 전";
        }
        return date(time.toLocalDate());
    }

    public static String errorTitle(@Nullable Integer status) {
        int code = status == null ? 500 : status;
        return switch (code) {
            case 403 -> "접근 권한이 없습니다";
            case 404 -> "페이지를 찾을 수 없습니다";
            case 400, 409, 422 -> "요청을 처리할 수 없습니다";
            default -> "오류가 발생했습니다";
        };
    }

    public static String errorDetail(@Nullable String message) {
        if (message == null || message.isBlank() || "No message available".equals(message)) {
            return "잠시 후 다시 시도해 주세요. 문제가 계속되면 관리자에게 문의하세요.";
        }
        return message;
    }

    public static String badge(EmployeeStatus status) {
        return switch (status) {
            case ACTIVE -> "badge badge-green";
            case ON_LEAVE -> "badge badge-yellow";
            case RESIGNED -> "badge badge-gray";
        };
    }

    public static String badge(EmployeeType type) {
        return switch (type) {
            case FULL_TIME -> "badge badge-blue";
            case FREELANCER -> "badge badge-purple";
            case OUTSOURCING -> "badge badge-yellow";
            case PART_TIME -> "badge badge-gray";
        };
    }

    public static String badge(ProjectStatus status) {
        return switch (status) {
            case SCHEDULED -> "badge badge-purple";
            case IN_PROGRESS -> "badge badge-blue";
            case COMPLETED -> "badge badge-green";
            case ON_HOLD -> "badge badge-yellow";
            case CANCELLED -> "badge badge-gray";
        };
    }

    public static String badge(NotificationType type) {
        return switch (type) {
            case INFO -> "bg-brand-500";
            case SUCCESS -> "bg-emerald-500";
            case WARNING -> "bg-amber-500";
            case ERROR -> "bg-rose-500";
        };
    }

    public static <E extends Enum<E> & Labeled> List<SelectOption> options(E[] values) {
        return Arrays.stream(values).map(v -> new SelectOption(v.name(), v.label())).toList();
    }

    public record SelectOption(String value, String label) {
    }

}
