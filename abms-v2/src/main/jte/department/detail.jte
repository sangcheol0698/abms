@import java.util.List
@import kr.co.abacus.abms.common.domain.Money
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.summary.ProfitQueryService.MonthPoint
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param Department department
@param DepartmentTree tree
@param List<Employee> members
@param int subtreeMemberCount
@param Employee leader = null
@param List<Project> projects
@param List<MonthPoint> trend = null
@param boolean canWrite
<div class="space-y-4">
    <section class="card">
        <div class="card-body">
            <div class="flex flex-wrap items-start justify-between gap-4">
                <div>
                    <p class="text-xs text-fg-neutral-subtle">
                        @for(Department d : tree.path(department.id()))
                            @if(!d.id().equals(department.id()))${d.getName()} › @endif
                        @endfor
                    </p>
                    <h2 class="mt-1 flex items-center gap-2 text-xl font-semibold text-fg-neutral">
                        ${department.getName()} <span class="${Seed.badge("neutral")}">${department.getType().label()}</span>
                    </h2>
                    <p class="mt-1 text-sm text-fg-neutral-subtle">코드 ${department.getCode()} · 소속 ${members.size()}명 · 하위 포함 ${subtreeMemberCount}명</p>
                </div>
                @if(canWrite)
                    <div class="flex flex-wrap gap-2">
                        <button type="button" class="${Seed.button("neutralWeak", "xsmall")}" hx-get="/departments/new?parentId=${department.id()}" hx-target="#modal-body">${Icons.of("plus", "size-4")} 하위 부서</button>
                        <button type="button" class="${Seed.button("neutralWeak", "xsmall")}" hx-get="/departments/${department.id()}/edit" hx-target="#modal-body">${Icons.of("pencil", "size-4")} 수정</button>
                        <button type="button" class="${Seed.button("neutralWeak", "xsmall")}" hx-get="/departments/${department.id()}/leader" hx-target="#modal-body">${Icons.of("user", "size-4")} 부서장</button>
                        <form method="post" action="/departments/${department.id()}/delete" hx-confirm="${department.getName()} 부서를 삭제할까요?">
                            <button class="${Seed.button("ghost", "xsmall")} text-fg-critical">${Icons.of("trash", "size-4")}</button>
                        </form>
                    </div>
                @endif
            </div>
            <div class="mt-5 flex items-center gap-3 rounded-lg bg-bg-neutral-muted p-3">
                @if(leader != null)
                    @template.components.avatar(avatar = leader.getAvatar(), initial = leader.initial())
                    <div>
                        <p class="text-xs text-fg-neutral-subtle">부서장</p>
                        <a href="/employees/${leader.id()}" class="text-sm font-medium text-fg-neutral hover:text-fg-brand">${leader.getName()} <span class="font-normal text-fg-neutral-subtle">${leader.getPosition().label()}</span></a>
                    </div>
                @else
                    <p class="text-sm text-fg-neutral-subtle">지정된 부서장이 없습니다.</p>
                @endif
            </div>
        </div>
    </section>

    @if(trend != null)
        !{Money revenue = trend.stream().map(MonthPoint::revenue).reduce(Money.ZERO, Money::plus);}
        !{Money cost = trend.stream().map(MonthPoint::cost).reduce(Money.ZERO, Money::plus);}
        <section class="card">
            <div class="card-header">
                <h3 class="card-title">${java.time.Year.now().getValue()}년 손익 (하위 부서 포함, 주관 프로젝트 기준)</h3>
            </div>
            <div class="grid grid-cols-3 divide-x divide-stroke-neutral-subtle">
                <div class="px-5 py-4"><p class="stat-label">매출</p><p class="mt-1 text-lg font-semibold tabular-nums">${revenue.compact()}원</p></div>
                <div class="px-5 py-4"><p class="stat-label">비용</p><p class="mt-1 text-lg font-semibold tabular-nums">${cost.compact()}원</p></div>
                <div class="px-5 py-4"><p class="stat-label">이익</p><p class="mt-1 text-lg font-semibold tabular-nums ${Ui.signClass(revenue.minus(cost))}">${revenue.minus(cost).compact()}원</p></div>
            </div>
        </section>
    @endif

    <section class="card overflow-hidden">
        <div class="card-header"><h3 class="card-title">구성원 (${members.size()})</h3></div>
        @if(members.isEmpty())
            @template.components.empty(message = "소속 직원이 없습니다.", icon = "users")
        @else
            <ul class="grid grid-cols-1 divide-y divide-stroke-neutral-subtle sm:grid-cols-2 sm:divide-y-0">
                @for(Employee e : members)
                    <li class="flex items-center gap-3 px-5 py-3 sm:border-b sm:border-stroke-neutral-subtle">
                        @template.components.avatar(avatar = e.getAvatar(), initial = e.initial())
                        <div class="min-w-0 flex-1">
                            <a href="/employees/${e.id()}" class="text-sm font-medium text-fg-neutral hover:text-fg-brand">${e.getName()}</a>
                            <p class="text-xs text-fg-neutral-subtle">${e.getPosition().label()} · ${e.getGrade().label()}</p>
                        </div>
                        <span class="${Ui.badge(e.getStatus())}">${e.getStatus().label()}</span>
                    </li>
                @endfor
            </ul>
        @endif
    </section>

    @if(!projects.isEmpty())
        <section class="card overflow-hidden">
            <div class="card-header"><h3 class="card-title">주관 프로젝트 (하위 부서 포함)</h3></div>
            <div class="overflow-x-auto">
                <table class="table">
                    <thead><tr><th>프로젝트</th><th>주관 부서</th><th>상태</th><th>기간</th><th class="num">계약금액</th></tr></thead>
                    <tbody class="divide-y divide-stroke-neutral-subtle">
                    @for(Project p : projects)
                        <tr>
                            <td><a href="/projects/${p.id()}" class="link">${p.getName()}</a><p class="text-xs text-fg-placeholder">${p.getCode()}</p></td>
                            <td>${tree.nameOf(p.getLeadDepartmentId())}</td>
                            <td><span class="${Ui.badge(p.getStatus())}">${p.getStatus().label()}</span></td>
                            <td class="tabular-nums">${Ui.period(p.getPeriod().startDate(), p.getPeriod().endDate())}</td>
                            <td class="num">${p.getContractAmount().compact()}</td>
                        </tr>
                    @endfor
                    </tbody>
                </table>
            </div>
        </section>
    @endif
</div>
