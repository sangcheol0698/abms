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
<div>
    <div class="min-w-0 space-y-6">
        <div class="flex flex-wrap items-start justify-between gap-x3">
            <div class="min-w-0">
                <p class="truncate t3-regular text-fg-neutral-subtle">
                    @for(Department d : tree.path(department.id()))
                        @if(!d.id().equals(department.id()))${d.getName()} › @endif
                    @endfor
                </p>
                <h2 class="mt-x1 flex items-center gap-x2 t7-bold text-fg-neutral">
                    ${department.getName()} <span class="${Seed.badge("neutral")}">${department.getType().label()}</span>
                </h2>
            </div>
            @if(canWrite)
                <div class="flex flex-wrap items-center gap-x1_5">
                    <button type="button" class="${Seed.button("neutralOutline", "xsmall")}" hx-get="/departments/new?parentId=${department.id()}" hx-target="#modal-body">${Icons.of("plus", "size-4")} 하위 부서</button>
                    <button type="button" class="${Seed.button("neutralOutline", "xsmall")}" hx-get="/departments/${department.id()}/edit" hx-target="#modal-body">${Icons.of("pencil", "size-4")} 수정</button>
                    <button type="button" class="${Seed.button("neutralOutline", "xsmall")}" hx-get="/departments/${department.id()}/leader" hx-target="#modal-body">${Icons.of("user", "size-4")} 부서장</button>
                    <form method="post" action="/departments/${department.id()}/delete" hx-confirm="${department.getName()} 부서를 삭제할까요?">
                        <button class="${Seed.iconButton("ghost", "xsmall")} text-fg-critical" aria-label="부서 삭제" title="부서 삭제">${Icons.of("trash", "size-4")}</button>
                    </form>
                </div>
            @endif
        </div>

        @if(trend != null)
            !{Money revenue = trend.stream().map(MonthPoint::revenue).reduce(Money.ZERO, Money::plus);}
            !{Money cost = trend.stream().map(MonthPoint::cost).reduce(Money.ZERO, Money::plus);}
            <section>
                <h3 class="section-title">${java.time.Year.now().getValue()}년 손익 <span class="section-count">하위 부서 포함 · 주관 프로젝트 기준</span></h3>
                <div class="list-box grid grid-cols-3 divide-x divide-stroke-neutral-subtle">
                    <div class="px-x4 py-x3"><p class="stat-label">매출</p><p class="mt-x1 t6-bold tabular-nums">${revenue.compact()}원</p></div>
                    <div class="px-x4 py-x3"><p class="stat-label">비용</p><p class="mt-x1 t6-bold tabular-nums">${cost.compact()}원</p></div>
                    <div class="px-x4 py-x3"><p class="stat-label">이익</p><p class="mt-x1 t6-bold tabular-nums ${Ui.signClass(revenue.minus(cost))}">${revenue.minus(cost).compact()}원</p></div>
                </div>
            </section>
        @endif

        <section>
            <h3 class="section-title">구성원 <span class="section-count">${members.size()}</span></h3>
            @if(members.isEmpty())
                <div class="list-box">@template.components.empty(message = "소속 직원이 없습니다.", icon = "users")</div>
            @else
                <div class="list-box">
                    @for(Employee e : members)
                        <a href="/employees/${e.id()}" class="list-row">
                            @template.components.avatar(avatar = e.getAvatar(), initial = e.initial(), size = "size-6 text-[11px]")
                            <span class="min-w-0 flex-1 truncate t4-medium">${e.getName()}</span>
                            <span class="list-cell-muted hidden w-28 @xl:block">${e.getPosition().label()} · ${e.getGrade().label()}</span>
                            <span class="w-14 shrink-0"><span class="${Ui.badge(e.getStatus())}">${e.getStatus().label()}</span></span>
                        </a>
                    @endfor
                </div>
            @endif
        </section>

        @if(!projects.isEmpty())
            <section>
                <h3 class="section-title">주관 프로젝트 <span class="section-count">${projects.size()} · 하위 부서 포함</span></h3>
                <div class="list-box">
                    @for(Project p : projects)
                        @template.components.projectRow(project = p, tree = tree)
                    @endfor
                </div>
            </section>
        @endif
    </div>

</div>
