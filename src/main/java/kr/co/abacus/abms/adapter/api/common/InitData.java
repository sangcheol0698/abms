package kr.co.abacus.abms.adapter.api.common;

import jakarta.annotation.PostConstruct;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.*;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;
import kr.co.abacus.abms.domain.positionhistory.PositionHistoryCreateRequest;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.shared.Period;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Profile({"local", "default"})
@Component
public class InitData {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;
    private final PartyRepository partyRepository;
    private final PositionHistoryRepository positionHistoryRepository;

    @PostConstruct
    public void init() {
        // 회사 루트
        Department company = departmentRepository.save(Department.create(
                "ABMS", "애버커스", DepartmentType.COMPANY, null, null));

        // 본부 구성
        Department mgmtDivision = departmentRepository.save(Department.create(
                "DIV-MGMT", "경영관리본부", DepartmentType.DIVISION, null,
                company));
        Department techDivision = departmentRepository.save(Department.create(
                "DIV-TECH", "기술본부", DepartmentType.DIVISION, null,
                company));
        Department salesDivision = departmentRepository.save(Department.create(
                "DIV-SALES", "영업본부", DepartmentType.DIVISION, null,
                company));

        // 경영관리본부 하위 조직
        Department hrDept = departmentRepository.save(Department.create(
                "DEPT-HR", "인사담당", DepartmentType.DEPARTMENT, null,
                mgmtDivision));
        Department finDept = departmentRepository.save(Department.create(
                "DEPT-FIN", "재무담당", DepartmentType.DEPARTMENT, null,
                mgmtDivision));
        Department hrTeam = departmentRepository.save(Department.create(
                "TEAM-HR", "인사팀", DepartmentType.TEAM, null, hrDept));
        Department finTeam = departmentRepository.save(Department.create(
                "TEAM-FIN", "재무팀", DepartmentType.TEAM, null, finDept));

        // 기술본부 하위 조직
        Department platformDept = departmentRepository.save(Department.create(
                "DEPT-PLAT", "플랫폼개발담당", DepartmentType.DEPARTMENT, null,
                techDivision));
        Department dataDept = departmentRepository.save(Department.create(
                "DEPT-DATA", "데이터담당", DepartmentType.DEPARTMENT, null,
                techDivision));
        Department beTeam = departmentRepository.save(Department.create(
                "TEAM-BE", "백엔드팀", DepartmentType.TEAM, null,
                platformDept));
        Department feTeam = departmentRepository.save(Department.create(
                "TEAM-FE", "프론트엔드팀", DepartmentType.TEAM, null,
                platformDept));
        Department deTeam = departmentRepository.save(Department.create(
                "TEAM-DE", "데이터엔지니어링팀", DepartmentType.TEAM, null,
                dataDept));
        Department lab = departmentRepository.save(Department.create(
                "LAB-RND", "기술연구소", DepartmentType.LAB, null,
                techDivision));
        Department aiTeam = departmentRepository.save(Department.create(
                "TEAM-AI", "AI팀", DepartmentType.TEAM, null, lab));

        // 영업본부 하위 조직
        Department krSalesTeam = departmentRepository.save(Department.create(
                "TEAM-SALES-KR", "국내영업팀", DepartmentType.TEAM, null,
                salesDivision));
        Department globalSalesTeam = departmentRepository.save(Department.create(
                "TEAM-SALES-GLOBAL", "해외영업팀", DepartmentType.TEAM, null,
                salesDivision));
        Department bizTf = departmentRepository.save(Department.create(
                "TF-BIZ", "사업기획TF", DepartmentType.TF, null,
                salesDivision));

        // 경영진 (회사 소속)
        employeeRepository.save(createEmployee(
                company.getId(), "ceo@abms.co", "안사장", LocalDate.of(2015, 3, 1),
                LocalDate.of(1970, 5, 12),
                EmployeePosition.PRESIDENT, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                "대표이사"));
        employeeRepository.save(createEmployee(
                company.getId(), "vp@abms.co", "김부사장", LocalDate.of(2017, 7, 1),
                LocalDate.of(1973, 9, 3),
                EmployeePosition.VICE_PRESIDENT, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT,
                EmployeeAvatar.SUNSET_BREEZE, "경영총괄"));

        // 경영관리본부 인사팀
        employeeRepository.save(createEmployee(
                hrTeam.getId(), "hr.mgr@abms.co", "유인사", LocalDate.of(2019, 2, 18),
                LocalDate.of(1985, 2, 21),
                EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.CORAL_SPARK,
                "인사팀장"));
        employeeRepository.save(createEmployee(
                hrTeam.getId(), "hr1@abms.co", "박서연", LocalDate.of(2021, 4, 5),
                LocalDate.of(1994, 11, 13),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.FOREST_MINT,
                "채용/평가"));
        employeeRepository.save(createEmployee(
                hrTeam.getId(), "hr2@abms.co", "최민준", LocalDate.of(2023, 1, 9),
                LocalDate.of(1997, 8, 22),
                EmployeePosition.ASSOCIATE, EmployeeType.PART_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.LAVENDER_MOON,
                "인턴/보조"));

        // 경영관리본부 재무팀
        employeeRepository.save(createEmployee(
                finTeam.getId(), "fin.mgr@abms.co", "한재무", LocalDate.of(2018, 6, 11),
                LocalDate.of(1983, 12, 2),
                EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.COBALT_WAVE,
                "재무팀장"));
        employeeRepository.save(createEmployee(
                finTeam.getId(), "fin1@abms.co", "이지은", LocalDate.of(2020, 9, 14),
                LocalDate.of(1992, 1, 28),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.ORANGE_BURST,
                "결산/세무"));
        employeeRepository.save(createEmployee(
                finTeam.getId(), "fin2@abms.co", "정우성", LocalDate.of(2022, 5, 2),
                LocalDate.of(1996, 6, 9),
                EmployeePosition.ASSOCIATE, EmployeeType.OUTSOURCING, EmployeeGrade.JUNIOR,
                EmployeeAvatar.SAGE_GUARD,
                "외주 지원"));

        // 기술본부 - 플랫폼개발담당: 백엔드팀
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be.lead@abms.co", "홍백엔", LocalDate.of(2018, 4, 2),
                LocalDate.of(1988, 4, 30),
                EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.BLOSSOM_SMILE,
                "백엔드팀장"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be1@abms.co", "김백엔", LocalDate.of(2021, 3, 8),
                LocalDate.of(1993, 10, 5),
                EmployeePosition.LEADER, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT,
                EmployeeAvatar.MIDNIGHT_WINK,
                "마이크로서비스/아키텍처"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be2@abms.co", "이도윤", LocalDate.of(2022, 7, 4),
                LocalDate.of(1995, 2, 17),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.AQUA_SPLASH,
                "도메인/영속성"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be3@abms.co", "박지훈", LocalDate.of(2024, 1, 2),
                LocalDate.of(1999, 3, 9),
                EmployeePosition.ASSOCIATE, EmployeeType.FREELANCER, EmployeeGrade.JUNIOR,
                EmployeeAvatar.GOLDEN_RAY,
                "배치/ETL"));

        employeeRepository.save(createEmployee(
                beTeam.getId(), "be4@abms.co", "김민수", LocalDate.of(2024, 2, 1),
                LocalDate.of(2000, 1, 15),
                EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.CORAL_SPARK,
                "API 개발"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be5@abms.co", "이지훈", LocalDate.of(2023, 5, 10),
                LocalDate.of(1999, 8, 20),
                EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.FOREST_MINT,
                "DB 설계"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be6@abms.co", "박서준", LocalDate.of(2022, 11, 15),
                LocalDate.of(1997, 3, 5),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.LAVENDER_MOON,
                "서버 구축"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be7@abms.co", "최도현", LocalDate.of(2021, 8, 20),
                LocalDate.of(1995, 12, 10),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.COBALT_WAVE,
                "클라우드 인프라"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be8@abms.co", "정하은", LocalDate.of(2024, 3, 1),
                LocalDate.of(2001, 5, 25),
                EmployeePosition.ASSOCIATE, EmployeeType.PART_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.ORANGE_BURST,
                "테스트 자동화"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be9@abms.co", "강지우", LocalDate.of(2023, 1, 5),
                LocalDate.of(1998, 9, 30),
                EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.SAGE_GUARD,
                "보안 모듈"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be10@abms.co", "조서연", LocalDate.of(2022, 6, 15),
                LocalDate.of(1996, 2, 14),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.BLOSSOM_SMILE,
                "결제 시스템"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be11@abms.co", "윤민지", LocalDate.of(2021, 9, 10),
                LocalDate.of(1994, 7, 7),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.MIDNIGHT_WINK,
                "로그 분석"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be12@abms.co", "장우진", LocalDate.of(2024, 4, 1),
                LocalDate.of(2000, 11, 11),
                EmployeePosition.ASSOCIATE, EmployeeType.FREELANCER, EmployeeGrade.JUNIOR,
                EmployeeAvatar.AQUA_SPLASH,
                "배포 파이프라인"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be13@abms.co", "임현우", LocalDate.of(2023, 8, 20),
                LocalDate.of(1999, 4, 4),
                EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.GOLDEN_RAY,
                "모니터링"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be14@abms.co", "한지민", LocalDate.of(2022, 12, 5),
                LocalDate.of(1997, 10, 20),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.SUNSET_BREEZE,
                "알림 서비스"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be15@abms.co", "오서윤", LocalDate.of(2021, 5, 15),
                LocalDate.of(1995, 6, 6),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.SKY_GLOW,
                "회원 관리"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be16@abms.co", "서준호", LocalDate.of(2024, 1, 10),
                LocalDate.of(2001, 12, 12),
                EmployeePosition.ASSOCIATE, EmployeeType.PART_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.CORAL_SPARK,
                "데이터 마이그레이션"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be17@abms.co", "신예은", LocalDate.of(2023, 10, 1),
                LocalDate.of(1998, 1, 1),
                EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.FOREST_MINT,
                "API 문서화"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be18@abms.co", "권민재", LocalDate.of(2022, 3, 20),
                LocalDate.of(1996, 8, 15),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.LAVENDER_MOON,
                "성능 최적화"));
        employeeRepository.save(createEmployee(
                beTeam.getId(), "be19@abms.co", "황지수", LocalDate.of(2021, 7, 5),
                LocalDate.of(1994, 5, 5),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.COBALT_WAVE,
                "레거시 청산"));

        // 기술본부 - 플랫폼개발담당: 프론트엔드팀
        employeeRepository.save(createEmployee(
                feTeam.getId(), "fe.lead@abms.co", "이프론", LocalDate.of(2019, 8, 19),
                LocalDate.of(1989, 7, 21),
                EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SUNSET_BREEZE,
                "프론트엔드팀장"));
        employeeRepository.save(createEmployee(
                feTeam.getId(), "fe1@abms.co", "최유진", LocalDate.of(2021, 11, 1),
                LocalDate.of(1994, 5, 6),
                EmployeePosition.LEADER, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT,
                EmployeeAvatar.ORANGE_BURST,
                "Vue/Tailwind"));
        employeeRepository.save(createEmployee(
                feTeam.getId(), "fe2@abms.co", "강서현", LocalDate.of(2023, 3, 6),
                LocalDate.of(1998, 12, 1),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.SKY_GLOW,
                "CSR/SSR"));

        // 기술본부 - 데이터담당: 데이터엔지니어링팀
        employeeRepository.save(createEmployee(
                deTeam.getId(), "de.lead@abms.co", "유데엔", LocalDate.of(2017, 5, 22),
                LocalDate.of(1987, 9, 14),
                EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.COBALT_WAVE,
                "데이터팀장"));
        employeeRepository.save(createEmployee(
                deTeam.getId(), "de1@abms.co", "이현우", LocalDate.of(2020, 10, 12),
                LocalDate.of(1993, 1, 20),
                EmployeePosition.STAFF, EmployeeType.OUTSOURCING, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.LAVENDER_MOON,
                "파이프라인/모델서빙"));

        // 기술연구소 - AI팀
        employeeRepository.save(createEmployee(
                aiTeam.getId(), "ai.lead@abms.co", "오에이", LocalDate.of(2019, 1, 14),
                LocalDate.of(1990, 2, 1),
                EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.MIDNIGHT_WINK,
                "AI팀장"));
        employeeRepository.save(createEmployee(
                aiTeam.getId(), "ai1@abms.co", "김가람", LocalDate.of(2022, 2, 7),
                LocalDate.of(1996, 4, 11),
                EmployeePosition.LEADER, EmployeeType.FREELANCER, EmployeeGrade.EXPERT,
                EmployeeAvatar.AQUA_SPLASH,
                "LLM/프롬프트"));
        employeeRepository.save(createEmployee(
                aiTeam.getId(), "ai2@abms.co", "박하늘", LocalDate.of(2023, 9, 4),
                LocalDate.of(1998, 7, 19),
                EmployeePosition.ASSOCIATE, EmployeeType.PART_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.SAGE_GUARD,
                "리서치 보조"));

        // 영업본부 - 국내영업팀
        employeeRepository.save(createEmployee(
                krSalesTeam.getId(), "sales.kr.mgr@abms.co", "민영국", LocalDate.of(2018, 9, 3),
                LocalDate.of(1986, 11, 23),
                EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.GOLDEN_RAY,
                "국내영업팀장"));
        employeeRepository.save(createEmployee(
                krSalesTeam.getId(), "sales.kr1@abms.co", "서지호", LocalDate.of(2021, 6, 7),
                LocalDate.of(1994, 9, 2),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.BLOSSOM_SMILE,
                "영업/견적"));

        // 영업본부 - 해외영업팀
        employeeRepository.save(createEmployee(
                globalSalesTeam.getId(), "sales.gl.mgr@abms.co", "노해외", LocalDate.of(2017, 4, 10),
                LocalDate.of(1985, 8, 7),
                EmployeePosition.MANAGER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SUNSET_BREEZE,
                "해외영업팀장"));
        employeeRepository.save(createEmployee(
                globalSalesTeam.getId(), "sales.gl1@abms.co", "장예린", LocalDate.of(2022, 1, 3),
                LocalDate.of(1996, 10, 10),
                EmployeePosition.STAFF, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.ORANGE_BURST,
                "수출/파트너"));

        // 영업본부 - 사업기획TF
        employeeRepository.save(createEmployee(
                bizTf.getId(), "biztf.lead@abms.co", "문기획", LocalDate.of(2020, 12, 1),
                LocalDate.of(1989, 1, 9),
                EmployeePosition.LEADER, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT,
                EmployeeAvatar.CORAL_SPARK,
                "TF 리더"));
        employeeRepository.save(createEmployee(
                bizTf.getId(), "biztf1@abms.co", "배수아", LocalDate.of(2023, 4, 3),
                LocalDate.of(1997, 12, 25),
                EmployeePosition.ASSOCIATE, EmployeeType.OUTSOURCING, EmployeeGrade.JUNIOR,
                EmployeeAvatar.FOREST_MINT,
                "시장/경쟁 분석"));

        // 임원 (본부 소속)
        employeeRepository.save(createEmployee(
                mgmtDivision.getId(), "dir.mgmt@abms.co", "박이사", LocalDate.of(2016, 2, 1),
                LocalDate.of(1978, 6, 18),
                EmployeePosition.DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT,
                EmployeeAvatar.SAGE_GUARD,
                "경영관리본부장"));
        employeeRepository.save(createEmployee(
                techDivision.getId(), "md.tech@abms.co", "정상무", LocalDate.of(2016, 9, 1),
                LocalDate.of(1976, 3, 12),
                EmployeePosition.MANAGING_DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT,
                EmployeeAvatar.COBALT_WAVE, "기술본부장"));
        employeeRepository.save(createEmployee(
                salesDivision.getId(), "dir.sales@abms.co", "이영업", LocalDate.of(2017, 1, 2),
                LocalDate.of(1979, 10, 4),
                EmployeePosition.DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT,
                EmployeeAvatar.GOLDEN_RAY,
                "영업본부장"));

        // ===== 협력사 생성 =====
        Party partyNaverCloud = partyRepository.save(Party.create(new PartyCreateRequest(
                "네이버클라우드",
                "이준호",
                "김영업",
                "010-1234-5678",
                "sales@navercloud.com")));

        Party partySamsungSDS = partyRepository.save(Party.create(new PartyCreateRequest(
                "삼성SDS",
                "박철수",
                "최담당",
                "010-2345-6789",
                "contact@samsungsds.com")));

        Party partyKakao = partyRepository.save(Party.create(new PartyCreateRequest(
                "카카오",
                "정여진",
                "이매니저",
                "010-3456-7890",
                "partner@kakao.com")));

        Party partyLGCNS = partyRepository.save(Party.create(new PartyCreateRequest(
                "LG CNS",
                "강대리",
                "송과장",
                "010-4567-8901",
                "business@lgcns.com")));

        Party partySkTelecom = partyRepository.save(Party.create(new PartyCreateRequest(
                "SK텔레콤",
                "윤사장",
                "임대리",
                "010-5678-9012",
                "sales@sktelecom.com")));

        // ===== 프로젝트 생성 =====
        projectRepository.save(Project.create(new ProjectCreateRequest(
                partyNaverCloud.getId(),
                "PROJ-2024-001",
                "네이버클라우드 ERP 시스템 구축",
                "네이버클라우드 기반 전사 ERP 시스템 개발 및 구축 프로젝트",
                ProjectStatus.IN_PROGRESS,
                580000000L,
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 12, 31))));

        projectRepository.save(Project.create(new ProjectCreateRequest(
                partySamsungSDS.getId(),
                "PROJ-2024-002",
                "삼성전자 협력사 포털 개발",
                "삼성전자向 협력사 통합 관리 포털 시스템 구축",
                ProjectStatus.IN_PROGRESS,
                420000000L,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 10, 31))));

        projectRepository.save(Project.create(new ProjectCreateRequest(
                partyKakao.getId(),
                "PROJ-2024-003",
                "카카오 AI 챗봇 플랫폼",
                "LLM 기반 고객지원 챗봇 플랫폼 개발",
                ProjectStatus.COMPLETED,
                350000000L,
                LocalDate.of(2023, 9, 1),
                LocalDate.of(2024, 2, 29))));

        projectRepository.save(Project.create(new ProjectCreateRequest(
                partyLGCNS.getId(),
                "PROJ-2024-004",
                "LG화학 데이터 웨어하우스",
                "빅데이터 기반 통합 데이터 웨어하우스 구축",
                ProjectStatus.ON_HOLD,
                820000000L,
                LocalDate.of(2024, 7, 1),
                LocalDate.of(2025, 6, 30))));

        projectRepository.save(Project.create(new ProjectCreateRequest(
                partySkTelecom.getId(),
                "PROJ-2024-005",
                "5G IoT 플랫폼 고도화",
                "5G 기반 IoT 디바이스 통합 관리 플랫폼 고도화",
                ProjectStatus.IN_PROGRESS,
                670000000L,
                LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 11, 30))));

        projectRepository.save(Project.create(new ProjectCreateRequest(
                partyNaverCloud.getId(),
                "PROJ-2023-008",
                "네이버페이 정산 시스템 리뉴얼",
                "차세대 정산 시스템 개발 및 레거시 마이그레이션",
                ProjectStatus.COMPLETED,
                480000000L,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31))));

        projectRepository.save(Project.create(new ProjectCreateRequest(
                partySamsungSDS.getId(),
                "PROJ-2023-012",
                "삼성디스플레이 MES 고도화",
                "제조실행시스템(MES) 기능 개선 및 고도화",
                ProjectStatus.CANCELLED,
                390000000L,
                LocalDate.of(2023, 6, 1),
                LocalDate.of(2024, 3, 31))));

        projectRepository.save(Project.create(new ProjectCreateRequest(
                partyKakao.getId(),
                "PROJ-2025-001",
                "카카오뱅크 디지털 플랫폼",
                "차세대 디지털 뱅킹 플랫폼 구축",
                ProjectStatus.ON_HOLD,
                920000000L,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31))));

        // ===== 직급 이력 생성 =====
        positionHistoryRepository.save(PositionHistory.create(
                new PositionHistoryCreateRequest(1L,
                        new Period(LocalDate.of(2017, 7, 1), LocalDate.of(2020, 12, 31)),
                        EmployeePosition.VICE_PRESIDENT
                )
        ));

        positionHistoryRepository.save(PositionHistory.create(
                new PositionHistoryCreateRequest(1L,
                        new Period(LocalDate.of(2021, 1, 1), LocalDate.of(9999, 12, 31)),
                        EmployeePosition.PRESIDENT
                )
        ));

        positionHistoryRepository.save(PositionHistory.create(
                new PositionHistoryCreateRequest(45L,
                        new Period(LocalDate.of(2017, 1, 1), LocalDate.of(2019, 12, 31)),
                        EmployeePosition.LEADER
                )
        ));

        positionHistoryRepository.save(PositionHistory.create(
                new PositionHistoryCreateRequest(45L,
                        new Period(LocalDate.of(2020, 1, 1), LocalDate.of(2024, 12, 31)),
                        EmployeePosition.MANAGER
                )
        ));

        positionHistoryRepository.save(PositionHistory.create(
                new PositionHistoryCreateRequest(45L,
                        new Period(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
                        EmployeePosition.SENIOR_MANAGER
                )
        ));

        positionHistoryRepository.save(PositionHistory.create(
                new PositionHistoryCreateRequest(45L,
                        new Period(LocalDate.of(2026, 1, 1), LocalDate.of(9999, 12, 31)),
                        EmployeePosition.DIRECTOR
                )
        ));
    }

    private Employee createEmployee(Long departmentId, String email, String name, LocalDate joinDate,
                                    LocalDate birthDate, EmployeePosition position, EmployeeType type,
                                    EmployeeGrade grade, EmployeeAvatar avatar, String memo) {
        return Employee.create(
                departmentId,
                name,
                email,
                joinDate,
                birthDate,
                position,
                type,
                grade,
                avatar,
                memo);
    }

}
