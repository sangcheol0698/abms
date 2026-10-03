@import gg.jte.Content
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param String title
@param Content content
<!DOCTYPE html>
<html lang="ko" class="h-full" data-seed data-seed-color-mode="system" data-seed-user-color-scheme="light">
<head>
    @template.layout.head(title = title)
</head>
<body class="h-full bg-bg-layer-basement" hx-boost="true" hx-headers="${ctx.csrfHeaderJson()}">
<div class="flex min-h-full">
    <%-- 모바일 메뉴 백드롭 --%>
    <div id="sidebar-backdrop" class="fixed inset-0 z-[35] hidden bg-bg-overlay-muted lg:hidden" onclick="abmsSidebar(false)"></div>

    <%-- 사이드바 (SEED Side Navigation, Linear 풍 배치) --%>
    <aside id="sidebar" class="seed-side-navigation__root seed-side-navigation__root--tone_neutral fixed! inset-y-0 left-0 z-40 hidden bg-bg-layer-basement! shadow-s3 lg:flex lg:shadow-none">
        <div class="seed-side-navigation__header flex items-center gap-x1">
            <a href="/" class="flex min-w-0 flex-1 items-center gap-x2 rounded-r2 px-x1_5 py-x1 hover:bg-bg-transparent-pressed">
                <span class="flex size-x5 shrink-0 items-center justify-center rounded-r1 bg-bg-brand-solid t2-bold text-fg-on-brand-solid">A</span>
                <span class="truncate t4-bold text-fg-neutral">ABMS</span>
            </a>
            @if(ctx.loggedIn())
                <details class="relative" data-dropdown>
                    <summary class="${Seed.iconButton("ghost", "xsmall")} relative" aria-label="알림"
                             hx-get="/notifications" hx-target="#notification-panel" hx-trigger="click">
                        ${Icons.of("bell", "size-4")}
                        @if(ctx.unreadNotifications() > 0)
                            <span id="notification-dot" class="absolute top-x1_5 right-x1_5 flex size-x1_5 rounded-full bg-bg-brand-solid"></span>
                        @endif
                    </summary>
                    <div class="dropdown-panel left-0 right-auto w-80 origin-top-left sm:w-96">
                        <div id="notification-panel" class="p-x4 t4-regular text-fg-neutral-subtle">불러오는 중…</div>
                    </div>
                </details>
            @endif
            <button type="button" class="${Seed.iconButton("ghost", "xsmall")} lg:hidden" aria-label="메뉴 닫기" onclick="abmsSidebar(false)">
                ${Icons.of("x", "size-4")}
            </button>
        </div>

        <div class="px-x3 pb-x1">
            <button type="button" data-open-palette
                    class="flex h-8 w-full cursor-pointer items-center gap-x2 rounded-r2 border border-stroke-neutral-subtle bg-bg-layer-default px-x2_5 t4-regular text-fg-neutral-subtle shadow-s1 transition-colors hover:text-fg-neutral">
                ${Icons.of("search", "size-4")}
                <span class="flex-1 text-left">검색 · 이동</span>
                <span class="kbd">⌘K</span>
            </button>
        </div>

        <nav class="seed-side-navigation__content" aria-label="주 메뉴">
            <div class="seed-side-navigation__group">
                @if(ctx.can(PermissionCode.DASHBOARD_READ))
                    @template.layout.navItem(href = "/", label = "대시보드", icon = "home", current = ctx.active("/"))
                @endif
                @template.layout.navItem(href = "/assistant", label = "AI 어시스턴트", icon = "sparkles", current = ctx.active("/assistant"))
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

        @if(ctx.loggedIn())
            <div class="seed-side-navigation__footer px-x3! pb-x3!">
                <details class="relative" data-dropdown>
                    <summary class="flex cursor-pointer items-center gap-x2 rounded-r2 px-x1_5 py-x1_5 transition-colors hover:bg-bg-transparent-pressed">
                        @template.components.avatar(avatar = ctx.requireUser().avatar(), initial = ctx.requireUser().initial(), size = "size-6 text-[11px]")
                        <span class="min-w-0 flex-1 truncate t4-medium text-fg-neutral">${ctx.requireUser().name()}</span>
                        ${Icons.of("chevron-down", "size-4 text-fg-neutral-subtle")}
                    </summary>
                    <div class="dropdown-panel bottom-full left-0 right-0 mb-x1 w-auto origin-bottom p-x1">
                        <div class="px-x2 py-x1_5">
                            <p class="truncate t3-regular text-fg-neutral-subtle">${ctx.requireUser().getUsername()}</p>
                        </div>
                        <a href="/me" class="menu-item">${Icons.of("user", "size-4 text-fg-neutral-subtle")} 내 정보</a>
                        <div class="my-x1 border-t border-stroke-neutral-subtle"></div>
                        <p class="px-x2 pt-x1 pb-x1_5 t3-medium text-fg-neutral-subtle">테마</p>
                        <div class="grid grid-cols-3 gap-x1 px-x1 pb-x1" role="group" aria-label="테마">
                            <button type="button" class="theme-option" data-theme-option="system" onclick="abmsTheme('system')">${Icons.of("monitor", "size-4")}<span>시스템</span></button>
                            <button type="button" class="theme-option" data-theme-option="light" onclick="abmsTheme('light')">${Icons.of("sun", "size-4")}<span>라이트</span></button>
                            <button type="button" class="theme-option" data-theme-option="dark" onclick="abmsTheme('dark')">${Icons.of("moon", "size-4")}<span>다크</span></button>
                        </div>
                        <div class="my-x1 border-t border-stroke-neutral-subtle"></div>
                        <form method="post" action="/logout" hx-boost="false">
                            <input type="hidden" name="${ctx.csrfParameter()}" value="${ctx.csrfToken()}">
                            <button type="submit" class="menu-item">${Icons.of("logout", "size-4 text-fg-neutral-subtle")} 로그아웃</button>
                        </form>
                    </div>
                </details>
            </div>
        @endif
    </aside>

    <div class="flex min-w-0 flex-1 flex-col lg:pl-[240px]">
        <%-- 모바일 상단 바 --%>
        <header class="sticky top-0 z-30 flex h-12 shrink-0 items-center gap-x2 border-b border-stroke-neutral-subtle bg-bg-layer-default px-x3 lg:hidden">
            <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" aria-label="메뉴 열기" onclick="abmsSidebar(true)">
                ${Icons.of("menu", "size-5")}
            </button>
            <span class="t4-bold text-fg-neutral">ABMS</span>
            <div class="flex-1"></div>
            <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" aria-label="검색" data-open-palette>${Icons.of("search", "size-4")}</button>
        </header>

        <main class="flex-1 lg:py-x2 lg:pr-x2">
            <div class="min-h-full bg-bg-layer-default lg:min-h-[calc(100vh-16px)] lg:rounded-r3 lg:border lg:border-stroke-neutral-subtle lg:shadow-s1">
                <div class="mx-auto w-full max-w-[1400px] px-x4 py-x5 sm:px-x6 lg:px-x8 lg:py-x6">
                    ${content}
                </div>
            </div>
        </main>
    </div>
</div>

<dialog id="modal" class="m-auto w-full max-w-lg rounded-r5 bg-bg-layer-floating p-0 shadow-s3">
    <div id="modal-body"></div>
</dialog>
<%-- Cmd+K 명령 팔레트 --%>
<dialog id="palette" class="mx-auto mt-[12vh] w-[calc(100%-32px)] max-w-xl overflow-hidden rounded-r5 bg-bg-layer-floating p-0 shadow-s3" aria-label="명령 팔레트">
    <div class="flex items-center gap-x2 border-b border-stroke-neutral-subtle px-x4">
        ${Icons.of("search", "size-4 shrink-0 text-fg-neutral-subtle")}
        <input id="palette-input" name="q" type="text" autocomplete="off" spellcheck="false" placeholder="이동, 검색, 명령 실행…"
               class="h-12 w-full bg-transparent t5-regular text-fg-neutral outline-none placeholder:text-fg-placeholder"
               hx-get="/palette" hx-trigger="input changed delay:120ms, palette-open" hx-target="#palette-results" hx-sync="this:replace"
               role="combobox" aria-controls="palette-results" aria-expanded="true">
        <span class="kbd">esc</span>
    </div>
    <div id="palette-results" class="max-h-[min(60vh,420px)] overflow-y-auto p-x1_5" role="listbox"></div>
    <div class="flex items-center gap-x4 border-t border-stroke-neutral-subtle px-x4 py-x2 t3-regular text-fg-neutral-subtle">
        <span class="flex items-center gap-x1"><span class="kbd">↑</span><span class="kbd">↓</span> 이동</span>
        <span class="flex items-center gap-x1"><span class="kbd">↵</span> 선택</span>
        <span class="ml-auto flex items-center gap-x1"><span class="kbd">?</span> 단축키</span>
    </div>
</dialog>

<%-- 단축키 도움말 --%>
<dialog id="shortcuts" class="m-auto w-[calc(100%-32px)] max-w-md rounded-r5 bg-bg-layer-floating p-0 shadow-s3" aria-label="키보드 단축키">
    <div class="flex items-center justify-between border-b border-stroke-neutral-subtle px-x5 py-x3">
        <h2 class="t5-bold text-fg-neutral">키보드 단축키</h2>
        <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" onclick="this.closest('dialog').close()" aria-label="닫기">${Icons.of("x", "size-4")}</button>
    </div>
    <dl class="grid grid-cols-[1fr_auto] gap-x-x4 gap-y-x2_5 px-x5 py-x4 t4-regular">
        <dt class="text-fg-neutral-muted">명령 팔레트</dt><dd class="flex gap-x1"><span class="kbd">⌘</span><span class="kbd">K</span></dd>
        <dt class="text-fg-neutral-muted">목록 검색창으로 이동</dt><dd><span class="kbd">/</span></dd>
        <dt class="text-fg-neutral-muted">새로 만들기 (현재 화면)</dt><dd><span class="kbd">C</span></dd>
        <dt class="text-fg-neutral-muted">대시보드</dt><dd class="flex gap-x1"><span class="kbd">G</span><span class="kbd">D</span></dd>
        <dt class="text-fg-neutral-muted">직원</dt><dd class="flex gap-x1"><span class="kbd">G</span><span class="kbd">E</span></dd>
        <dt class="text-fg-neutral-muted">부서</dt><dd class="flex gap-x1"><span class="kbd">G</span><span class="kbd">O</span></dd>
        <dt class="text-fg-neutral-muted">프로젝트</dt><dd class="flex gap-x1"><span class="kbd">G</span><span class="kbd">P</span></dd>
        <dt class="text-fg-neutral-muted">협력사</dt><dd class="flex gap-x1"><span class="kbd">G</span><span class="kbd">C</span></dd>
        <dt class="text-fg-neutral-muted">손익 현황</dt><dd class="flex gap-x1"><span class="kbd">G</span><span class="kbd">S</span></dd>
        <dt class="text-fg-neutral-muted">주간 보고서</dt><dd class="flex gap-x1"><span class="kbd">G</span><span class="kbd">R</span></dd>
        <dt class="text-fg-neutral-muted">AI 어시스턴트</dt><dd class="flex gap-x1"><span class="kbd">G</span><span class="kbd">A</span></dd>
        <dt class="text-fg-neutral-muted">내 정보</dt><dd class="flex gap-x1"><span class="kbd">G</span><span class="kbd">M</span></dd>
        <dt class="text-fg-neutral-muted">단축키 보기</dt><dd><span class="kbd">?</span></dd>
    </dl>
</dialog>

<div id="toasts" class="pointer-events-none fixed inset-x-0 bottom-6 z-50 flex flex-col items-center gap-x2 px-x4" aria-live="polite"></div>
@if(ctx.toast() != null)
    <div id="flash-toast" hidden data-type="${ctx.toast().type()}" data-message="${ctx.toast().message()}"></div>
@endif
</body>
</html>
