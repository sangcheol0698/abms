@import java.util.List
@import java.util.Map
@import kr.co.abacus.abms.access.Permission
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.access.PermissionScope
@import kr.co.abacus.abms.account.Account
@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.project.ProjectAssignment
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param Employee employee
@param Account account
@param DepartmentTree tree
@param List<ProjectAssignment> assignments
@param Map<Long, String> projectNames
@param java.util.Set<Long> readableProjectIds
@param List<Permission> permissions
@param FormErrors errors
@template.layout.app(ctx = ctx, title = "내 정보", content = @`
    @template.components.pageHeader(title = "내 정보", actions = @`
        <a href="/employees/${employee.id()}" class="${Seed.button("neutralOutline")}">${Icons.of("user", "size-4")} 직원 상세 보기</a>
    `)
    <div class="mx-auto max-w-4xl">
        <section class="form-section">
            <div><h2 class="form-section-title">프로필</h2><p class="form-section-desc">이름·생년월일·아바타는 직원 상세에서 직접 수정할 수 있습니다.</p></div>
            <div class="flex items-center gap-x4">
                @template.components.avatar(avatar = employee.getAvatar(), initial = employee.initial(), size = "size-12 text-lg")
                <div class="min-w-0">
                    <p class="t6-bold text-fg-neutral">${employee.getName()}</p>
                    <p class="t4-regular text-fg-neutral-subtle">${tree.nameOf(employee.getDepartmentId())} · ${employee.getPosition().label()}</p>
                </div>
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">계정</h2></div>
            <dl class="property-list">
                <dt>아이디</dt><dd>${account.getUsername()}</dd>
                <dt>최근 로그인</dt><dd class="tabular-nums">${Ui.dateTime(account.getLastLoginAt())}</dd>
                <dt>비밀번호 변경</dt><dd class="tabular-nums">${Ui.dateTime(account.getPasswordChangedAt())}</dd>
            </dl>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">비밀번호 변경</h2><p class="form-section-desc">8자 이상, 영문과 숫자를 포함하세요.</p></div>
            <div id="password-card">@template.me.passwordForm(errors = errors, done = false)</div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">참여 프로젝트</h2><p class="form-section-desc">최근 투입 이력입니다.</p></div>
            <div class="list-box">
                @if(assignments.isEmpty())
                    @template.components.empty(message = "최근 참여한 프로젝트가 없습니다.", icon = "folder")
                @else
                    @for(ProjectAssignment a : assignments)
                        <div class="list-row">
                            ${Icons.of("folder", "size-4 shrink-0 text-fg-neutral-subtle")}
                            <span class="min-w-0 flex-1 truncate t4-medium">
                                @if(readableProjectIds.contains(a.getProjectId()))
                                    <a href="/projects/${a.getProjectId()}" class="hover:underline">${projectNames.get(a.getProjectId())}</a>
                                @else
                                    ${projectNames.get(a.getProjectId())}
                                @endif
                            </span>
                            <span class="list-cell-muted hidden w-52 tabular-nums @2xl:block">${a.getRole() == null ? "" : a.getRole().label() + " · "}${Ui.period(a.getPeriod().startDate(), a.getPeriod().endDate())}</span>
                            <span class="w-16 shrink-0 text-right">
                                @if(a.isActiveOn(java.time.LocalDate.now()))
                                    <span class="${Seed.badge("informative")}">투입 중</span>
                                @elseif(a.getPeriod().startDate().isAfter(java.time.LocalDate.now()))
                                    <span class="${Seed.badge("neutral", "outline")}">예정</span>
                                @else
                                    <span class="${Seed.badge("neutral")}">종료</span>
                                @endif
                            </span>
                        </div>
                    @endfor
                @endif
            </div>
        </section>
        <section class="form-section">
            <div><h2 class="form-section-title">내 권한</h2><p class="form-section-desc">로그인 시점 기준입니다. 변경되면 다시 로그인하세요.</p></div>
            <div class="list-box">
                @for(Permission p : permissions)
                    !{var code = PermissionCode.fromCode(p.getCode());}
                    !{java.util.Set<PermissionScope> scopes = code.isPresent() ? ctx.requireUser().scopes(code.get()) : java.util.Set.of();}
                    <div class="list-row min-h-10">
                        <span class="min-w-0 flex-1 truncate ${scopes.isEmpty() ? "text-fg-neutral-subtle" : "text-fg-neutral"}">${p.getName()}</span>
                        <span class="flex flex-wrap justify-end gap-x1">
                            @if(scopes.isEmpty())
                                <span class="t3-regular text-fg-neutral-subtle">없음</span>
                            @else
                                @for(PermissionScope s : scopes)
                                    <span class="${Seed.badge("neutral", "outline")}">${s.label()}</span>
                                @endfor
                            @endif
                        </span>
                    </div>
                @endfor
            </div>
        </section>
    </div>
`)
