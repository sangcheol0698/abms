package kr.co.abacus.abms.attachment;

import kr.co.abacus.abms.common.domain.Labeled;

public enum AttachmentCategory implements Labeled {

    CONTRACT("계약서"),
    INVOICE("세금계산서"),
    DELIVERABLE("산출물"),
    ETC("기타");

    private final String label;

    AttachmentCategory(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
