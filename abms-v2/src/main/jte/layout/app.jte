@import gg.jte.Content
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param String title
@param Content content
<!DOCTYPE html>
<html lang="ko" class="h-full" data-seed data-seed-color-mode="light-only">
<head>
    @template.layout.head(title = title)
</head>
<body class="h-full" hx-boost="true" hx-headers="${ctx.csrfHeaderJson()}">
<div class="flex min-h-full">
    <%-- 모바일 메뉴 백드롭 --%>
    <div id="sidebar-backdrop" class="fixed inset-0 z-[35] hidden bg-bg-overlay-muted lg:hidden" onclick="abmsSidebar(false)"></div>
    <%-- 사이드바 (SEED Side Navigation) --%>
    <aside id="sidebar" class="seed-side-navigation__root seed-side-navigation__root--tone_neutral fixed! inset-y-0 left-0 z-40 hidden shadow-s3 lg:flex lg:shadow-none">
        <div class="seed-side-navigation__header flex items-center gap-x2_5 px-x4">
            <a href="/" class="flex items-center gap-x2_5">
                <span class="flex size-x8 items-center justify-center rounded-r2 bg-bg-brand-solid t4-bold text-fg-on-brand-solid">A</span>
                <span class="t6-bold text-fg-neutral">ABMS</span>
            </a>
            <button type="button" class="${Seed.iconButton("ghost", "small")} ml-auto lg:hidden" aria-label="메뉴 닫기"
                    onclick="abmsSidebar(false)">
                ${Icons.of("x", "size-5")}
            </button>
        </div>
        <nav class="seed-side-navigation__content" aria-label="주 메뉴">
            <div class="seed-side-navigation__group">
                @if(ctx.can(PermissionCode.DASHBOARD_READ))
                    @template.layout.navItem(href = "/", label = "대시보드", icon = "home", current = ctx.active("/"))
                @endif
            </div>
            <div class="seed-side-navigation__group">
                <p class="seed-side-navigation__groupLabel">조직</p>
                @template.layout.navItem(href = "/employees", label = "직원", icon = "users", current = ctx.active("/employees"))
                @template.layout.navItem(href = "/departments", label = "부서", icon = "building", current = ctx.active("/departments"))
            </div>
            <div class="seed-side-navigation__group">
                <p class="seed-side-navigation__groupLabel">사업</p>
                @if(ctx.can(PermissionCode.PROJECT_READ))
                    @template.layout.navItem(href = "/projects", label = "프로젝트", icon = "folder", current = ctx.active("/projects"))
                @endif
                @if(ctx.can(PermissionCode.PARTY_READ))
                    @template.layout.navItem(href = "/parties", label = "협력사", icon = "briefcase", current = ctx.active("/parties"))
                @endif
                @if(ctx.can(PermissionCode.DASHBOARD_READ))
                    @template.layout.navItem(href = "/summary", label = "손익 현황", icon = "chart", current = ctx.active("/summary"))
                @endif
                @if(ctx.can(PermissionCode.REPORT_READ))
                    @template.layout.navItem(href = "/reports", label = "주간 보고서", icon = "document", current = ctx.active("/reports"))
                @endif
                @template.layout.navItem(href = "/assistant", label = "AI 어시스턴트", icon = "sparkles", current = ctx.active("/assistant"))
            </div>
            @if(ctx.can(PermissionCode.ACCOUNT_MANAGE) || ctx.can(PermissionCode.PERMISSION_GROUP_MANAGE) || ctx.can(PermissionCode.SUMMARY_MANAGE))
                <div class="seed-side-navigation__group">
                    <p class="seed-side-navigation__groupLabel">관리</p>
                    @if(ctx.can(PermissionCode.ACCOUNT_MANAGE))
                        @template.layout.navItem(href = "/admin/accounts", label = "계정", icon = "key", current = ctx.active("/admin/accounts"))
                    @endif
                    @if(ctx.can(PermissionCode.PERMISSION_GROUP_MANAGE))
                        @template.layout.navItem(href = "/admin/permission-groups", label = "권한 그룹", icon = "shield", current = ctx.active("/admin/permission-groups"))
                    @endif
                    @if(ctx.can(PermissionCode.SUMMARY_MANAGE))
                        @template.layout.navItem(href = "/admin/cost-policies", label = "원가 정책", icon = "calculator", current = ctx.active("/admin/cost-policies"))
                    @endif
                </div>
            @endif
        </nav>
    </aside>

    <div class="flex min-w-0 flex-1 flex-col lg:pl-[240px]">
        <%-- 상단 바 --%>
        <header class="sticky top-0 z-30 flex h-16 shrink-0 items-center gap-x2 border-b border-stroke-neutral-subtle bg-bg-layer-default px-x4 sm:px-x6">
            <button type="button" class="${Seed.iconButton("ghost", "small")} -ml-x2 lg:hidden" aria-label="메뉴 열기"
                    onclick="abmsSidebar(true)">
                ${Icons.of("menu", "size-5")}
            </button>
            <div class="flex-1"></div>
            @if(ctx.loggedIn())
                <details class="relative" data-dropdown>
                    <summary class="${Seed.iconButton("ghost", "small")} relative" aria-label="알림"
                             hx-get="/notifications" hx-target="#notification-panel" hx-trigger="click">
                        ${Icons.of("bell", "size-5")}
                        @if(ctx.unreadNotifications() > 0)
                            <span id="notification-dot" class="absolute top-x2 right-x2 flex size-x2 rounded-full bg-bg-critical-solid ring-2 ring-bg-layer-default"></span>
                        @endif
                    </summary>
                    <div class="dropdown-panel w-80 sm:w-96">
                        <div id="notification-panel" class="p-x4 t4-regular text-fg-neutral-subtle">불러오는 중…</div>
                    </div>
                </details>
                <details class="relative" data-dropdown>
                    <summary class="flex cursor-pointer items-center gap-x2 rounded-r2 px-x2 py-x1_5 transition-colors hover:bg-bg-transparent-pressed">
                        @template.components.avatar(avatar = ctx.requireUser().avatar(), initial = ctx.requireUser().initial(), size = "size-8 text-sm")
                        <span class="hidden t4-medium text-fg-neutral sm:block">${ctx.requireUser().name()}</span>
                        ${Icons.of("chevron-down", "size-4 text-fg-neutral-subtle")}
                    </summary>
                    <div class="dropdown-panel w-56 py-x1_5">
                        <div class="border-b border-stroke-neutral-subtle px-x4 py-x2_5">
                            <p class="t4-bold text-fg-neutral">${ctx.requireUser().name()}</p>
                            <p class="truncate t3-regular text-fg-neutral-subtle">${ctx.requireUser().getUsername()}</p>
                        </div>
                        <a href="/me" class="flex items-center gap-x2 px-x4 py-x2 t4-regular text-fg-neutral hover:bg-bg-layer-floating-pressed">${Icons.of("user", "size-4")} 내 정보</a>
                        <form method="post" action="/logout" hx-boost="false">
                            <input type="hidden" name="${ctx.csrfParameter()}" value="${ctx.csrfToken()}">
                            <button type="submit" class="flex w-full cursor-pointer items-center gap-x2 px-x4 py-x2 text-left t4-regular text-fg-neutral hover:bg-bg-layer-floating-pressed">
                                ${Icons.of("logout", "size-4")} 로그아웃
                            </button>
                        </form>
                    </div>
                </details>
            @endif
        </header>

        <main class="mx-auto w-full max-w-[1440px] flex-1 px-x4 py-x6 sm:px-x6 lg:px-x8">
            ${content}
        </main>
    </div>
</div>

<dialog id="modal" class="m-auto w-full max-w-lg rounded-r6 bg-bg-layer-floating p-0 shadow-s3">
    <div id="modal-body"></div>
</dialog>
<div id="toasts" class="pointer-events-none fixed inset-x-0 bottom-6 z-50 flex flex-col items-center gap-x2 px-x4" aria-live="polite"></div>
@if(ctx.toast() != null)
    <div id="flash-toast" hidden data-type="${ctx.toast().type()}" data-message="${ctx.toast().message()}"></div>
@endif
</body>
</html>
