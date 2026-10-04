@import kr.co.abacus.abms.common.web.PageView
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.employee.EmployeeSearchForm
@import kr.co.abacus.abms.common.web.Seed
@param PageView<Employee> page
@param DepartmentTree tree
@param EmployeeSearchForm search
@if(page.isEmpty())
    @template.components.empty(message = "조건에 맞는 직원이 없습니다.", icon = "users")
@else
    @for(Employee e : page.content())
        <a href="/employees/${e.id()}" class="list-row sm:px-x6 lg:px-x8">
            @template.components.avatar(avatar = e.getAvatar(), initial = e.initial(), size = "size-6 text-[11px]")
            <span class="flex min-w-0 flex-1 items-baseline gap-x2">
                <span class="truncate t4-medium">${e.getName()}</span>
                <span class="hidden truncate t3-regular text-fg-neutral-subtle @xl:inline">${e.isDeleted() ? e.originalEmail() : e.getEmail()}</span>
            </span>
            <span class="list-cell-muted hidden w-40 @2xl:block">${tree.nameOf(e.getDepartmentId())}</span>
            <span class="list-cell-muted hidden w-28 @4xl:block">${e.getJob() == null ? "-" : e.getJob().label()}</span>
            <span class="list-cell-muted hidden w-24 @3xl:block">${e.getPosition().label()} · ${e.getGrade().label()}</span>
            <span class="hidden w-20 @3xl:block"><span class="${Ui.badge(e.getType())}">${e.getType().label()}</span></span>
            <span class="w-16 shrink-0">
                @if(e.isDeleted())
                    <span class="${Seed.badge("critical")}">삭제됨</span>
                @else
                    <span class="${Ui.badge(e.getStatus())}">${e.getStatus().label()}</span>
                @endif
            </span>
            <span class="list-cell-muted hidden w-24 text-right tabular-nums @5xl:block">${Ui.date(e.getJoinDate())}</span>
        </a>
    @endfor
@endif
<div class="sm:px-x2 lg:px-x4">@template.components.pagination(page = page, target = "#employee-results")</div>
