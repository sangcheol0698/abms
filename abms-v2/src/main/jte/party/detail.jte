@import java.util.List
@import kr.co.abacus.abms.access.PermissionCode
@import kr.co.abacus.abms.common.domain.Money
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.party.Party
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param Party party
@param List<Project> projects
@param long projectCount
@param DepartmentTree tree
@template.layout.app(ctx = ctx, title = party.getName(), content = @`
    @template.components.pageHeader(title = party.getName(), breadcrumb = @`
        <a href="/parties" class="hover:text-fg-neutral-muted">협력사</a>${Icons.of("chevron-right", "size-3")}<span>${party.getName()}</span>
    `, actions = @`
        @if(ctx.can(PermissionCode.PARTY_WRITE))
            <a href="/parties/${party.id()}/edit" class="${Seed.button("neutralOutline")}">${Icons.of("pencil", "size-4")} 수정</a>
            @if(projectCount == 0)
                <form method="post" action="/parties/${party.id()}/delete" hx-confirm="${party.getName()} 협력사를 삭제할까요?">
                    <button class="${Seed.button("neutralOutline")} text-fg-critical">${Icons.of("trash", "size-4")} 삭제</button>
                </form>
            @endif
        @endif
    `)
    <div class="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <section class="card">
            <div class="card-header"><h2 class="card-title">기본 정보</h2></div>
            <dl class="card-body space-y-3 text-sm">
                <div class="flex justify-between gap-4"><dt class="text-fg-neutral-subtle">대표자</dt><dd>${Ui.value(party.getCeoName())}</dd></div>
                <div class="flex justify-between gap-4"><dt class="text-fg-neutral-subtle">영업 담당자</dt><dd>${Ui.value(party.getSalesRepName())}</dd></div>
                <div class="flex justify-between gap-4"><dt class="text-fg-neutral-subtle">연락처</dt><dd>${Ui.value(party.getSalesRepPhone())}</dd></div>
                <div class="flex justify-between gap-4"><dt class="text-fg-neutral-subtle">이메일</dt><dd class="truncate">${Ui.value(party.getSalesRepEmail())}</dd></div>
                <div class="flex justify-between gap-4"><dt class="text-fg-neutral-subtle">프로젝트 수</dt><dd>${projectCount}건</dd></div>
                <div class="flex justify-between gap-4"><dt class="text-fg-neutral-subtle">누적 계약금액</dt><dd class="font-semibold tabular-nums">${Ui.won(projects.stream().map(Project::getContractAmount).reduce(Money.ZERO, Money::plus))}원</dd></div>
            </dl>
        </section>
        <section class="card overflow-hidden xl:col-span-2">
            <div class="card-header"><h2 class="card-title">프로젝트</h2></div>
            @if(projects.isEmpty())
                @template.components.empty(message = ctx.can(PermissionCode.PROJECT_READ) ? "조회 가능한 프로젝트가 없습니다." : "프로젝트 조회 권한이 없습니다.")
            @else
                <div class="overflow-x-auto">
                    <table class="table">
                        <thead><tr><th>프로젝트</th><th>주관 부서</th><th>상태</th><th>기간</th><th class="num">계약금액</th></tr></thead>
                        <tbody class="divide-y divide-stroke-neutral-subtle">
                        @for(Project p : projects)
                            <tr>
                                <td><a href="/projects/${p.id()}" class="link">${p.getName()}</a><p class="text-xs text-fg-placeholder">${p.getCode()}</p></td>
                                <td>${tree.nameOf(p.getLeadDepartmentId())}</td>
                                <td><span class="${Ui.badge(p.getStatus())}">${p.getStatus().label()}</span></td>
                                <td class="tabular-nums">${Ui.period(p.getPeriod().startDate(), p.getPeriod().endDate())}</td>
                                <td class="num">${p.getContractAmount().compact()}</td>
                            </tr>
                        @endfor
                        </tbody>
                    </table>
                </div>
            @endif
        </section>
    </div>
`)
