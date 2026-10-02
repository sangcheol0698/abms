package kr.co.abacus.abms.notification;

import kr.co.abacus.abms.common.domain.Labeled;

public enum NotificationType implements Labeled {

    INFO("안내"),
    SUCCESS("완료"),
    WARNING("주의"),
    ERROR("오류");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
