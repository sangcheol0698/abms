package kr.co.abacus.abms.domain.department;

import lombok.Getter;

@Getter
public enum DepartmentType {

    COMPANY("회사"),            // (주)애버커스
    DIVISION("본부"),           // 경영기획본부, 연구개발본부
    DEPARTMENT("담당"),         // 통신이행담당, 경영빌링담당
    TEAM("팀"),                 // 고객정보팀, 가입정보팀
    LAB("연구소"),               // 플랫폼연구소
    TF("TF");                  // UX STUDIO TF

    private final String description;

    DepartmentType(String description) {
        this.description = description;
    }

}
