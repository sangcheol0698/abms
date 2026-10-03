@import java.util.List
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.Ui.SelectOption
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.employee.EmployeePosition
@import kr.co.abacus.abms.employee.EmployeeSearchForm
@import kr.co.abacus.abms.employee.EmployeeStatus
@import kr.co.abacus.abms.employee.EmployeeType
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param PageView<Employee> page
@param DepartmentTree tree
@param EmployeeSearchForm search
@param List<SelectOption> departmentOptions
@template.layout.app(ctx = ctx, title = "직원", content = @`
    @template.components.pageHeader(title = "직원", subtitle = "임직원 디렉터리 · 상세 정보는 권한 범위 안에서만 조회됩니다.", actions = @`
        @if(ctx.can(PermissionCode.EMPLOYEE_EXPORT))
            <a href="/employees/export${search.exportQuery()}" hx-boost="false" class="${Seed.button("neutralOutline")}">${Icons.of("download", "size-4")} CSV 내보내기</a>
        @endif
        @if(ctx.can(PermissionCode.EMPLOYEE_WRITE))
            <a href="/employees/new" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}">${Icons.of("plus", "size-4")} 직원 등록</a>
        @endif
    `)

    <div class="-mx-x4 border-t border-stroke-neutral-subtle sm:-mx-x6 lg:-mx-x8">
        <form class="view-bar sm:px-x6 lg:px-x8" action="/employees" method="get"
              hx-get="/employees" hx-target="#employee-results" hx-push-url="true"
              hx-trigger="input changed delay:300ms from:input[name=q], change from:select, change from:input[type=radio], change from:input[type=checkbox], submit">
            <div class="flex flex-wrap items-center gap-x1" role="radiogroup" aria-label="상태">
                <label class="chip"><input type="radio" name="status" value="" checked="${search.status() == null}">전체</label>
                @for(EmployeeStatus s : EmployeeStatus.values())
                    <label class="chip"><input type="radio" name="status" value="${s.name()}" checked="${s == search.status()}">${s.label()}</label>
                @endfor
                @if(ctx.can(PermissionCode.EMPLOYEE_WRITE))
                    <label class="chip"><input type="checkbox" name="deleted" value="true" checked="${Boolean.TRUE.equals(search.deleted())}">${Icons.of("trash", "size-3.5")}삭제됨</label>
                @endif
            </div>
            <div class="ml-auto flex flex-wrap items-center gap-x2">
                <div class="relative">
                    <span class="pointer-events-none absolute inset-y-0 left-2.5 flex items-center text-fg-placeholder">${Icons.of("search", "size-3.5")}</span>
                    <input type="search" name="q" value="${Ui.value(search.q())}" placeholder="이름 또는 이메일" class="input filter-input w-48 pl-x8" aria-label="검색어">
                </div>
                <select name="departmentId" class="select filter-select" aria-label="부서">
                    <option value="">전체 부서</option>
                    @for(SelectOption option : departmentOptions)
                        <option value="${option.value()}" selected="${option.value().equals(Ui.value(search.departmentId()))}">${option.label()}</option>
                    @endfor
                </select>
                <select name="type" class="select filter-select" aria-label="고용유형">
                    <option value="">전체 고용유형</option>
                    @for(EmployeeType t : EmployeeType.values())
                        <option value="${t.name()}" selected="${t == search.type()}">${t.label()}</option>
                    @endfor
                </select>
                <select name="position" class="select filter-select" aria-label="직급">
                    <option value="">전체 직급</option>
                    @for(EmployeePosition p : EmployeePosition.values())
                        <option value="${p.name()}" selected="${p == search.position()}">${p.label()}</option>
                    @endfor
                </select>
            </div>
        </form>
        <div id="employee-results">
            @template.employee.results(page = page, tree = tree, search = search)
        </div>
    </div>
`)
