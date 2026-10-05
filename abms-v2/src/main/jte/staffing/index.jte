@import java.math.BigDecimal
@import java.util.List
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.employee.EmployeeInsight
@import kr.co.abacus.abms.employee.EmployeeJob
@import kr.co.abacus.abms.site.Site
@import kr.co.abacus.abms.staffing.AvailabilityService.Candidate
@import kr.co.abacus.abms.staffing.AvailabilityService.Criteria
@param ViewContext ctx
@param Criteria criteria
@param List<Site> sites
@param List<Candidate> candidates
@param String error
@template.layout.app(ctx = ctx, title = "인력 찾기", fill = true, content = @`
<div class="flex flex-col lg:h-full">
    <div class="px-x4 pt-x5 sm:px-x6 lg:px-x8 lg:pt-x6">
    @template.components.pageHeader(title = "인력 찾기", subtitle = "기간 동안의 투입 계획을 합산해 여유가 있는 재직자를 찾습니다. 1.0 M/M = 한 달 전부 투입.")
    </div>

    <div class="flex min-h-0 flex-1 flex-col border-t border-stroke-neutral-subtle">
        <div class="z-10 shrink-0 bg-bg-layer-default md:sticky md:top-(--sticky-top)">
        <form class="view-bar sm:px-x6 lg:px-x8" action="/staffing" method="get" hx-push-url="true">
            <div class="flex flex-wrap items-center gap-x2">
                <input type="month" name="from" value="${criteria.from().toString()}" class="input filter-input w-36" aria-label="시작 월">
                <span class="t3-regular text-fg-neutral-subtle">~</span>
                <input type="month" name="to" value="${criteria.to().toString()}" class="input filter-input w-36" aria-label="끝 월">
                <label class="chip"><input type="checkbox" name="onlyAvailable" value="true" checked="${criteria.onlyAvailable()}">여유 있는 사람만</label>
            </div>
            <div class="ml-auto flex flex-wrap items-center gap-x2">
                <input type="search" name="skills" value="${Ui.value(criteria.skills())}" placeholder="기술 (쉼표로 여러 개)" class="input filter-input w-48" aria-label="보유 기술">
                <select name="job" class="select filter-select" aria-label="직무">
                    <option value="">전체 직무</option>
                    @for(EmployeeJob j : EmployeeJob.values())
                        <option value="${j.name()}" selected="${j == criteria.job()}">${j.label()}</option>
                    @endfor
                </select>
                <select name="minCareer" class="select filter-select" aria-label="최소 경력">
                    <option value="">경력 무관</option>
                    @for(int y : new int[]{1, 3, 5, 7, 10, 15})
                        <option value="${y}" selected="${criteria.minCareerYears() != null && criteria.minCareerYears() == y}">${y}년 이상</option>
                    @endfor
                </select>
                <select name="siteId" class="select filter-select" aria-label="사업장">
                    <option value="">전체 사업장</option>
                    @for(Site s : sites)
                        <option value="${s.id()}" selected="${s.id().equals(criteria.siteId())}">${s.getName()}</option>
                    @endfor
                </select>
                <button type="submit" class="${Seed.button("neutralOutline")}">${Icons.of("search", "size-4")} 찾기</button>
            </div>
        </form>
        <div class="list-head sm:px-x6 lg:px-x8" aria-hidden="true">
            <span class="min-w-0 flex-1 pl-9">직원 <span class="tabular-nums">${candidates.size()}</span></span>
            <span class="hidden w-24 @3xl:block">직무</span>
            <span class="hidden w-20 @4xl:block">경력</span>
            <span class="hidden w-56 @2xl:block">월별 투입</span>
            <span class="w-20 shrink-0 text-right">가용</span>
            <span class="hidden w-56 @5xl:block">투입 중 프로젝트</span>
        </div>
        </div>
        <div class="min-h-0 flex-1 lg:overflow-y-auto lg:overflow-x-hidden lg:overscroll-y-contain" data-fill-scroll>
            @if(error != null)
                @template.components.empty(message = error, icon = "calendar")
            @elseif(candidates.isEmpty())
                @template.components.empty(message = "조건에 맞는 재직자가 없습니다.", icon = "users")
            @else
                @for(Candidate c : candidates)
                    <a href="/employees/${c.employee().id()}" class="list-row sm:px-x6 lg:px-x8">
                        <span class="flex min-w-0 flex-1 items-center gap-x2">
                            @template.components.avatar(photoUrl = c.employee().photoUrl(), name = c.employee().getName(), size = "size-7")
                            <span class="min-w-0">
                                <span class="block truncate t4-medium">${c.employee().getName()}</span>
                                <span class="block truncate t2-regular text-fg-neutral-subtle">${c.departmentName()}@if(c.site() != null) · ${c.site().getName()}@endif</span>
                            </span>
                        </span>
                        <span class="list-cell-muted hidden w-24 truncate @3xl:block">${c.employee().getJob() == null ? "-" : c.employee().getJob().label()}</span>
                        <span class="list-cell-muted hidden w-20 @4xl:block">${EmployeeInsight.years(c.careerMonths())}</span>
                        <span class="hidden w-56 items-end gap-0.5 @2xl:flex" title="기간 평균 ${c.load().toPlainString()} M/M · 최대 ${c.peak().toPlainString()} M/M">
                            @for(int i = 0; i < c.monthly().size(); i++)
                                !{BigDecimal mm = c.monthly().get(i); int pct = Math.min(100, mm.multiply(BigDecimal.valueOf(100)).intValue());}
                                <span class="flex h-6 flex-1 items-end rounded-r0_5 bg-bg-neutral-weak" title="${criteria.from().plusMonths(i).toString()} ${mm.toPlainString()} M/M">
                                    <span class="w-full rounded-r0_5 ${mm.compareTo(BigDecimal.ONE) > 0 ? "bg-bg-critical-solid" : "bg-bg-brand-solid"}" style="height: ${pct}%"></span>
                                </span>
                            @endfor
                        </span>
                        <span class="w-20 shrink-0 text-right">
                            <span class="block t4-bold tabular-nums ${c.free().signum() > 0 ? "text-fg-positive" : "text-fg-neutral-subtle"}">${c.free().toPlainString()}</span>
                            <span class="block t2-regular text-fg-neutral-subtle tabular-nums">투입 ${c.loadPercent()}%</span>
                        </span>
                        <span class="list-cell-muted hidden w-56 truncate @5xl:block" title="${String.join(", ", c.projects())}">${c.projects().isEmpty() ? "-" : String.join(", ", c.projects())}</span>
                    </a>
                @endfor
            @endif
        </div>
    </div>
</div>
`)
