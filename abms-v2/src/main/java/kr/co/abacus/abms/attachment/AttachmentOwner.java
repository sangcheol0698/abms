package kr.co.abacus.abms.attachment;

/** 첨부 파일을 붙일 수 있는 대상 */
public enum AttachmentOwner {

    PROJECT("Project"),
    PARTY("Party");

    /** 변경 이력의 상위 엔티티 종류 */
    private final String auditType;

    AttachmentOwner(String auditType) {
        this.auditType = auditType;
    }

    public String auditType() {
        return auditType;
    }

}
