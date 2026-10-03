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
@param ViewContext ctx
@param PageView<Employee> page
@param DepartmentTree tree
@param EmployeeSearchForm search
@param List<SelectOption> departmentOptions
@template.layout.app(ctx = ctx, title = "직원", content = @`
    @template.components.pageHeader(title = "직원", subtitle = "임직원 디렉터리 · 상세 정보는 권한 범위 안에서만 조회됩니다.", actions = @`
        @if(ctx.can(PermissionCode.EMPLOYEE_EXPORT))
            <a href="/employees/export${search.exportQuery()}" hx-boost="false" class="btn btn-secondary">${Icons.of("download", "size-4")} CSV 내보내기</a>
        @endif
        @if(ctx.can(PermissionCode.EMPLOYEE_WRITE))
            <a href="/employees/new" class="btn btn-primary">${Icons.of("plus", "size-4")} 직원 등록</a>
        @endif
    `)

    <div class="card">
        <form class="grid grid-cols-1 gap-3 border-b border-slate-100 p-4 sm:grid-cols-2 lg:grid-cols-6" action="/employees" method="get"
              hx-get="/employees" hx-target="#employee-results" hx-push-url="true"
              hx-trigger="input changed delay:300ms from:input[name=q], change from:select, change from:input[type=checkbox], submit">
            <div class="relative lg:col-span-2">
                <span class="pointer-events-none absolute inset-y-0 left-3 flex items-center text-slate-400">${Icons.of("search", "size-4")}</span>
                <input type="search" name="q" value="${Ui.value(search.q())}" placeholder="이름 또는 이메일 검색" class="input pl-9" aria-label="검색어">
            </div>
            <select name="departmentId" class="select" aria-label="부서">
                <option value="">전체 부서</option>
                @for(SelectOption option : departmentOptions)
                    <option value="${option.value()}" selected="${option.value().equals(Ui.value(search.departmentId()))}">${option.label()}</option>
                @endfor
            </select>
            <select name="status" class="select" aria-label="상태">
                <option value="">전체 상태</option>
                @for(EmployeeStatus s : EmployeeStatus.values())
                    <option value="${s.name()}" selected="${s == search.status()}">${s.label()}</option>
                @endfor
            </select>
            <select name="type" class="select" aria-label="고용유형">
                <option value="">전체 고용유형</option>
                @for(EmployeeType t : EmployeeType.values())
                    <option value="${t.name()}" selected="${t == search.type()}">${t.label()}</option>
                @endfor
            </select>
            <select name="position" class="select" aria-label="직급">
                <option value="">전체 직급</option>
                @for(EmployeePosition p : EmployeePosition.values())
                    <option value="${p.name()}" selected="${p == search.position()}">${p.label()}</option>
                @endfor
            </select>
            @if(ctx.can(PermissionCode.EMPLOYEE_WRITE))
                <label class="flex items-center gap-2 text-sm text-slate-600 lg:col-span-6">
                    <input type="checkbox" name="deleted" value="true" class="checkbox" checked="${Boolean.TRUE.equals(search.deleted())}">
                    삭제된 직원 보기
                </label>
            @endif
        </form>
        <div id="employee-results">
            @template.employee.results(page = page, tree = tree, search = search)
        </div>
    </div>
`)
