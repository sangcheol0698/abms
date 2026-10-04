package kr.co.abacus.abms.project;

import kr.co.abacus.abms.common.domain.Labeled;

/**
 * 프로젝트 수행 장소 구분.
 * 고객사 상주는 주소를 비우면 협력사 주소를, 자사는 주관 부서의 사업장 주소를 쓴다.
 */
public enum WorkPlace implements Labeled {

    OFFICE("자사"),
    CLIENT_SITE("고객사 상주"),
    OTHER("별도 장소"),
    REMOTE("원격");

    private final String label;

    WorkPlace(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
