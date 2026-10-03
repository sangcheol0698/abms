@import java.util.List
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.domain.Money
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.party.Party
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.project.ProjectSections.RevenueSection
@import kr.co.abacus.abms.project.ProjectSections.StaffingSection
@import kr.co.abacus.abms.project.ProjectStatus
@import kr.co.abacus.abms.summary.MonthlyRevenueSummary
@param ViewContext ctx
@param Project project
@param Party party
@param DepartmentTree tree
@param boolean canWrite
@param RevenueSection revenue
@param StaffingSection staffing
@param List<MonthlyRevenueSummary> history
@param boolean showHistory
@template.layout.app(ctx = ctx, title = project.getName(), content = @`
    @template.components.pageHeader(title = project.getName(), subtitle = project.getCode(), breadcrumb = @`
        <a href="/projects" class="hover:text-slate-700">프로젝트</a>${Icons.of("chevron-right", "size-3")}<span>${project.getName()}</span>
    `, actions = @`
        @if(canWrite)
            <a href="/projects/${project.id()}/edit" class="btn btn-secondary">${Icons.of("pencil", "size-4")} 수정</a>
            <details class="relative" data-dropdown>
                <summary class="btn btn-secondary">더보기 ${Icons.of("chevron-down", "size-4")}</summary>
                <div class="dropdown-panel w-44 py-1.5">
                    @if(!project.getStatus().isClosed())
                        <form method="post" action="/projects/${project.id()}/complete" hx-confirm="프로젝트를 완료 처리할까요?">
                            <button class="block w-full px-4 py-2 text-left text-sm hover:bg-slate-50">완료 처리</button>
                        </form>
                        <form method="post" action="/projects/${project.id()}/cancel" hx-confirm="프로젝트를 취소 처리할까요?">
                            <button class="block w-full px-4 py-2 text-left text-sm hover:bg-slate-50">취소 처리</button>
                        </form>
                    @endif
                    <form method="post" action="/projects/${project.id()}/delete" hx-confirm="프로젝트와 매출 계획, 투입 정보를 모두 삭제할까요?">
                        <button class="block w-full px-4 py-2 text-left text-sm text-rose-600 hover:bg-rose-50">삭제</button>
                    </form>
                </div>
            </details>
        @endif
    `)

    <section class="card">
        <dl class="grid grid-cols-2 divide-slate-100 sm:grid-cols-3 lg:grid-cols-6 lg:divide-x">
            <div class="px-5 py-4"><dt class="stat-label">상태</dt><dd class="mt-1.5"><span class="${Ui.badge(project.getStatus())}">${project.getStatus().label()}</span></dd></div>
            <div class="px-5 py-4"><dt class="stat-label">협력사</dt><dd class="mt-1 text-sm font-medium">
                @if(ctx.can(PermissionCode.PARTY_READ))<a class="link" href="/parties/${party.id()}">${party.getName()}</a>@else${party.getName()}@endif
            </dd></div>
            <div class="px-5 py-4"><dt class="stat-label">주관 부서</dt><dd class="mt-1 text-sm font-medium"><a class="link" href="/departments?selected=${project.getLeadDepartmentId()}">${tree.nameOf(project.getLeadDepartmentId())}</a></dd></div>
            <div class="px-5 py-4 lg:col-span-2"><dt class="stat-label">기간</dt><dd class="mt-1 text-sm font-medium tabular-nums">${Ui.period(project.getPeriod().startDate(), project.getPeriod().endDate())}</dd></div>
            <div class="px-5 py-4"><dt class="stat-label">계약금액</dt><dd class="mt-1 text-sm font-semibold tabular-nums">${Ui.won(project.getContractAmount())}원</dd></div>
        </dl>
        @if(project.getDescription() != null)
            <p class="border-t border-slate-100 px-5 py-3 text-sm text-slate-600">${project.getDescription()}</p>
        @endif
    </section>

    <section class="card mt-4" id="revenue-section">
        @template.project.revenueSection(section = revenue)
    </section>

    <section class="card mt-4" id="staffing-section">
        @template.project.staffingSection(section = staffing)
    </section>

    @if(showHistory)
        <section class="card mt-4 overflow-hidden">
            <div class="card-header">
                <h2 class="card-title">월별 손익 집계</h2>
                <span class="text-xs text-slate-500">매일 새벽 자동 집계 · 손익 현황에서 재집계할 수 있습니다.</span>
            </div>
            @if(history.isEmpty())
                @template.components.empty(message = "아직 집계된 손익이 없습니다.", icon = "chart")
            @else
                !{Money totalRevenue = history.stream().map(MonthlyRevenueSummary::getRevenueAmount).reduce(Money.ZERO, Money::plus);}
                !{Money totalCost = history.stream().map(MonthlyRevenueSummary::getCostAmount).reduce(Money.ZERO, Money::plus);}
                <div class="overflow-x-auto">
                    <table class="table">
                        <thead><tr><th>월</th><th class="num">매출</th><th class="num">비용</th><th class="num">이익</th><th>집계 시각</th></tr></thead>
                        <tbody class="divide-y divide-slate-100">
                        @for(MonthlyRevenueSummary s : history)
                            <tr>
                                <td><a class="link" href="/summary?month=${java.time.YearMonth.from(s.getTargetMonth()).toString()}">${Ui.month(java.time.YearMonth.from(s.getTargetMonth()))}</a></td>
                                <td class="num">${Ui.won(s.getRevenueAmount())}</td>
                                <td class="num">${Ui.won(s.getCostAmount())}</td>
                                <td class="num font-semibold ${Ui.signClass(s.getProfitAmount())}">${Ui.won(s.getProfitAmount())}</td>
                                <td class="text-xs text-slate-500">${Ui.dateTime(s.getCalculatedAt())}</td>
                            </tr>
                        @endfor
                        </tbody>
                        <tfoot class="bg-slate-50 text-sm font-semibold">
                            <tr>
                                <td class="px-4 py-3">합계</td>
                                <td class="px-4 py-3 text-right tabular-nums">${Ui.won(totalRevenue)}</td>
                                <td class="px-4 py-3 text-right tabular-nums">${Ui.won(totalCost)}</td>
                                <td class="px-4 py-3 text-right tabular-nums ${Ui.signClass(totalRevenue.minus(totalCost))}">${Ui.won(totalRevenue.minus(totalCost))}</td>
                                <td></td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            @endif
        </section>
    @endif
`)
