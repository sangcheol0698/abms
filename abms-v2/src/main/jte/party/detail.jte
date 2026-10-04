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
!{Money totalContract = projects.stream().map(Project::getContractAmount).reduce(Money.ZERO, Money::plus);}
@template.layout.app(ctx = ctx, aside = @`
            <dl class="property-list">
                <dt>대표자</dt>
                <dd>${party.getCeoName() == null ? "-" : party.getCeoName()}</dd>
                <dt>영업 담당자</dt>
                <dd>${party.getSalesRepName() == null ? "-" : party.getSalesRepName()}</dd>
                <dt>연락처</dt>
                <dd class="tabular-nums">${party.getSalesRepPhone() == null ? "-" : party.getSalesRepPhone()}</dd>
                <dt>이메일</dt>
                <dd>
                    @if(party.getSalesRepEmail() != null)
                        <a class="link" href="mailto:${party.getSalesRepEmail()}" title="${party.getSalesRepEmail()}">${party.getSalesRepEmail()}</a>
                    @else
                        -
                    @endif
                </dd>
                <dt>프로젝트</dt>
                <dd class="tabular-nums">${projectCount}건</dd>
                <dt>누적 계약금액</dt>
                <dd class="t4-bold tabular-nums">${Ui.won(totalContract)}원</dd>
            </dl>
`, title = party.getName(), content = @`
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
        <section class="min-w-0">
            <h2 class="section-title">프로젝트 <span class="section-count">${projects.size()}</span></h2>
            <div class="list-box">
                @if(projects.isEmpty())
                    @template.components.empty(message = ctx.can(PermissionCode.PROJECT_READ) ? "조회 가능한 프로젝트가 없습니다." : "프로젝트 조회 권한이 없습니다.", icon = "folder")
                @else
                    @for(Project p : projects)
                        @template.components.projectRow(project = p, tree = tree)
                    @endfor
                @endif
            </div>
        </section>

`)
