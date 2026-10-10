@import gg.jte.Content
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.web.ViewContext
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param String title
@param Content content
@param Content aside = null
@param boolean fill = false
<%--
  앱 셸: 상단 헤더 / 왼쪽 사이드바(메뉴) / 본문 패널(안쪽만 스크롤) / 오른쪽 사이드바(속성·알림)
  - 데스크톱(lg+): 문서는 스크롤하지 않고 #app-scroll 만 스크롤한다. 양쪽 사이드바는 html[data-left|data-right] 로 열고 닫는다.
  - 모바일: 문서 스크롤 + 양쪽 사이드바는 서랍(drawer)으로 연다.
  - aside: 화면별 오른쪽 사이드바 '속성' 탭 내용, fill: 본문이 직접 열별 스크롤을 관리하는 화면(부서·어시스턴트)
--%>
<!DOCTYPE html>
<html lang="ko" class="h-full" data-seed data-seed-color-mode="system" data-seed-user-color-scheme="light">
<head>
    @template.layout.head(title = title)
</head>
<body class="app-body" hx-boost="true" hx-headers="${ctx.csrfHeaderJson()}" data-map-key="${ctx.mapKey()}">
<%-- data-tauri-drag-region: 데스크톱 앱(macOS)에서 상단 바의 빈 곳을 끌어 창을 옮긴다. 브라우저에서는 쓰이지 않는다. --%>
<header class="app-header" data-tauri-drag-region="deep">
    <a href="/" class="flex items-center gap-x2 rounded-r2 px-x1_5 py-x1 hover:bg-bg-transparent-pressed">
        <span class="flex size-x5 shrink-0 items-center justify-center rounded-r1 bg-bg-brand-solid t2-bold text-fg-on-brand-solid">A</span>
        <span class="hidden t4-bold text-fg-neutral sm:inline">ABMS</span>
    </a>
    <button type="button" class="app-left-toggle ${Seed.iconButton("ghost", "xsmall")}" data-toggle-panel="left" aria-controls="left-sidebar" aria-label="왼쪽 사이드바 열기/닫기" title="사이드바 ( [ )">
        ${Icons.of("sidebar-left", "size-4")}
    </button>
    <%-- 데스크톱 앱에는 브라우저의 뒤로·앞으로 버튼이 없어 앱에서만 보인다. --%>
    <span class="app-shell-history">
        <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" data-history="back" aria-label="뒤로" title="뒤로 ( ⌘[ )">${Icons.of("chevron-left", "size-4")}</button>
        <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" data-history="forward" aria-label="앞으로" title="앞으로 ( ⌘] )">${Icons.of("chevron-right", "size-4")}</button>
    </span>
    <div class="flex flex-1 justify-center px-x2">
        <button type="button" data-open-palette
                class="flex h-8 w-full max-w-md cursor-pointer items-center gap-x2 rounded-r2 border border-stroke-neutral-subtle bg-bg-layer-default px-x2_5 t4-regular text-fg-neutral-subtle shadow-s1 transition-colors hover:text-fg-neutral">
            ${Icons.of("search", "size-4")}
            <span class="flex-1 truncate text-left">검색 · 이동 · 명령</span>
            <span class="kbd hidden sm:inline-flex">⌘K</span>
        </button>
    </div>
    @if(ctx.loggedIn())
        <button type="button" class="${Seed.iconButton("ghost", "xsmall")} relative" data-open-notifications aria-label="알림" title="알림">
            ${Icons.of("bell", "size-4")}
            @if(ctx.unreadNotifications() > 0)
                <span id="notification-dot" class="absolute top-1 right-1 flex size-x1_5 rounded-full bg-bg-brand-solid"></span>
            @endif
        </button>
        <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" data-toggle-panel="right" aria-controls="right-sidebar" aria-label="오른쪽 사이드바 열기/닫기" title="오른쪽 패널 ( ] )">
            ${Icons.of("sidebar-right", "size-4")}
        </button>
        <details class="relative" data-dropdown>
            <summary class="flex cursor-pointer items-center gap-x1_5 rounded-r2 p-x1 transition-colors hover:bg-bg-transparent-pressed" aria-label="사용자 메뉴">
                @template.components.avatar(photoUrl = ctx.requireUser().photoUrl(), name = ctx.requireUser().name(), size = "size-6")
            </summary>
            <div class="dropdown-panel w-60 p-x1">
                <div class="px-x2 py-x1_5">
                    <p class="truncate t4-bold text-fg-neutral">${ctx.requireUser().name()}</p>
                    <p class="truncate t3-regular text-fg-neutral-subtle">${ctx.requireUser().getUsername()}</p>
                </div>
                <a href="/me" class="menu-item">${Icons.of("user", "size-4 text-fg-neutral-subtle")} 내 정보</a>
                <button type="button" class="menu-item" data-open-dialog="shortcuts">${Icons.of("command", "size-4 text-fg-neutral-subtle")} 키보드 단축키</button>
                <div class="my-x1 border-t border-stroke-neutral-subtle"></div>
                <p class="px-x2 pt-x1 pb-x1_5 t3-medium text-fg-neutral-subtle">테마</p>
                <div class="grid grid-cols-3 gap-x1 px-x1 pb-x1" role="group" aria-label="테마">
                    <button type="button" class="theme-option" data-theme-option="system">${Icons.of("monitor", "size-4")}<span>시스템</span></button>
                    <button type="button" class="theme-option" data-theme-option="light">${Icons.of("sun", "size-4")}<span>라이트</span></button>
                    <button type="button" class="theme-option" data-theme-option="dark">${Icons.of("moon", "size-4")}<span>다크</span></button>
                </div>
                <div class="my-x1 border-t border-stroke-neutral-subtle"></div>
                <form method="post" action="/logout" hx-boost="false">
                    <input type="hidden" name="${ctx.csrfParameter()}" value="${ctx.csrfToken()}">
                    <button type="submit" class="menu-item">${Icons.of("logout", "size-4 text-fg-neutral-subtle")} 로그아웃</button>
                </form>
            </div>
        </details>
    @endif
</header>

<div class="app-frame">
    <div class="app-backdrop" data-close-panels aria-hidden="true"></div>

    <%-- 왼쪽 사이드바 (SEED Side Navigation) --%>
    <aside id="left-sidebar" class="app-left seed-side-navigation__root seed-side-navigation__root--tone_neutral" aria-label="주 메뉴">
        <nav class="seed-side-navigation__content">
            <div class="seed-side-navigation__group">
                @if(ctx.can(PermissionCode.DASHBOARD_READ))
                    @template.layout.navItem(href = "/", label = "대시보드", icon = "home", current = ctx.active("/"))
                @endif
                @template.layout.navItem(href = "/assistant", label = "AI 어시스턴트", icon = "sparkles", current = ctx.active("/assistant"))
                @template.layout.navItem(href = "/notices", label = "공지사항", icon = "megaphone", current = ctx.active("/notices"), badge = ctx.unreadNotices())
            </div>
            <div class="seed-side-navigation__group">
                <p class="seed-side-navigation__groupLabel">조직</p>
                @template.layout.navItem(href = "/employees", label = "직원", icon = "users", current = ctx.active("/employees"))
                @template.layout.navItem(href = "/departments", label = "부서", icon = "building", current = ctx.active("/departments"))
                @template.layout.navItem(href = "/sites", label = "사업장", icon = "map-pin", current = ctx.active("/sites"))
            </div>
            <div class="seed-side-navigation__group">
                <p class="seed-side-navigation__groupLabel">사업</p>
                @if(ctx.can(PermissionCode.PROJECT_READ))
                    @template.layout.navItem(href = "/projects", label = "프로젝트", icon = "folder", current = ctx.active("/projects"))
                @endif
                @if(ctx.can(PermissionCode.PROJECT_WRITE))
                    @template.layout.navItem(href = "/staffing", label = "인력 찾기", icon = "search", current = ctx.active("/staffing"))
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
                    @if(ctx.can(PermissionCode.ACCOUNT_MANAGE))
                        @template.layout.navItem(href = "/admin/audit-logs", label = "변경 이력", icon = "refresh", current = ctx.active("/admin/audit-logs"))
                    @endif
                </div>
            @endif
        </nav>
    </aside>
    <div class="app-resize" data-resize-panel="left" role="separator" aria-orientation="vertical" aria-controls="left-sidebar" aria-label="왼쪽 사이드바 너비" tabindex="0"></div>

    <%-- 본문 패널 + 오른쪽 사이드바 --%>
    <div class="app-panel">
        <main id="app-scroll" class="app-scroll ${fill ? "app-scroll-fill" : ""}" tabindex="-1">
            @if(fill)
                ${content}
            @else
                <div class="mx-auto w-full max-w-[1400px] px-x4 py-x5 sm:px-x6 lg:px-x8 lg:py-x6">${content}</div>
            @endif
        </main>
        <div class="app-resize" data-resize-panel="right" role="separator" aria-orientation="vertical" aria-controls="right-sidebar" aria-label="오른쪽 사이드바 너비" tabindex="0"></div>

        <aside id="right-sidebar" class="app-right" aria-label="오른쪽 사이드바">
            <div class="flex h-11 shrink-0 items-center gap-x1 border-b border-stroke-neutral-subtle px-x3" role="tablist">
                @if(aside != null)
                    <button type="button" class="right-tab" role="tab" data-right-tab="props">속성</button>
                @endif
                <button type="button" class="right-tab" role="tab" data-right-tab="notifications">
                    알림@if(ctx.loggedIn() && ctx.unreadNotifications() > 0)<span class="ml-x1 t3-regular text-fg-neutral-subtle tabular-nums">${ctx.unreadNotifications()}</span>@endif
                </button>
                <button type="button" class="${Seed.iconButton("ghost", "xsmall")} ml-auto" data-toggle-panel="right" aria-label="오른쪽 사이드바 닫기" title="닫기 ( ] )">${Icons.of("x", "size-4")}</button>
            </div>
            @if(aside != null)
                <div class="right-pane" data-right-pane="props" role="tabpanel">${aside}</div>
            @endif
            <div class="right-pane" data-right-pane="notifications" role="tabpanel">
                <div id="notification-panel" hx-get="/notifications" hx-trigger="intersect once" class="p-x4 t4-regular text-fg-neutral-subtle">불러오는 중…</div>
            </div>
        </aside>
    </div>
</div>

@if(ctx.noticePopup())
    <div hidden hx-get="/notices/popup" hx-trigger="load delay:400ms" hx-target="#modal-body" data-notice-popup-loader></div>
@endif
<dialog id="modal" class="m-auto w-full max-w-lg rounded-r5 bg-bg-layer-floating p-0 shadow-s3">
    <div id="modal-body"></div>
</dialog>

<%-- Cmd+K 명령 팔레트 --%>
<dialog id="palette" class="mx-auto mt-[12vh] w-[calc(100%-32px)] max-w-xl overflow-hidden rounded-r5 bg-bg-layer-floating p-0 shadow-s3" aria-label="명령 팔레트">
    <div class="flex items-center gap-x2 border-b border-stroke-neutral-subtle px-x4">
        ${Icons.of("search", "size-4 shrink-0 text-fg-neutral-subtle")}
        <input id="palette-input" name="q" type="text" autocomplete="off" spellcheck="false" placeholder="이동, 검색, 명령 실행…"
               class="h-12 w-full bg-transparent t5-regular text-fg-neutral outline-none placeholder:text-fg-placeholder"
               hx-get="/palette" hx-trigger="input delay:120ms, palette-open" hx-target="#palette-results" hx-sync="this:replace"
               role="combobox" aria-controls="palette-results" aria-expanded="true">
        <%-- 닫기: 데스크톱은 esc 표시, 모바일(키보드 없음)은 '취소' 버튼 --%>
        <button type="button" data-close-dialog class="shrink-0 cursor-pointer" aria-label="명령 팔레트 닫기">
            <span class="kbd hidden sm:inline-flex">esc</span>
            <span class="t4-medium text-fg-brand sm:hidden">취소</span>
        </button>
    </div>
    <div id="palette-results" class="max-h-[min(60vh,420px)] overflow-y-auto overflow-x-hidden overscroll-y-contain p-x1_5" role="listbox"></div>
    <div class="hidden items-center gap-x4 border-t border-stroke-neutral-subtle px-x4 py-x2 t3-regular text-fg-neutral-subtle sm:flex">
        <span class="flex items-center gap-x1"><span class="kbd">↑</span><span class="kbd">↓</span> 이동</span>
        <span class="flex items-center gap-x1"><span class="kbd">↵</span> 선택</span>
        <span class="ml-auto flex items-center gap-x1"><span class="kbd">?</span> 단축키</span>
    </div>
</dialog>

<%-- 단축키 도움말 --%>
<dialog id="shortcuts" class="m-auto w-[calc(100%-32px)] max-w-md rounded-r5 bg-bg-layer-floating p-0 shadow-s3" aria-label="키보드 단축키">
    <div class="flex items-center justify-between border-b border-stroke-neutral-subtle px-x5 py-x3">
        <h2 class="t5-bold text-fg-neutral">키보드 단축키</h2>
        <button type="button" class="${Seed.iconButton("ghost", "xsmall")}" data-close-dialog aria-label="닫기">${Icons.of("x", "size-4")}</button>
    </div>
    <dl class="grid grid-cols-[1fr_auto] gap-x-x4 gap-y-x2_5 px-x5 py-x4 t4-regular">
        <dt class="text-fg-neutral-muted">명령 팔레트</dt><dd class="flex gap-x1"><span class="kbd">⌘</span><span class="kbd">K</span></dd>
        <dt class="text-fg-neutral-muted">왼쪽 사이드바 열기/닫기</dt><dd><span class="kbd">[</span></dd>
        <dt class="text-fg-neutral-muted">오른쪽 사이드바 열기/닫기</dt><dd><span class="kbd">]</span></dd>
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
        <dt class="text-fg-neutral-muted">모달 폼 제출</dt><dd class="flex gap-x1"><span class="kbd">⌘</span><span class="kbd">↵</span></dd>
        <dt class="text-fg-neutral-muted">단축키 보기</dt><dd><span class="kbd">?</span></dd>
    </dl>
</dialog>

<div id="toasts" popover="manual" class="toast-region" aria-live="polite"></div>
@if(ctx.toast() != null)
    <div id="flash-toast" hidden data-type="${ctx.toast().type()}" data-message="${ctx.toast().message()}"></div>
@endif
</body>
</html>
