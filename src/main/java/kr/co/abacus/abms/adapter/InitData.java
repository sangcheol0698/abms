package kr.co.abacus.abms.adapter;

import java.time.LocalDate;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentCreateRequest;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;

@Profile({"local", "default"})
@Component
public class InitData {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public InitData(EmployeeRepository employeeRepository, DepartmentRepository departmentRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    @PostConstruct
    public void init() {
        // 회사 루트
        Department company = departmentRepository.save(Department.createRoot(
            new DepartmentCreateRequest("ABMS", "아바쿠스", DepartmentType.COMPANY, null)
        ));

        // 본부 구성
        Department mgmtDivision = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("DIV-MGMT", "경영관리본부", DepartmentType.DIVISION, null), company
        ));
        Department techDivision = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("DIV-TECH", "기술본부", DepartmentType.DIVISION, null), company
        ));
        Department salesDivision = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("DIV-SALES", "영업본부", DepartmentType.DIVISION, null), company
        ));

        // 경영관리본부 하위 조직
        Department hrDept = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("DEPT-HR", "인사담당", DepartmentType.DEPARTMENT, null), mgmtDivision
        ));
        Department finDept = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("DEPT-FIN", "재무담당", DepartmentType.DEPARTMENT, null), mgmtDivision
        ));
        Department hrTeam = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEAM-HR", "인사팀", DepartmentType.TEAM, null), hrDept
        ));
        Department finTeam = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEAM-FIN", "재무팀", DepartmentType.TEAM, null), finDept
        ));

        // 기술본부 하위 조직
        Department platformDept = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("DEPT-PLAT", "플랫폼개발담당", DepartmentType.DEPARTMENT, null), techDivision
        ));
        Department dataDept = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("DEPT-DATA", "데이터담당", DepartmentType.DEPARTMENT, null), techDivision
        ));
        Department beTeam = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEAM-BE", "백엔드팀", DepartmentType.TEAM, null), platformDept
        ));
        Department feTeam = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEAM-FE", "프론트엔드팀", DepartmentType.TEAM, null), platformDept
        ));
        Department deTeam = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEAM-DE", "데이터엔지니어링팀", DepartmentType.TEAM, null), dataDept
        ));
        Department lab = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("LAB-RND", "기술연구소", DepartmentType.LAB, null), techDivision
        ));
        Department aiTeam = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEAM-AI", "AI팀", DepartmentType.TEAM, null), lab
        ));

        // 영업본부 하위 조직
        Department krSalesTeam = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEAM-SALES-KR", "국내영업팀", DepartmentType.TEAM, null), salesDivision
        ));
        Department globalSalesTeam = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TEAM-SALES-GLOBAL", "해외영업팀", DepartmentType.TEAM, null), salesDivision
        ));
        Department bizTf = departmentRepository.save(Department.create(
            new DepartmentCreateRequest("TF-BIZ", "사업기획TF", DepartmentType.TF, null), salesDivision
        ));

        // 경영진 (회사 소속)
        employeeRepository.save(Employee.create(employee(
            company.getId(), "ceo@abms.co", "안사장", LocalDate.of(2015, 3, 1), LocalDate.of(1970, 5, 12),
            EmployeePosition.PRESIDENT, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.SKY_GLOW, "대표이사"
        )));
        employeeRepository.save(Employee.create(employee(
            company.getId(), "vp@abms.co", "김부사장", LocalDate.of(2017, 7, 1), LocalDate.of(1973, 9, 3),
            EmployeePosition.VICE_PRESIDENT, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT, EmployeeAvatar.SUNSET_BREEZE, "경영총괄"
        )));

        // 경영관리본부 인사팀
        employeeRepository.save(Employee.create(employee(
            hrTeam.getId(), "hr.mgr@abms.co", "유인사", LocalDate.of(2019, 2, 18), LocalDate.of(1985, 2, 21),
            EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.CORAL_SPARK, "인사팀장"
        )));
        employeeRepository.save(Employee.create(employee(
            hrTeam.getId(), "hr1@abms.co", "박서연", LocalDate.of(2021, 4, 5), LocalDate.of(1994, 11, 13),
            EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL, EmployeeAvatar.FOREST_MINT, "채용/평가"
        )));
        employeeRepository.save(Employee.create(employee(
            hrTeam.getId(), "hr2@abms.co", "최민준", LocalDate.of(2023, 1, 9), LocalDate.of(1997, 8, 22),
            EmployeePosition.ASSOCIATE, EmployeeType.PART_TIME, EmployeeGrade.JUNIOR, EmployeeAvatar.LAVENDER_MOON, "인턴/보조"
        )));

        // 경영관리본부 재무팀
        employeeRepository.save(Employee.create(employee(
            finTeam.getId(), "fin.mgr@abms.co", "한재무", LocalDate.of(2018, 6, 11), LocalDate.of(1983, 12, 2),
            EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.COBALT_WAVE, "재무팀장"
        )));
        employeeRepository.save(Employee.create(employee(
            finTeam.getId(), "fin1@abms.co", "이지은", LocalDate.of(2020, 9, 14), LocalDate.of(1992, 1, 28),
            EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL, EmployeeAvatar.ORANGE_BURST, "결산/세무"
        )));
        employeeRepository.save(Employee.create(employee(
            finTeam.getId(), "fin2@abms.co", "정우성", LocalDate.of(2022, 5, 2), LocalDate.of(1996, 6, 9),
            EmployeePosition.ASSOCIATE, EmployeeType.OUTSOURCING, EmployeeGrade.JUNIOR, EmployeeAvatar.SAGE_GUARD, "외주 지원"
        )));

        // 기술본부 - 플랫폼개발담당: 백엔드팀
        employeeRepository.save(Employee.create(employee(
            beTeam.getId(), "be.lead@abms.co", "홍백엔", LocalDate.of(2018, 4, 2), LocalDate.of(1988, 4, 30),
            EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.BLOSSOM_SMILE, "백엔드팀장"
        )));
        employeeRepository.save(Employee.create(employee(
            beTeam.getId(), "be1@abms.co", "김백엔", LocalDate.of(2021, 3, 8), LocalDate.of(1993, 10, 5),
            EmployeePosition.LEADER, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT, EmployeeAvatar.MIDNIGHT_WINK, "마이크로서비스/아키텍처"
        )));
        employeeRepository.save(Employee.create(employee(
            beTeam.getId(), "be2@abms.co", "이도윤", LocalDate.of(2022, 7, 4), LocalDate.of(1995, 2, 17),
            EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL, EmployeeAvatar.AQUA_SPLASH, "도메인/영속성"
        )));
        employeeRepository.save(Employee.create(employee(
            beTeam.getId(), "be3@abms.co", "박지훈", LocalDate.of(2024, 1, 2), LocalDate.of(1999, 3, 9),
            EmployeePosition.ASSOCIATE, EmployeeType.FREELANCER, EmployeeGrade.JUNIOR, EmployeeAvatar.GOLDEN_RAY, "배치/ETL"
        )));

        // 기술본부 - 플랫폼개발담당: 프론트엔드팀
        employeeRepository.save(Employee.create(employee(
            feTeam.getId(), "fe.lead@abms.co", "이프론", LocalDate.of(2019, 8, 19), LocalDate.of(1989, 7, 21),
            EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.SUNSET_BREEZE, "프론트엔드팀장"
        )));
        employeeRepository.save(Employee.create(employee(
            feTeam.getId(), "fe1@abms.co", "최유진", LocalDate.of(2021, 11, 1), LocalDate.of(1994, 5, 6),
            EmployeePosition.LEADER, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT, EmployeeAvatar.ORANGE_BURST, "Vue/Tailwind"
        )));
        employeeRepository.save(Employee.create(employee(
            feTeam.getId(), "fe2@abms.co", "강서현", LocalDate.of(2023, 3, 6), LocalDate.of(1998, 12, 1),
            EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL, EmployeeAvatar.SKY_GLOW, "CSR/SSR"
        )));

        // 기술본부 - 데이터담당: 데이터엔지니어링팀
        employeeRepository.save(Employee.create(employee(
            deTeam.getId(), "de.lead@abms.co", "유데엔", LocalDate.of(2017, 5, 22), LocalDate.of(1987, 9, 14),
            EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.COBALT_WAVE, "데이터팀장"
        )));
        employeeRepository.save(Employee.create(employee(
            deTeam.getId(), "de1@abms.co", "이현우", LocalDate.of(2020, 10, 12), LocalDate.of(1993, 1, 20),
            EmployeePosition.STAFF, EmployeeType.OUTSOURCING, EmployeeGrade.MID_LEVEL, EmployeeAvatar.LAVENDER_MOON, "파이프라인/모델서빙"
        )));

        // 기술연구소 - AI팀
        employeeRepository.save(Employee.create(employee(
            aiTeam.getId(), "ai.lead@abms.co", "오에이", LocalDate.of(2019, 1, 14), LocalDate.of(1990, 2, 1),
            EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.MIDNIGHT_WINK, "AI팀장"
        )));
        employeeRepository.save(Employee.create(employee(
            aiTeam.getId(), "ai1@abms.co", "김가람", LocalDate.of(2022, 2, 7), LocalDate.of(1996, 4, 11),
            EmployeePosition.LEADER, EmployeeType.FREELANCER, EmployeeGrade.EXPERT, EmployeeAvatar.AQUA_SPLASH, "LLM/프롬프트"
        )));
        employeeRepository.save(Employee.create(employee(
            aiTeam.getId(), "ai2@abms.co", "박하늘", LocalDate.of(2023, 9, 4), LocalDate.of(1998, 7, 19),
            EmployeePosition.ASSOCIATE, EmployeeType.PART_TIME, EmployeeGrade.JUNIOR, EmployeeAvatar.SAGE_GUARD, "리서치 보조"
        )));

        // 영업본부 - 국내영업팀
        employeeRepository.save(Employee.create(employee(
            krSalesTeam.getId(), "sales.kr.mgr@abms.co", "민영국", LocalDate.of(2018, 9, 3), LocalDate.of(1986, 11, 23),
            EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.GOLDEN_RAY, "국내영업팀장"
        )));
        employeeRepository.save(Employee.create(employee(
            krSalesTeam.getId(), "sales.kr1@abms.co", "서지호", LocalDate.of(2021, 6, 7), LocalDate.of(1994, 9, 2),
            EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL, EmployeeAvatar.BLOSSOM_SMILE, "영업/견적"
        )));

        // 영업본부 - 해외영업팀
        employeeRepository.save(Employee.create(employee(
            globalSalesTeam.getId(), "sales.gl.mgr@abms.co", "노해외", LocalDate.of(2017, 4, 10), LocalDate.of(1985, 8, 7),
            EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR, EmployeeAvatar.SUNSET_BREEZE, "해외영업팀장"
        )));
        employeeRepository.save(Employee.create(employee(
            globalSalesTeam.getId(), "sales.gl1@abms.co", "장예린", LocalDate.of(2022, 1, 3), LocalDate.of(1996, 10, 10),
            EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL, EmployeeAvatar.ORANGE_BURST, "수출/파트너"
        )));

        // 영업본부 - 사업기획TF
        employeeRepository.save(Employee.create(employee(
            bizTf.getId(), "biztf.lead@abms.co", "문기획", LocalDate.of(2020, 12, 1), LocalDate.of(1989, 1, 9),
            EmployeePosition.LEADER, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT, EmployeeAvatar.CORAL_SPARK, "TF 리더"
        )));
        employeeRepository.save(Employee.create(employee(
            bizTf.getId(), "biztf1@abms.co", "배수아", LocalDate.of(2023, 4, 3), LocalDate.of(1997, 12, 25),
            EmployeePosition.ASSOCIATE, EmployeeType.OUTSOURCING, EmployeeGrade.JUNIOR, EmployeeAvatar.FOREST_MINT, "시장/경쟁 분석"
        )));

        // 임원 (본부 소속)
        employeeRepository.save(Employee.create(employee(
            mgmtDivision.getId(), "dir.mgmt@abms.co", "박이사", LocalDate.of(2016, 2, 1), LocalDate.of(1978, 6, 18),
            EmployeePosition.DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT, EmployeeAvatar.SAGE_GUARD, "경영관리본부장"
        )));
        employeeRepository.save(Employee.create(employee(
            techDivision.getId(), "md.tech@abms.co", "정상무", LocalDate.of(2016, 9, 1), LocalDate.of(1976, 3, 12),
            EmployeePosition.MANAGING_DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT, EmployeeAvatar.COBALT_WAVE, "기술본부장"
        )));
        employeeRepository.save(Employee.create(employee(
            salesDivision.getId(), "dir.sales@abms.co", "이영업", LocalDate.of(2017, 1, 2), LocalDate.of(1979, 10, 4),
            EmployeePosition.DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT, EmployeeAvatar.GOLDEN_RAY, "영업본부장"
        )));
    }

    private EmployeeCreateRequest employee(
        java.util.UUID departmentId,
        String email,
        String name,
        LocalDate joinDate,
        LocalDate birthDate,
        EmployeePosition position,
        EmployeeType type,
        EmployeeGrade grade,
        EmployeeAvatar avatar,
        String memo
    ) {
        return new EmployeeCreateRequest(
            departmentId,
            email,
            name,
            joinDate,
            birthDate,
            position,
            type,
            grade,
            avatar,
            memo
        );
    }

}
