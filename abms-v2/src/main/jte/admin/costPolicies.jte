@import java.util.List
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.employee.EmployeeType
@import kr.co.abacus.abms.summary.EmployeeCostPolicy
@param ViewContext ctx
@param List<EmployeeCostPolicy> policies
@param int currentYear
@template.layout.app(ctx = ctx, title = "원가 정책", content = @`
    @template.components.pageHeader(title = "원가 정책", subtitle = "직원 월 원가 = 월 기본급(연봉 ÷ 12) × (1 + 제경비율 + 판관비율). 해당 연도 정책이 없으면 가장 최근 연도 정책을 적용합니다.")
    <div class="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <section class="card overflow-hidden xl:col-span-2">
            <div class="card-header"><h2 class="card-title">정책 목록</h2></div>
            @if(policies.isEmpty())
                @template.components.empty(message = "등록된 원가 정책이 없습니다.", icon = "calculator")
            @else
                <div class="overflow-x-auto">
                    <table class="table">
                        <thead><tr><th>연도</th><th>고용유형</th><th>제경비율 (%)</th><th>판관비율 (%)</th><th>원가 배수</th><th></th></tr></thead>
                        <tbody class="divide-y divide-slate-100">
                        @for(EmployeeCostPolicy p : policies)
                            !{String formId = "policy-" + p.id();}
                            <tr>
                                <td class="font-medium">${p.getApplyYear()}</td>
                                <td>${p.getType().label()}</td>
                                <td><input form="${formId}" name="overheadPercent" type="number" step="0.01" min="0" max="100" class="input !w-24" value="${p.getOverheadRate().movePointRight(2).stripTrailingZeros().toPlainString()}" aria-label="제경비율"></td>
                                <td><input form="${formId}" name="sgaPercent" type="number" step="0.01" min="0" max="100" class="input !w-24" value="${p.getSgaRate().movePointRight(2).stripTrailingZeros().toPlainString()}" aria-label="판관비율"></td>
                                <td class="tabular-nums text-slate-500">× ${java.math.BigDecimal.ONE.add(p.getOverheadRate()).add(p.getSgaRate()).stripTrailingZeros().toPlainString()}</td>
                                <td class="text-right">
                                    <form id="${formId}" method="post" action="/admin/cost-policies/${p.id()}" class="inline"><button class="btn btn-ghost btn-sm">저장</button></form>
                                    <form method="post" action="/admin/cost-policies/${p.id()}/delete" class="inline" hx-confirm="${p.getApplyYear()}년 ${p.getType().label()} 정책을 삭제할까요?">
                                        <button class="btn btn-danger-ghost btn-sm" aria-label="삭제">${Icons.of("trash", "size-4")}</button>
                                    </form>
                                </td>
                            </tr>
                        @endfor
                        </tbody>
                    </table>
                </div>
            @endif
        </section>
        <section class="card h-fit">
            <div class="card-header"><h2 class="card-title">정책 추가</h2></div>
            <form method="post" action="/admin/cost-policies" class="card-body grid grid-cols-2 gap-4">
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
                <button type="submit" class="btn btn-primary col-span-2">${Icons.of("plus", "size-4")} 추가</button>
            </form>
        </section>
    </div>
`)
