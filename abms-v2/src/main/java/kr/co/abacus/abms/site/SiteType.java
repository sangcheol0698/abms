package kr.co.abacus.abms.site;

import kr.co.abacus.abms.common.domain.Labeled;

public enum SiteType implements Labeled {

    HEADQUARTERS("본사"),
    BRANCH("지사"),
    RESEARCH("연구소"),
    OFFICE("사무소"),
    ETC("기타");

    private final String label;

    SiteType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
