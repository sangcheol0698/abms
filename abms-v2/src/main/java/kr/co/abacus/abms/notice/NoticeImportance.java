package kr.co.abacus.abms.notice;

import kr.co.abacus.abms.common.domain.Labeled;

/** 공지 중요도. 팝업은 높은 순서로 보여준다. */
public enum NoticeImportance implements Labeled {

    URGENT("긴급"),
    IMPORTANT("중요"),
    NORMAL("일반");

    private final String label;

    NoticeImportance(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
