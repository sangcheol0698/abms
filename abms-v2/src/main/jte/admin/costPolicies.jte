@import java.util.List
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.employee.EmployeeType
@import kr.co.abacus.abms.summary.EmployeeCostPolicy
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param List<EmployeeCostPolicy> policies
@param int currentYear
@template.layout.app(ctx = ctx, title = "원가 정책", content = @`
    @template.components.pageHeader(title = "원가 정책", subtitle = "직원 월 원가 = 월 기본급(연봉 ÷ 12) × (1 + 제경비율 + 판관비율). 해당 연도 정책이 없으면 가장 최근 연도 정책을 적용합니다.")
    !{List<Integer> years = policies.stream().map(EmployeeCostPolicy::getApplyYear).distinct().toList();}
    <div class="grid gap-x6 xl:grid-cols-[minmax(0,1fr)_300px]">
        <section class="min-w-0">
            <h2 class="section-title">정책 목록 <span class="section-count">${policies.size()}</span></h2>
            <div class="list-box overflow-x-auto">
                @if(policies.isEmpty())
                    @template.components.empty(message = "등록된 원가 정책이 없습니다.", icon = "calculator")
                @else
                    <div class="min-w-[560px]">
                        <div class="list-head"><span class="flex-1">고용유형</span><span class="w-28 shrink-0">제경비율 (%)</span><span class="w-28 shrink-0">판관비율 (%)</span><span class="w-20 shrink-0 text-right">원가 배수</span><span class="w-24 shrink-0"></span></div>
                        @for(Integer year : years)
                            <h3 class="list-group-header">
                                ${Icons.of("calendar", "size-3.5")}<span class="text-fg-neutral">${year}년</span>
                                @if(year == currentYear)<span class="${Seed.badge("informative")}">올해</span>@endif
                            </h3>
                            @for(EmployeeCostPolicy p : policies.stream().filter(x -> x.getApplyYear() == year).toList())
                                !{String formId = "policy-" + p.id();}
                                <div class="list-row">
                                    <span class="min-w-0 flex-1 truncate t4-medium">${p.getType().label()}</span>
                                    <span class="w-28 shrink-0"><input form="${formId}" name="overheadPercent" type="number" step="0.01" min="0" max="100" class="input filter-input w-24 tabular-nums" value="${p.getOverheadRate().movePointRight(2).stripTrailingZeros().toPlainString()}" aria-label="${year}년 ${p.getType().label()} 제경비율"></span>
                                    <span class="w-28 shrink-0"><input form="${formId}" name="sgaPercent" type="number" step="0.01" min="0" max="100" class="input filter-input w-24 tabular-nums" value="${p.getSgaRate().movePointRight(2).stripTrailingZeros().toPlainString()}" aria-label="${year}년 ${p.getType().label()} 판관비율"></span>
                                    <span class="w-20 shrink-0 text-right tabular-nums text-fg-neutral-muted">× ${java.math.BigDecimal.ONE.add(p.getOverheadRate()).add(p.getSgaRate()).stripTrailingZeros().toPlainString()}</span>
                                    <span class="flex w-24 shrink-0 justify-end gap-x1">
                                        <form id="${formId}" method="post" action="/admin/cost-policies/${p.id()}"><button class="${Seed.button("neutralOutline", "xsmall")}">저장</button></form>
                                        <form method="post" action="/admin/cost-policies/${p.id()}/delete" hx-confirm="${p.getApplyYear()}년 ${p.getType().label()} 정책을 삭제할까요?">
                                            <button class="${Seed.iconButton("ghost", "xsmall")} text-fg-critical" aria-label="${year}년 ${p.getType().label()} 정책 삭제" title="삭제">${Icons.of("trash", "size-4")}</button>
                                        </form>
                                    </span>
                                </div>
                            @endfor
                        @endfor
                    </div>
                @endif
            </div>
        </section>

        <%-- 정책 추가 패널 --%>
        <aside>
            <div class="xl:sticky xl:top-6">
                <h2 class="section-title">정책 추가</h2>
                <form method="post" action="/admin/cost-policies" class="list-box grid grid-cols-2 gap-x3 p-x4">
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
`)
