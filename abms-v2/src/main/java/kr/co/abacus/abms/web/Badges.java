package kr.co.abacus.abms.web;

import kr.co.abacus.abms.common.web.Seed;
import kr.co.abacus.abms.employee.EmployeeStatus;
import kr.co.abacus.abms.employee.EmployeeType;
import kr.co.abacus.abms.notification.NotificationType;
import kr.co.abacus.abms.project.ProjectStatus;

/**
 * 기능별 상태 값 → 뱃지 클래스. 기능 타입을 알아야 해서 공통 도우미({@link kr.co.abacus.abms.common.web.Ui}) 대신 여기에 둔다.
 */
public final class Badges {

    private Badges() {
    }

    public static String of(EmployeeStatus status) {
        return switch (status) {
            case ACTIVE -> Seed.badge("positive");
            case ON_LEAVE -> Seed.badge("warning");
            case RESIGNED -> Seed.badge("neutral");
        };
    }

    public static String of(EmployeeType type) {
        return switch (type) {
            case FULL_TIME -> Seed.badge("informative");
            case FREELANCER -> Seed.badge("neutral", "outline");
            case OUTSOURCING -> Seed.badge("neutral", "outline");
            case PART_TIME -> Seed.badge("neutral");
        };
    }

    public static String of(ProjectStatus status) {
        return switch (status) {
            case SCHEDULED -> Seed.badge("neutral", "outline");
            case IN_PROGRESS -> Seed.badge("informative");
            case COMPLETED -> Seed.badge("positive");
            case ON_HOLD -> Seed.badge("warning");
            case CANCELLED -> Seed.badge("neutral");
        };
    }

    public static String of(NotificationType type) {
        return switch (type) {
            case INFO -> "bg-bg-informative-solid";
            case SUCCESS -> "bg-bg-positive-solid";
            case WARNING -> "bg-bg-warning-solid";
            case ERROR -> "bg-bg-critical-solid";
        };
    }

}
