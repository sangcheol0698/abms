package kr.co.abacus.abms.adapter.api.weeklyreport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.application.weeklyreport.dto.command.WeeklyReportGenerateCommand;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportDraftDetail;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportDraftListItem;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshotSummary;
import kr.co.abacus.abms.application.weeklyreport.inbound.WeeklyReportDraftFinder;
import kr.co.abacus.abms.application.weeklyreport.inbound.WeeklyReportDraftManager;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.grouppermissiongrant.GroupPermissionGrant;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.domain.weeklyreport.WeeklyReportStatus;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("주간 보고서 초안 API 문서화")
class WeeklyReportDraftApiDocumentationTest extends ApiIntegrationTestBase {

    private static final String USERNAME = "weekly-report-docs@abacus.co.kr";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private PermissionGroupRepository permissionGroupRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @Autowired
    private GroupPermissionGrantRepository groupPermissionGrantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private WeeklyReportDraftManager weeklyReportDraftManager;

    @MockitoBean
    private WeeklyReportDraftFinder weeklyReportDraftFinder;

    @BeforeEach
    void setUp() {
        Department department = departmentRepository.save(Department.create(
                "WEEKLY-REPORT",
                "주간보고팀",
                DepartmentType.TEAM,
                null,
                null
        ));

        Employee employee = employeeRepository.save(Employee.create(
                department.getIdOrThrow(),
                "주간보고사용자",
                USERNAME,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
        Long accountId = accountRepository.save(Account.create(
                employee.getIdOrThrow(),
                USERNAME,
                passwordEncoder.encode(PASSWORD)
        )).getIdOrThrow();

        Permission permission = permissionRepository.save(Permission.create(
                "report.read",
                "주간 보고서 조회",
                "주간 보고서 조회 권한"
        ));
        PermissionGroup permissionGroup = permissionGroupRepository.save(PermissionGroup.create(
                "주간 보고서 문서화 그룹",
                "문서화 테스트용 그룹",
                PermissionGroupType.CUSTOM
        ));
        accountGroupAssignmentRepository.save(AccountGroupAssignment.create(accountId, permissionGroup.getIdOrThrow()));
        groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                permissionGroup.getIdOrThrow(),
                permission.getIdOrThrow(),
                PermissionScope.ALL
        ));
        flushAndClear();
    }

    @Test
    @DisplayName("주간 보고서 초안 생성 API를 문서화한다")
    void createDraft() throws Exception {
        given(weeklyReportDraftManager.createDraft(anyLong(), any(WeeklyReportGenerateCommand.class)))
                .willReturn(sampleDetail(1L));
        MockHttpSession session = login();

        mockMvc.perform(post("/api/reports/weekly/drafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "weekStart", "2026-03-23",
                                "weekEnd", "2026-03-29"
                        )))
                        .session(session))
                .andDo(document("weekly-report/create",
                        requestFields(
                                fieldWithPath("weekStart").description("주간 보고서 시작일 (월요일)").optional(),
                                fieldWithPath("weekEnd").description("주간 보고서 종료일 (일요일)").optional()
                        ),
                        responseFields(
                                fieldWithPath("id").description("초안 식별자"),
                                fieldWithPath("title").description("초안 제목"),
                                fieldWithPath("weekStart").description("보고 시작일"),
                                fieldWithPath("weekEnd").description("보고 종료일"),
                                fieldWithPath("status").description("초안 상태 코드"),
                                fieldWithPath("statusDescription").description("초안 상태 설명"),
                                fieldWithPath("reportMarkdown").description("AI 생성 Markdown 초안"),
                                fieldWithPath("snapshotSummary").description("카드 표시용 핵심 요약"),
                                fieldWithPath("snapshotSummary.totalEmployees").description("총 직원 수"),
                                fieldWithPath("snapshotSummary.onLeaveEmployees").description("휴직 인원 수"),
                                fieldWithPath("snapshotSummary.inProgressProjects").description("진행 중 프로젝트 수"),
                                fieldWithPath("snapshotSummary.startedProjects").description("주간 신규 시작 프로젝트 수"),
                                fieldWithPath("snapshotSummary.endedProjects").description("주간 종료 프로젝트 수"),
                                fieldWithPath("snapshotSummary.monthlyRevenueAvailable").description("월 매출 집계 사용 가능 여부"),
                                fieldWithPath("failureReason").description("생성 실패 사유").optional(),
                                fieldWithPath("snapshotJson").description("AI 입력에 사용된 스냅샷 JSON"),
                                fieldWithPath("createdAt").description("생성 시각"),
                                fieldWithPath("updatedAt").description("수정 시각")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("주간 보고서 초안 목록 API를 문서화한다")
    void listDrafts() throws Exception {
        given(weeklyReportDraftFinder.findRecentDrafts(anyLong()))
                .willReturn(List.of(sampleSummary(1L)));
        MockHttpSession session = login();

        mockMvc.perform(get("/api/reports/weekly/drafts").session(session))
                .andDo(document("weekly-report/list",
                        responseFields(
                                fieldWithPath("[].id").description("초안 식별자"),
                                fieldWithPath("[].title").description("초안 제목"),
                                fieldWithPath("[].weekStart").description("보고 시작일"),
                                fieldWithPath("[].weekEnd").description("보고 종료일"),
                                fieldWithPath("[].status").description("초안 상태 코드"),
                                fieldWithPath("[].statusDescription").description("초안 상태 설명"),
                                fieldWithPath("[].reportMarkdown").description("AI 생성 Markdown 초안"),
                                fieldWithPath("[].snapshotSummary").description("카드 표시용 핵심 요약"),
                                fieldWithPath("[].snapshotSummary.totalEmployees").description("총 직원 수"),
                                fieldWithPath("[].snapshotSummary.onLeaveEmployees").description("휴직 인원 수"),
                                fieldWithPath("[].snapshotSummary.inProgressProjects").description("진행 중 프로젝트 수"),
                                fieldWithPath("[].snapshotSummary.startedProjects").description("주간 신규 시작 프로젝트 수"),
                                fieldWithPath("[].snapshotSummary.endedProjects").description("주간 종료 프로젝트 수"),
                                fieldWithPath("[].snapshotSummary.monthlyRevenueAvailable").description("월 매출 집계 사용 가능 여부"),
                                fieldWithPath("[].failureReason").description("생성 실패 사유").optional(),
                                fieldWithPath("[].createdAt").description("생성 시각"),
                                fieldWithPath("[].updatedAt").description("수정 시각")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("주간 보고서 초안 상세 API를 문서화한다")
    void getDraftDetail() throws Exception {
        given(weeklyReportDraftFinder.findDraftDetail(anyLong(), anyLong()))
                .willReturn(sampleDetail(1L));
        MockHttpSession session = login();

        mockMvc.perform(get("/api/reports/weekly/drafts/{draftId}", 1L).session(session))
                .andDo(document("weekly-report/detail",
                        pathParameters(
                                parameterWithName("draftId").description("조회할 초안 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").description("초안 식별자"),
                                fieldWithPath("title").description("초안 제목"),
                                fieldWithPath("weekStart").description("보고 시작일"),
                                fieldWithPath("weekEnd").description("보고 종료일"),
                                fieldWithPath("status").description("초안 상태 코드"),
                                fieldWithPath("statusDescription").description("초안 상태 설명"),
                                fieldWithPath("reportMarkdown").description("AI 생성 Markdown 초안"),
                                fieldWithPath("snapshotSummary").description("카드 표시용 핵심 요약"),
                                fieldWithPath("snapshotSummary.totalEmployees").description("총 직원 수"),
                                fieldWithPath("snapshotSummary.onLeaveEmployees").description("휴직 인원 수"),
                                fieldWithPath("snapshotSummary.inProgressProjects").description("진행 중 프로젝트 수"),
                                fieldWithPath("snapshotSummary.startedProjects").description("주간 신규 시작 프로젝트 수"),
                                fieldWithPath("snapshotSummary.endedProjects").description("주간 종료 프로젝트 수"),
                                fieldWithPath("snapshotSummary.monthlyRevenueAvailable").description("월 매출 집계 사용 가능 여부"),
                                fieldWithPath("failureReason").description("생성 실패 사유").optional(),
                                fieldWithPath("snapshotJson").description("AI 입력에 사용된 스냅샷 JSON"),
                                fieldWithPath("createdAt").description("생성 시각"),
                                fieldWithPath("updatedAt").description("수정 시각")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("주간 보고서 초안 재생성 API를 문서화한다")
    void regenerateDraft() throws Exception {
        given(weeklyReportDraftManager.regenerateDraft(anyLong(), anyLong()))
                .willReturn(sampleDetail(2L));
        MockHttpSession session = login();

        mockMvc.perform(post("/api/reports/weekly/drafts/{draftId}/regenerate", 1L).session(session))
                .andDo(document("weekly-report/regenerate",
                        pathParameters(
                                parameterWithName("draftId").description("재생성 기준이 되는 초안 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").description("새로 생성된 초안 식별자"),
                                fieldWithPath("title").description("초안 제목"),
                                fieldWithPath("weekStart").description("보고 시작일"),
                                fieldWithPath("weekEnd").description("보고 종료일"),
                                fieldWithPath("status").description("초안 상태 코드"),
                                fieldWithPath("statusDescription").description("초안 상태 설명"),
                                fieldWithPath("reportMarkdown").description("AI 생성 Markdown 초안"),
                                fieldWithPath("snapshotSummary").description("카드 표시용 핵심 요약"),
                                fieldWithPath("snapshotSummary.totalEmployees").description("총 직원 수"),
                                fieldWithPath("snapshotSummary.onLeaveEmployees").description("휴직 인원 수"),
                                fieldWithPath("snapshotSummary.inProgressProjects").description("진행 중 프로젝트 수"),
                                fieldWithPath("snapshotSummary.startedProjects").description("주간 신규 시작 프로젝트 수"),
                                fieldWithPath("snapshotSummary.endedProjects").description("주간 종료 프로젝트 수"),
                                fieldWithPath("snapshotSummary.monthlyRevenueAvailable").description("월 매출 집계 사용 가능 여부"),
                                fieldWithPath("failureReason").description("생성 실패 사유").optional(),
                                fieldWithPath("snapshotJson").description("AI 입력에 사용된 스냅샷 JSON"),
                                fieldWithPath("createdAt").description("생성 시각"),
                                fieldWithPath("updatedAt").description("수정 시각")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("주간 보고서 초안 중지 API를 문서화한다")
    void cancelDraft() throws Exception {
        given(weeklyReportDraftManager.cancelDraft(anyLong(), anyLong()))
                .willReturn(sampleCancelledDetail(1L));
        MockHttpSession session = login();

        mockMvc.perform(post("/api/reports/weekly/drafts/{draftId}/cancel", 1L).session(session))
                .andDo(document("weekly-report/cancel",
                        pathParameters(
                                parameterWithName("draftId").description("중지할 초안 ID")
                        ),
                        responseFields(
                                fieldWithPath("id").description("초안 식별자"),
                                fieldWithPath("title").description("초안 제목"),
                                fieldWithPath("weekStart").description("보고 시작일"),
                                fieldWithPath("weekEnd").description("보고 종료일"),
                                fieldWithPath("status").description("초안 상태 코드"),
                                fieldWithPath("statusDescription").description("초안 상태 설명"),
                                fieldWithPath("reportMarkdown").description("AI 생성 Markdown 초안"),
                                fieldWithPath("snapshotSummary").description("카드 표시용 핵심 요약"),
                                fieldWithPath("snapshotSummary.totalEmployees").description("총 직원 수"),
                                fieldWithPath("snapshotSummary.onLeaveEmployees").description("휴직 인원 수"),
                                fieldWithPath("snapshotSummary.inProgressProjects").description("진행 중 프로젝트 수"),
                                fieldWithPath("snapshotSummary.startedProjects").description("주간 신규 시작 프로젝트 수"),
                                fieldWithPath("snapshotSummary.endedProjects").description("주간 종료 프로젝트 수"),
                                fieldWithPath("snapshotSummary.monthlyRevenueAvailable").description("월 매출 집계 사용 가능 여부"),
                                fieldWithPath("failureReason").description("생성 실패 사유").optional(),
                                fieldWithPath("snapshotJson").description("AI 입력에 사용된 스냅샷 JSON"),
                                fieldWithPath("createdAt").description("생성 시각"),
                                fieldWithPath("updatedAt").description("수정 시각")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private MockHttpSession login() throws Exception {
        return (MockHttpSession) mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", USERNAME,
                                "password", PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }

    private WeeklyReportDraftListItem sampleSummary(Long id) {
        return new WeeklyReportDraftListItem(
                id,
                "주간 운영 보고서 (2026-03-23 ~ 2026-03-29)",
                LocalDate.of(2026, 3, 23),
                LocalDate.of(2026, 3, 29),
                WeeklyReportStatus.DRAFT,
                "# 이번 주 요약\n- 초안",
                new WeeklyReportSnapshotSummary(30, 2, 8, 1, 1, true),
                null,
                LocalDateTime.of(2026, 3, 30, 9, 0),
                LocalDateTime.of(2026, 3, 30, 9, 5)
        );
    }

    private WeeklyReportDraftDetail sampleDetail(Long id) {
        WeeklyReportDraftListItem summary = id == 1L
                ? new WeeklyReportDraftListItem(
                        1L,
                        "주간 운영 보고서 (2026-03-23 ~ 2026-03-29)",
                        LocalDate.of(2026, 3, 23),
                        LocalDate.of(2026, 3, 29),
                        WeeklyReportStatus.PENDING,
                        "",
                        new WeeklyReportSnapshotSummary(0, 0, 0, 0, 0, false),
                        null,
                        LocalDateTime.of(2026, 3, 30, 9, 0),
                        LocalDateTime.of(2026, 3, 30, 9, 1)
                )
                : new WeeklyReportDraftListItem(
                        id,
                        "주간 운영 보고서 (2026-03-23 ~ 2026-03-29)",
                        LocalDate.of(2026, 3, 23),
                        LocalDate.of(2026, 3, 29),
                        WeeklyReportStatus.PENDING,
                        "",
                        new WeeklyReportSnapshotSummary(0, 0, 0, 0, 0, false),
                        null,
                        LocalDateTime.of(2026, 3, 30, 9, 0),
                        LocalDateTime.of(2026, 3, 30, 9, 1)
                );
        return new WeeklyReportDraftDetail(
                summary.id(),
                summary.title(),
                summary.weekStart(),
                summary.weekEnd(),
                summary.status(),
                summary.reportMarkdown(),
                summary.snapshotSummary(),
                "",
                summary.failureReason(),
                summary.createdAt(),
                summary.updatedAt()
        );
    }

    private WeeklyReportDraftDetail sampleCancelledDetail(Long id) {
        return new WeeklyReportDraftDetail(
                id,
                "주간 운영 보고서 (2026-03-23 ~ 2026-03-29)",
                LocalDate.of(2026, 3, 23),
                LocalDate.of(2026, 3, 29),
                WeeklyReportStatus.CANCELLED,
                "",
                new WeeklyReportSnapshotSummary(0, 0, 0, 0, 0, false),
                "",
                null,
                LocalDateTime.of(2026, 3, 30, 9, 0),
                LocalDateTime.of(2026, 3, 30, 9, 1)
        );
    }
}
