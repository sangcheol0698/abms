@import gg.jte.Content
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@param ViewContext ctx
@param String title
@param Content content
<!DOCTYPE html>
<html lang="ko" class="h-full">
<head>
    @template.layout.head(title = title)
</head>
<body class="h-full" hx-boost="true" hx-headers="${ctx.csrfHeaderJson()}">
<div class="flex min-h-full">
    <%-- 사이드바 --%>
    <aside id="sidebar" class="fixed inset-y-0 left-0 z-40 hidden w-64 flex-col bg-slate-900 lg:flex">
        <div class="flex h-16 shrink-0 items-center gap-2.5 px-5">
            <div class="flex size-8 items-center justify-center rounded-lg bg-brand-500 text-sm font-bold text-white">A</div>
            <a href="/" class="text-lg font-semibold tracking-tight text-white">ABMS</a>
            <button type="button" class="ml-auto text-slate-400 lg:hidden" onclick="document.getElementById('sidebar').classList.add('hidden')">
                ${Icons.of("x", "size-5")}
            </button>
        </div>
        <nav class="flex-1 overflow-y-auto px-3 pb-6">
            @if(ctx.can(PermissionCode.DASHBOARD_READ))
                <a href="/" class="nav-link ${ctx.active("/") ? "active" : ""}">${Icons.of("home", "size-5")} 대시보드</a>
            @endif
            <p class="nav-section">조직</p>
            <a href="/employees" class="nav-link ${ctx.active("/employees") ? "active" : ""}">${Icons.of("users", "size-5")} 직원</a>
            <a href="/departments" class="nav-link ${ctx.active("/departments") ? "active" : ""}">${Icons.of("building", "size-5")} 부서</a>

            <p class="nav-section">사업</p>
            @if(ctx.can(PermissionCode.PROJECT_READ))
                <a href="/projects" class="nav-link ${ctx.active("/projects") ? "active" : ""}">${Icons.of("folder", "size-5")} 프로젝트</a>
            @endif
            @if(ctx.can(PermissionCode.PARTY_READ))
                <a href="/parties" class="nav-link ${ctx.active("/parties") ? "active" : ""}">${Icons.of("briefcase", "size-5")} 협력사</a>
            @endif
            @if(ctx.can(PermissionCode.DASHBOARD_READ))
                <a href="/summary" class="nav-link ${ctx.active("/summary") ? "active" : ""}">${Icons.of("chart", "size-5")} 손익 현황</a>
            @endif
            @if(ctx.can(PermissionCode.REPORT_READ))
                <a href="/reports" class="nav-link ${ctx.active("/reports") ? "active" : ""}">${Icons.of("document", "size-5")} 주간 보고서</a>
            @endif
            <a href="/assistant" class="nav-link ${ctx.active("/assistant") ? "active" : ""}">${Icons.of("sparkles", "size-5")} AI 어시스턴트</a>

            @if(ctx.can(PermissionCode.ACCOUNT_MANAGE) || ctx.can(PermissionCode.PERMISSION_GROUP_MANAGE) || ctx.can(PermissionCode.SUMMARY_MANAGE))
                <p class="nav-section">관리</p>
                @if(ctx.can(PermissionCode.ACCOUNT_MANAGE))
                    <a href="/admin/accounts" class="nav-link ${ctx.active("/admin/accounts") ? "active" : ""}">${Icons.of("key", "size-5")} 계정</a>
                @endif
                @if(ctx.can(PermissionCode.PERMISSION_GROUP_MANAGE))
                    <a href="/admin/permission-groups" class="nav-link ${ctx.active("/admin/permission-groups") ? "active" : ""}">${Icons.of("shield", "size-5")} 권한 그룹</a>
                @endif
                @if(ctx.can(PermissionCode.SUMMARY_MANAGE))
                    <a href="/admin/cost-policies" class="nav-link ${ctx.active("/admin/cost-policies") ? "active" : ""}">${Icons.of("calculator", "size-5")} 원가 정책</a>
                @endif
            @endif
        </nav>
    </aside>

    <div class="flex min-w-0 flex-1 flex-col lg:pl-64">
        <%-- 상단 바 --%>
        <header class="sticky top-0 z-30 flex h-16 shrink-0 items-center gap-3 border-b border-slate-200 bg-white/90 px-4 backdrop-blur sm:px-6">
            <button type="button" class="btn btn-ghost -ml-2 lg:hidden" aria-label="메뉴 열기"
                    onclick="document.getElementById('sidebar').classList.remove('hidden')">
                ${Icons.of("menu", "size-5")}
            </button>
            <div class="flex-1"></div>
            @if(ctx.loggedIn())
                <details class="relative" data-dropdown>
                    <summary class="btn btn-ghost relative !p-2" aria-label="알림"
                             hx-get="/notifications" hx-target="#notification-panel" hx-trigger="click">
                        ${Icons.of("bell", "size-5")}
                        @if(ctx.unreadNotifications() > 0)
                            <span id="notification-dot" class="absolute top-1.5 right-1.5 flex size-2 rounded-full bg-rose-500"></span>
                        @endif
                    </summary>
                    <div class="dropdown-panel w-80 sm:w-96">
                        <div id="notification-panel" class="p-4 text-sm text-slate-500">불러오는 중…</div>
                    </div>
                </details>
                <details class="relative" data-dropdown>
                    <summary class="flex cursor-pointer items-center gap-2 rounded-lg px-2 py-1.5 hover:bg-slate-100">
                        @template.components.avatar(avatar = ctx.requireUser().avatar(), initial = ctx.requireUser().initial(), size = "size-8 text-sm")
                        <span class="hidden text-sm font-medium text-slate-700 sm:block">${ctx.requireUser().name()}</span>
                        ${Icons.of("chevron-down", "size-4 text-slate-400")}
                    </summary>
                    <div class="dropdown-panel w-56 py-1.5">
                        <div class="border-b border-slate-100 px-4 py-2.5">
                            <p class="text-sm font-medium text-slate-900">${ctx.requireUser().name()}</p>
                            <p class="truncate text-xs text-slate-500">${ctx.requireUser().getUsername()}</p>
                        </div>
                        <a href="/me" class="flex items-center gap-2 px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">${Icons.of("user", "size-4")} 내 정보</a>
                        <form method="post" action="/logout" hx-boost="false">
                            <input type="hidden" name="${ctx.csrfParameter()}" value="${ctx.csrfToken()}">
                            <button type="submit" class="flex w-full items-center gap-2 px-4 py-2 text-left text-sm text-slate-700 hover:bg-slate-50">
                                ${Icons.of("logout", "size-4")} 로그아웃
                            </button>
                        </form>
                    </div>
                </details>
            @endif
        </header>

        <main class="flex-1 px-4 py-6 sm:px-6 lg:px-8">
            ${content}
        </main>
    </div>
</div>

<dialog id="modal" class="m-auto w-full max-w-lg rounded-2xl bg-white p-0 shadow-xl">
    <div id="modal-body"></div>
</dialog>
<div id="toasts" class="pointer-events-none fixed top-4 right-4 z-50 flex flex-col gap-2" aria-live="polite"></div>
@if(ctx.toast() != null)
    <div id="flash-toast" hidden data-type="${ctx.toast().type()}" data-message="${ctx.toast().message()}"></div>
@endif
</body>
</html>
