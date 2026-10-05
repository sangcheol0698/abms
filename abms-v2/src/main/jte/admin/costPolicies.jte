@import java.util.List
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.employee.EmployeeType
@import kr.co.abacus.abms.summary.EmployeeCostPolicy
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param List<EmployeeCostPolicy> policies
@param int currentYear
@template.layout.app(ctx = ctx, title = "원가 정책", fill = true, content = @`
!{List<Integer> years = policies.stream().map(EmployeeCostPolicy::getApplyYear).distinct().toList();}
<%-- 데스크톱: 페이지는 고정, 왼쪽 정책 목록만 스크롤하고 오른쪽 추가 패널은 고정 (fill 레이아웃). 태블릿·모바일은 문서 스크롤. --%>
<div class="flex flex-col lg:h-full">
    <div class="px-x4 pt-x5 sm:px-x6 lg:px-x8 lg:pt-x6">
    @template.components.pageHeader(title = "원가 정책", subtitle = "직원 월 원가 = 월 기본급(연봉 ÷ 12) × (1 + 제경비율 + 판관비율). 해당 연도 정책이 없으면 가장 최근 연도 정책을 적용합니다.")
    </div>

    <div class="grid min-h-0 flex-1 border-t border-stroke-neutral-subtle @5xl:grid-cols-[minmax(0,1fr)_320px]">
        <%-- 좁은 화면은 열 제목과 함께 가로 스크롤 --%>
        <section class="flex min-h-0 min-w-0 flex-col overflow-x-auto @5xl:border-r @5xl:border-stroke-neutral-subtle" aria-label="정책 목록">
                @if(policies.isEmpty())
                    @template.components.empty(message = "등록된 원가 정책이 없습니다.", icon = "calculator")
                @else
                    <div class="flex min-h-0 min-w-[560px] flex-1 flex-col">
                        <div class="list-head shrink-0 sm:px-x6 lg:px-x8"><span class="flex-1">고용유형 <span class="tabular-nums">${policies.size()}</span></span><span class="w-28 shrink-0">제경비율 (%)</span><span class="w-28 shrink-0">판관비율 (%)</span><span class="w-20 shrink-0 text-right">원가 배수</span><span class="w-24 shrink-0"></span></div>
                        <div class="min-h-0 flex-1 lg:overflow-y-auto lg:overscroll-contain" data-fill-scroll>
                        @for(Integer year : years)
                            <h3 class="list-group-header max-lg:static sm:px-x6 lg:px-x8">
                                ${Icons.of("calendar", "size-3.5")}<span class="text-fg-neutral">${year}년</span>
                                @if(year == currentYear)<span class="${Seed.badge("informative")}">올해</span>@endif
                            </h3>
                            @for(EmployeeCostPolicy p : policies.stream().filter(x -> x.getApplyYear() == year).toList())
                                !{String formId = "policy-" + p.id();}
                                <div class="list-row sm:px-x6 lg:px-x8">
                                    <span class="min-w-0 flex-1 truncate t4-medium">${p.getType().label()}</span>
                                    <span class="w-28 shrink-0"><input form="${formId}" name="overheadPercent" type="number" step="0.01" min="0" max="100" class="input filter-input w-24 tabular-nums" value="${p.getOverheadRate().movePointRight(2).stripTrailingZeros().toPlainString()}" aria-label="${year}년 ${p.getType().label()} 제경비율"></span>
                                    <span class="w-28 shrink-0"><input form="${formId}" name="sgaPercent" type="number" step="0.01" min="0" max="100" class="input filter-input w-24 tabular-nums" value="${p.getSgaRate().movePointRight(2).stripTrailingZeros().toPlainString()}" aria-label="${year}년 ${p.getType().label()} 판관비율"></span>
                                    <span class="w-20 shrink-0 text-right tabular-nums text-fg-neutral-muted">× ${java.math.BigDecimal.ONE.add(p.getOverheadRate()).add(p.getSgaRate()).stripTrailingZeros().toPlainString()}</span>
                                    <span class="flex w-24 shrink-0 justify-end gap-x1">
                                        <form id="${formId}" method="post" action="/admin/cost-policies/${p.id()}">${kr.co.abacus.abms.common.web.Csrf.field()}<button class="${Seed.button("neutralOutline", "xsmall")}">저장</button></form>
                                        <form method="post" action="/admin/cost-policies/${p.id()}/delete" hx-confirm="${p.getApplyYear()}년 ${p.getType().label()} 정책을 삭제할까요?">${kr.co.abacus.abms.common.web.Csrf.field()}
                                            <button class="${Seed.iconButton("ghost", "xsmall")} text-fg-critical" aria-label="${year}년 ${p.getType().label()} 정책 삭제" title="삭제">${Icons.of("trash", "size-4")}</button>
                                        </form>
                                    </span>
                                </div>
                            @endfor
                        @endfor
                        </div>
                    </div>
                @endif
        </section>

        <%-- 정책 추가 패널 --%>
        <aside class="border-t border-stroke-neutral-subtle px-x4 py-x5 sm:px-x6 @5xl:border-t-0 @5xl:overflow-y-auto">
            <div>
                <h2 class="section-title">정책 추가</h2>
                <form method="post" action="/admin/cost-policies" class="list-box grid grid-cols-2 gap-x3 p-x4">${kr.co.abacus.abms.common.web.Csrf.field()}
                    <div>
                        <label for="year" class="label">연도</label>
                        <input id="year" name="year" type="number" class="input" value="${currentYear + 1}" required>
                    </div>
                    <div>
                        <label for="type" class="label">고용유형</label>
                        <select id="type" name="type" class="select">
                            @for(EmployeeType t : EmployeeType.values())
                                <option value="${t.name()}">${t.label()}</option>
                            @endfor
                        </select>
                    </div>
                    <div>
                        <label for="overheadPercent" class="label">제경비율 (%)</label>
                        <input id="overheadPercent" name="overheadPercent" type="number" step="0.01" min="0" max="100" class="input" value="10" required>
                    </div>
                    <div>
                        <label for="sgaPercent" class="label">판관비율 (%)</label>
                        <input id="sgaPercent" name="sgaPercent" type="number" step="0.01" min="0" max="100" class="input" value="5" required>
                    </div>
                    <button type="submit" class="${Seed.button("brandSolid")} col-span-2">${Icons.of("plus", "size-4")} 추가</button>
                    <p class="col-span-2 t3-regular text-fg-neutral-subtle">마감된 월이 포함된 연도의 정책은 마감 해제 전까지 바꿀 수 없습니다.</p>
                </form>
            </div>
        </aside>
    </div>
</div>
`)
