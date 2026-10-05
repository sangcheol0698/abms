@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.account.Account
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param List<Account> accounts
@param Map<Long, Employee> employees
@param Map<Long, List<String>> accountGroups
@param DepartmentTree tree
@param Long issueEmployeeId = null
@template.layout.app(ctx = ctx, title = "계정 관리", fill = true, content = @`
<%-- 데스크톱: 페이지는 고정, 목록 본문만 스크롤 (fill 레이아웃). 태블릿·모바일은 문서 스크롤. --%>
<div class="flex flex-col lg:h-full">
    <div class="px-x4 pt-x5 sm:px-x6 lg:px-x8 lg:pt-x6">
    @template.components.pageHeader(title = "계정 관리", subtitle = "직원에게 로그인 계정을 발급하고 비밀번호·잠금 상태를 관리합니다.", actions = @`
        <button type="button" data-shortcut="create" title="새로 만들기 (C)" class="${Seed.button("brandSolid")}" hx-get="/admin/accounts/issue${issueEmployeeId == null ? "" : "?employeeId=" + issueEmployeeId}" hx-target="#modal-body"
                hx-trigger="click${issueEmployeeId == null ? "" : ", load"}">${Icons.of("plus", "size-4")} 계정 발급</button>
    `)
    </div>

    <div class="flex min-h-0 flex-1 flex-col border-t border-stroke-neutral-subtle" hx-get="/admin/accounts" hx-trigger="accountsChanged from:body" hx-select="#account-table" hx-target="#account-table" hx-swap="outerHTML"
         hx-disinherit="*">
        <%-- 좁은 화면은 가로 스크롤(열 제목도 함께), 데스크톱은 열 제목 고정·행만 세로 스크롤 --%>
        <div id="account-table" class="flex min-h-0 flex-1 flex-col overflow-x-auto @5xl:overflow-x-visible">
            <div class="flex min-h-0 min-w-[880px] flex-1 flex-col @5xl:min-w-0">
                <div class="list-head shrink-0 sm:px-x6 lg:px-x8">
                    <span class="min-w-0 flex-1">직원</span>
                    <span class="w-48 shrink-0">아이디</span>
                    <span class="w-44 shrink-0">권한 그룹</span>
                    <span class="w-20 shrink-0">상태</span>
                    <span class="w-32 shrink-0">최근 로그인</span>
                    <span class="w-32 shrink-0">비밀번호 변경</span>
                    <span class="w-8 shrink-0"></span>
                </div>
                <div class="min-h-0 flex-1 lg:overflow-y-auto lg:overflow-x-hidden lg:overscroll-y-contain" data-fill-scroll>
                @for(Account a : accounts)
                    !{Employee e = employees.get(a.getEmployeeId());}
                    <div class="list-row sm:px-x6 lg:px-x8">
                        <span class="min-w-0 flex-1">
                            @if(e != null)
                                <a href="/employees/${e.id()}" class="flex min-w-0 items-center gap-x2 hover:underline">
                                    @template.components.avatar(photoUrl = e.photoUrl(), name = e.getName(), size = "size-6")
                                    <span class="truncate t4-medium">${e.getName()}</span>
                                    <span class="truncate t3-regular text-fg-neutral-subtle">${tree.nameOf(e.getDepartmentId())}</span>
                                </a>
                            @endif
                        </span>
                        <span class="list-cell-muted w-48 shrink-0">${a.getUsername()}</span>
                        <span class="flex w-44 shrink-0 flex-wrap gap-x1">
                            @for(String g : accountGroups.getOrDefault(a.id(), List.of()))
                                <span class="${Seed.badge("neutral", "outline")}">${g}</span>
                            @endfor
                        </span>
                        <span class="w-20 shrink-0">
                            @if(!a.isEnabled())
                                <span class="${Seed.badge("neutral")}">비활성</span>
                            @elseif(a.isLocked())
                                <span class="${Seed.badge("critical")}">${Icons.of("lock", "size-3")} 잠김</span>
                            @else
                                <span class="${Seed.badge("positive")}">사용 중</span>
                            @endif
                        </span>
                        <span class="w-32 shrink-0 t3-regular text-fg-neutral-subtle tabular-nums">${Ui.dateTime(a.getLastLoginAt())}</span>
                        <span class="w-32 shrink-0 t3-regular text-fg-neutral-subtle tabular-nums">${Ui.dateTime(a.getPasswordChangedAt())}</span>
                        <span class="w-8 shrink-0 text-right">
                            <details class="relative inline-block text-left" data-dropdown>
                                <summary class="${Seed.iconButton("ghost", "xsmall")}" aria-label="${a.getUsername()} 관리" title="관리">${Icons.of("ellipsis", "size-4")}</summary>
                                <div class="dropdown-panel w-44 p-x1">
                                    <button type="button" class="menu-item"
                                            hx-post="/admin/accounts/${a.id()}/reset-password" hx-target="#modal-body" hx-confirm="${a.getUsername()} 비밀번호를 초기화할까요?">${Icons.of("key", "size-4 text-fg-neutral-subtle")} 비밀번호 초기화</button>
                                    @if(a.isLocked())
                                        <form method="post" action="/admin/accounts/${a.id()}/unlock">${kr.co.abacus.abms.common.web.Csrf.field()}<button class="menu-item">${Icons.of("lock", "size-4 text-fg-neutral-subtle")} 잠금 해제</button></form>
                                    @endif
                                    @if(a.isEnabled())
                                        <form method="post" action="/admin/accounts/${a.id()}/disable" hx-confirm="계정을 비활성화할까요? 해당 사용자는 로그인할 수 없습니다.">${kr.co.abacus.abms.common.web.Csrf.field()}<button class="menu-item text-fg-critical">${Icons.of("x", "size-4")} 비활성화</button></form>
                                    @else
                                        <form method="post" action="/admin/accounts/${a.id()}/enable">${kr.co.abacus.abms.common.web.Csrf.field()}<button class="menu-item">${Icons.of("check", "size-4 text-fg-neutral-subtle")} 활성화</button></form>
                                    @endif
                                </div>
                            </details>
                        </span>
                    </div>
                @endfor
                </div>
            </div>
        </div>
    </div>
</div>
`)
