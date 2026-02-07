package kr.co.abacus.abms.adapter.api.common;

import jakarta.annotation.PostConstruct;

import kr.co.abacus.abms.application.department.DepartmentModifyService;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.EmployeeModifyService;
import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
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

    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;
    private final PartyRepository partyRepository;
    private final EmployeeModifyService employeeModifyService;

    @PostConstruct
    public void init() {
        // ---------------------------------------------------------
        // 조직도 구성
        // ---------------------------------------------------------

        // Root Company (ABC0000)
        Department company = departmentRepository.save(Department.create(
            "ABC0000", "(주)애버커스", DepartmentType.COMPANY, 1L, null)
        );

        // 경영기획본부 (ABC1000)
        Department mngDivision = departmentRepository.save(Department.create(
            "ABC1000", "경영기획본부", DepartmentType.DIVISION, 4L, company)
        );

        // 연구개발본부 (ABC4000)
        Department rndDivision = departmentRepository.save(Department.create(
            "ABC4000", "연구개발본부", DepartmentType.DIVISION, null, company)
        );
        // 기술연구소 (ABC4100)
        Department techLab = departmentRepository.save(Department.create(
            "ABC4100", "기술연구소", DepartmentType.LAB, null, rndDivision)
        );
        // 팀 (ABC4101 ~ ABC4199)
        Department ABC4101 = departmentRepository.save(Department.create(
            "ABC4101", "플랫폼연구개발팀", DepartmentType.TEAM, null, techLab)
        );

        // 미래사업본부 (ABC3000)
        Department futDivision = departmentRepository.save(Department.create(
            "ABC3000", "미래사업본부", DepartmentType.DIVISION, null, company)
        );
        // 전략사업담당 (ABC3100)
        Department strgGroup = departmentRepository.save(Department.create(
            "ABC3100", "전략사업담당", DepartmentType.GROUP, null, futDivision)
        );
        // 팀 (ABC3101 ~ ABC3199)
        Department ABC3101 = departmentRepository.save(Department.create(
            "ABC3101", "컨버전스사업팀", DepartmentType.TEAM, 14L, strgGroup)
        );
        Department ABC3102 = departmentRepository.save(Department.create(
            "ABC3102", "핀테크사업팀", DepartmentType.TEAM, 9L, strgGroup)
        );
        Department ABC3103 = departmentRepository.save(Department.create(
            "ABC3103", "IoT융합사업팀", DepartmentType.TEAM, null, strgGroup)
        );
        Department ABC3104 = departmentRepository.save(Department.create(
            "ABC3104", "융합서비스사업팀", DepartmentType.TEAM, null, strgGroup)
        );
        // AI/Data사업담당 (ABC3200)
        Department aiDataGroup = departmentRepository.save(Department.create(
            "ABC3200", "AI/Data사업담당", DepartmentType.GROUP, null, futDivision)
        );
        // 팀 (ABC3201 ~ ABC3299)
        Department ABC3201 = departmentRepository.save(Department.create(
            "ABC3201", "Data플랫폼사업팀", DepartmentType.TEAM, 10L, aiDataGroup)
        );
        Department ABC3202 = departmentRepository.save(Department.create(
            "ABC3202", "지능플랫폼사업팀", DepartmentType.TEAM, 12L, aiDataGroup)
        );
        Department ABC3203 = departmentRepository.save(Department.create(
            "ABC3203", "Data서비스운영팀", DepartmentType.TEAM, 18L, aiDataGroup)
        );
        Department ABC3204 = departmentRepository.save(Department.create(
            "ABC3204", "AX플랫폼사업팀", DepartmentType.TEAM, 28L, aiDataGroup)
        );
        Department ABC3205 = departmentRepository.save(Department.create(
            "ABC3205", "UX STUDIO TF", DepartmentType.TEAM, null, futDivision)
        );


        // 통신사업본부 (ABC2000)
        Department telDivision = departmentRepository.save(Department.create(
            "ABC2000", "통신사업본부", DepartmentType.DIVISION, 3L, company)
        );
        // 통신이행담당 (ABC2100)
        Department telImpGroup = departmentRepository.save(Department.create(
            "ABC2100", "통신이행담당", DepartmentType.GROUP, null, telDivision)
        );
        // 팀 (ABC2101 ~ ABC2199)
        Department ABC2101 = departmentRepository.save(Department.create(
            "ABC2101", "고객정보팀", DepartmentType.TEAM, 16L, telImpGroup)
        );
        Department ABC2102 = departmentRepository.save(Department.create(
            "ABC2102", "가입정보팀", DepartmentType.TEAM, 15L, telImpGroup)
        );
        Department ABC2103 = departmentRepository.save(Department.create(
            "ABC2103", "빌링시스템팀", DepartmentType.TEAM, null, telImpGroup)
        );
        Department ABC2104 = departmentRepository.save(Department.create(
            "ABC2104", "영업정보팀", DepartmentType.TEAM, null, telImpGroup)
        );
        Department ABC2105 = departmentRepository.save(Department.create(
            "ABC2105", "기반기술팀", DepartmentType.TEAM, 11L, telImpGroup)
        );
        // 경영빌링담당 (ABC2200)
        Department mngBillGroup = departmentRepository.save(Department.create(
            "ABC2200", "경영빌링담당", DepartmentType.GROUP, 6L, telDivision)
        );
        // 팀 (ABC2201 ~ ABC2299)
        Department ABC2201 = departmentRepository.save(Department.create(
            "ABC2201", "경영플랫폼팀", DepartmentType.TEAM, null, mngBillGroup)
        );
        Department ABC2202 = departmentRepository.save(Department.create(
            "ABC2202", "CRM팀", DepartmentType.TEAM, null, mngBillGroup)
        );
        Department ABC2203 = departmentRepository.save(Department.create(
            "ABC2203", "경영정보팀", DepartmentType.TEAM, null, mngBillGroup)
        );
        Department ABC2204 = departmentRepository.save(Department.create(
            "ABC2204", "NMS사업팀", DepartmentType.TEAM, 13L, mngBillGroup)
        );
        Department ABC2205 = departmentRepository.save(Department.create(
            "ABC2205", "융합데이터분석팀", DepartmentType.TEAM, null, mngBillGroup)
        );


        // ---------------------------------------------------------
        // 직원 구성
        // ---------------------------------------------------------

        // 임원진
        employeeModifyService.create(
            createEmployeeCreateCommand(
                company.getId(), "lyt@abms.co", "임회장", LocalDate.of(2001, 1, 30),
                LocalDate.of(1960, 5, 12),
                EmployeePosition.CHAIRMAN, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                "대표이사")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                company.getId(), "chaesungsoo@abms.co", "채사장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.PRESIDENT, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SUNSET_BREEZE,
                "사업총괄")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                telDivision.getId(), "ydp@abms.co", "유부사장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.VICE_PRESIDENT, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.CORAL_SPARK,
                "통신")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                mngDivision.getId(), "chin@abms.co", "진부사장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.VICE_PRESIDENT, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.CORAL_SPARK,
                "경영")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                futDivision.getId(), "jhkim@abms.co", "김상무", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.MANAGING_DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.FOREST_MINT,
                "미래")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                mngBillGroup.getId(), "hades@abms.co", "허담당", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.FOREST_MINT,
                "경영빌링담당")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                futDivision.getId(), "hyosang@abms.co", "홍이사", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TECHNICAL_DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT,
                EmployeeAvatar.FOREST_MINT,
                "미래")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                futDivision.getId(), "harryson@abms.co", "손이사", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TECHNICAL_DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT,
                EmployeeAvatar.FOREST_MINT,
                "미래")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                rndDivision.getId(), "yjeepark@abms.co", "박상무", LocalDate.of(2007, 6, 1),
                LocalDate.of(1968, 10, 10),
                EmployeePosition.MANAGING_DIRECTOR, EmployeeType.FULL_TIME, EmployeeGrade.EXPERT,
                EmployeeAvatar.FOREST_MINT,
                "")
        );

        // 중간 관리자
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3102.getId(), "ysfl@abms.co", "고팀장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.GOLDEN_RAY,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3201.getId(), "krinsa@abms.co", "류팀장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SAGE_GUARD,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2105.getId(), "sh3817@abms.co", "김팀장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SAGE_GUARD,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3202.getId(), "babopuding@abms.co", "금팀장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SAGE_GUARD,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2204.getId(), "zase98@abms.co", "박팀장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SAGE_GUARD,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3101.getId(), "uhchae@abms.co", "채팀장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SAGE_GUARD,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2102.getId(), "higo100@abms.co", "양팀장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SAGE_GUARD,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2101.getId(), "ladder@abms.co", "김팀장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SAGE_GUARD,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3203.getId(), "jyc939393@abms.co", "지팀장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.SKY_GLOW,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2205.getId(), "sogarib@abms.co", "봉팀장", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.TEAM_LEADER, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.COBALT_WAVE,
                "")
        );

        // 실무진
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2204.getId(), "cidlist@abms.co", "권책임", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.PRINCIPAL, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.AQUA_SPLASH,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2203.getId(), "bsaba@abms.co", "김책임", LocalDate.of(2002, 3, 1),
                LocalDate.of(1962, 7, 1),
                EmployeePosition.PRINCIPAL, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.BLOSSOM_SMILE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                mngDivision.getId(), "azure00@abms.co", "김책임", LocalDate.of(2002, 3, 1),
                LocalDate.of(1969, 7, 1),
                EmployeePosition.PRINCIPAL, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.BLOSSOM_SMILE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2101.getId(), "breadco@abms.co", "류책임", LocalDate.of(2002, 3, 1),
                LocalDate.of(1971, 7, 1),
                EmployeePosition.PRINCIPAL, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.BLOSSOM_SMILE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                strgGroup.getId(), "ojpark2010@abms.co", "박책임", LocalDate.of(2012, 4, 11),
                LocalDate.of(1979, 7, 1),
                EmployeePosition.PRINCIPAL, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.BLOSSOM_SMILE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2204.getId(), "needless2@abms.co", "오책임", LocalDate.of(2016, 3, 13),
                LocalDate.of(1988, 8, 8),
                EmployeePosition.PRINCIPAL, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.BLOSSOM_SMILE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2104.getId(), "springbi@abms.co", "임책임", LocalDate.of(2020, 2, 19),
                LocalDate.of(1977, 4, 4),
                EmployeePosition.PRINCIPAL, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.BLOSSOM_SMILE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2103.getId(), "dogbank@abms.co", "이책임", LocalDate.of(2009, 12, 11),
                LocalDate.of(1980, 3, 3),
                EmployeePosition.PRINCIPAL, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.BLOSSOM_SMILE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3204.getId(), "myride@abms.co", "장책임", LocalDate.of(2012, 3, 1),
                LocalDate.of(1972, 7, 1),
                EmployeePosition.PRINCIPAL, EmployeeType.FULL_TIME, EmployeeGrade.SENIOR,
                EmployeeAvatar.BLOSSOM_SMILE,
                "")
        );

        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3205.getId(), "jason0814@abms.co", "방선임", LocalDate.of(2020, 3, 1),
                LocalDate.of(1993, 7, 1),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.BLOSSOM_SMILE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2201.getId(), "sdi4931@abms.co", "신선임", LocalDate.of(2020, 3, 1),
                LocalDate.of(1996, 10, 27),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.MIDNIGHT_WINK,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2102.getId(), "wessuh11@abms.co", "서선임", LocalDate.of(2021, 9, 5),
                LocalDate.of(1995, 1, 15),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.MIDNIGHT_WINK,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2104.getId(), "woong22@abms.co", "이선임", LocalDate.of(2021, 9, 6),
                LocalDate.of(1992, 8, 20),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.MIDNIGHT_WINK,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3103.getId(), "dyd5795@abms.co", "장선임", LocalDate.of(2021, 9, 6),
                LocalDate.of(1991, 10, 14),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.MIDNIGHT_WINK,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3101.getId(), "hoonjoo@abms.co", "장선임", LocalDate.of(2025, 7, 2),
                LocalDate.of(1998, 7, 1),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.ORANGE_BURST,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2202.getId(), "3097mk@abms.co", "최선임", LocalDate.of(2024, 3, 1),
                LocalDate.of(1987, 6, 12),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.LAVENDER_MOON,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2203.getId(), "moomstone2@abms.co", "허선임", LocalDate.of(2019, 5, 10),
                LocalDate.of(1991, 1, 1),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.SAGE_GUARD,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2205.getId(), "mjs6350@abms.co", "문선임", LocalDate.of(2017, 1, 6),
                LocalDate.of(1990, 7, 1),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.LAVENDER_MOON,
                "")
        );

        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3104.getId(), "cy950315@abms.co", "오사원", LocalDate.of(2023, 11, 5),
                LocalDate.of(1995, 3, 1),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.COBALT_WAVE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3103.getId(), "dlwltn6604@abms.co", "이사원", LocalDate.of(2024, 11, 21),
                LocalDate.of(1999, 2, 1),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.COBALT_WAVE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3103.getId(), "woouk747@abms.co", "이사원", LocalDate.of(2024, 9, 21),
                LocalDate.of(1998, 2, 12),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.COBALT_WAVE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3101.getId(), "heeseokoh@abms.co", "오사원", LocalDate.of(2025, 9, 21),
                LocalDate.of(1997, 12, 8),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.COBALT_WAVE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3104.getId(), "dhflgkwls@abms.co", "윤사원", LocalDate.of(2023, 9, 21),
                LocalDate.of(1996, 4, 17),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.COBALT_WAVE,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2101.getId(), "vicent77@abms.co", "임사원", LocalDate.of(2024, 11, 21),
                LocalDate.of(1996, 9, 29),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2101.getId(), "ABC3202@abms.co", "정사원", LocalDate.of(2024, 11, 21),
                LocalDate.of(1996, 10, 31),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                mngDivision.getId(), "zyz9229@abms.co", "제사원", LocalDate.of(2022, 11, 21),
                LocalDate.of(1996, 5, 5),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2104.getId(), "rainbow2774@abms.co", "차사원", LocalDate.of(2022, 11, 21),
                LocalDate.of(1996, 6, 16),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC3201.getId(), "sychoi9089@abms.co", "최사원", LocalDate.of(2022, 11, 21),
                LocalDate.of(1996, 7, 3),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC4101.getId(), "kidsan20@abms.co", "윤사원", LocalDate.of(2023, 11, 5),
                LocalDate.of(1996, 2, 22),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                "")
        );
        employeeModifyService.create(
            createEmployeeCreateCommand(
                ABC2201.getId(), "sdi4932@abms.co", "신사원", LocalDate.of(2023, 11, 5),
                LocalDate.of(2002, 12, 7),
                EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME, EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                "")
        );

        // ---------------------------------------------------------
        // 협력사 구성
        // ---------------------------------------------------------
        Party partyNaverCloud = partyRepository.save(
            Party.create(new PartyCreateRequest(
                "네이버클라우드",
                "이준호",
                "김영업",
                "010-1234-5678",
                "sales@navercloud.com"))
        );
        Party partySamsungSDS = partyRepository.save(
            Party.create(new PartyCreateRequest(
                "삼성SDS",
                "박철수",
                "최담당",
                "010-2345-6789",
                "contact@samsungsds.com"))
        );
        Party partyKakao = partyRepository.save(
            Party.create(new PartyCreateRequest(
                "카카오",
                "정여진",
                "이매니저",
                "010-3456-7890",
                "partner@kakao.com"))
        );
        Party partyLGCNS = partyRepository.save(
            Party.create(new PartyCreateRequest(
                "LG CNS",
                "강대리",
                "송과장",
                "010-4567-8901",
                "business@lgcns.com"))
        );
        Party partySkTelecom = partyRepository.save(
            Party.create(new PartyCreateRequest(
                "SK텔레콤",
                "윤사장",
                "임대리",
                "010-5678-9012",
                "sales@sktelecom.com"))
        );

        // ---------------------------------------------------------
        // 프로젝트 구성
        // ---------------------------------------------------------
        projectRepository.save(
            Project.create(new ProjectCreateRequest(
                partyNaverCloud.getId(),
                "PROJ-2024-001",
                "네이버클라우드 ERP 시스템 구축",
                "네이버클라우드 기반 전사 ERP 시스템 개발 및 구축 프로젝트",
                ProjectStatus.IN_PROGRESS,
                580000000L,
                LocalDate.of(2024, 1, 15),
                LocalDate.of(2024, 12, 31)))
        );
        projectRepository.save(
            Project.create(new ProjectCreateRequest(
                partySamsungSDS.getId(),
                "PROJ-2024-002",
                "삼성전자 협력사 포털 개발",
                "삼성전자向 협력사 통합 관리 포털 시스템 구축",
                ProjectStatus.IN_PROGRESS,
                420000000L,
                LocalDate.of(2024, 3, 1),
                LocalDate.of(2024, 10, 31)))
        );
        projectRepository.save(
            Project.create(new ProjectCreateRequest(
                partyKakao.getId(),
                "PROJ-2024-003",
                "카카오 AI 챗봇 플랫폼",
                "LLM 기반 고객지원 챗봇 플랫폼 개발",
                ProjectStatus.COMPLETED,
                350000000L,
                LocalDate.of(2023, 9, 1),
                LocalDate.of(2024, 2, 29)))
        );
        projectRepository.save(
            Project.create(new ProjectCreateRequest(
                partyLGCNS.getId(),
                "PROJ-2024-004",
                "LG화학 데이터 웨어하우스",
                "빅데이터 기반 통합 데이터 웨어하우스 구축",
                ProjectStatus.ON_HOLD,
                820000000L,
                LocalDate.of(2024, 7, 1),
                LocalDate.of(2025, 6, 30)))
        );
        projectRepository.save(
            Project.create(new ProjectCreateRequest(
                partySkTelecom.getId(),
                "PROJ-2024-005",
                "5G IoT 플랫폼 고도화",
                "5G 기반 IoT 디바이스 통합 관리 플랫폼 고도화",
                ProjectStatus.IN_PROGRESS,
                670000000L,
                LocalDate.of(2024, 4, 1),
                LocalDate.of(2024, 11, 30)))
        );
        projectRepository.save(
            Project.create(new ProjectCreateRequest(
                partyNaverCloud.getId(),
                "PROJ-2023-008",
                "네이버페이 정산 시스템 리뉴얼",
                "차세대 정산 시스템 개발 및 레거시 마이그레이션",
                ProjectStatus.COMPLETED,
                480000000L,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31)))
        );
        projectRepository.save(
            Project.create(new ProjectCreateRequest(
                partySamsungSDS.getId(),
                "PROJ-2023-012",
                "삼성디스플레이 MES 고도화",
                "제조실행시스템(MES) 기능 개선 및 고도화",
                ProjectStatus.CANCELLED,
                390000000L,
                LocalDate.of(2023, 6, 1),
                LocalDate.of(2024, 3, 31)))
        );
        projectRepository.save(
            Project.create(new ProjectCreateRequest(
                partyKakao.getId(),
                "PROJ-2025-001",
                "카카오뱅크 디지털 플랫폼",
                "차세대 디지털 뱅킹 플랫폼 구축",
                ProjectStatus.ON_HOLD,
                920000000L,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)))
        );
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

    private EmployeeCreateCommand createEmployeeCreateCommand(Long departmentId, String email, String name, LocalDate joinDate,
                                                              LocalDate birthDate, EmployeePosition position, EmployeeType type,
                                                              EmployeeGrade grade, EmployeeAvatar avatar, String memo) {
        return EmployeeCreateCommand.builder()
            .departmentId(departmentId)
            .email(email)
            .name(name)
            .joinDate(joinDate)
            .birthDate(birthDate)
            .grade(grade)
            .position(position)
            .type(type)
            .avatar(avatar)
            .memo(memo)
            .build();
    }

}
