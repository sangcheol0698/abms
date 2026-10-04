package kr.co.abacus.abms.employee;

import kr.co.abacus.abms.common.domain.Labeled;

/**
 * 직무. SI 인력 배치·검색 기준.
 */
public enum EmployeeJob implements Labeled {

    DEVELOPMENT("개발"),
    PROJECT_MANAGEMENT("PM/PL"),
    PLANNING("기획/분석"),
    DESIGN("디자인/퍼블리싱"),
    QA("품질/테스트"),
    INFRA("인프라/DevOps"),
    DATA("데이터/AI"),
    CONSULTING("컨설팅"),
    SALES("영업"),
    MANAGEMENT("경영지원"),
    ETC("기타");

    private final String label;

    EmployeeJob(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
