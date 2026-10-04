@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@param Department department
@param DepartmentTree tree
@param java.util.List<Employee> members
@param int subtreeMemberCount
@param Employee leader = null
@param kr.co.abacus.abms.site.Site site = null
!{Department parent = department.getParentId() == null ? null : tree.get(department.getParentId());}
<dl class="property-list">
    <dt>코드</dt>
    <dd class="tabular-nums">${department.getCode()}</dd>
    <dt>유형</dt>
    <dd>${department.getType().label()}</dd>
    <dt>부서장</dt>
    <dd>
        @if(leader != null)
            <a href="/employees/${leader.id()}" class="link inline-flex items-center gap-x1_5">@template.components.avatar(avatar = leader.getAvatar(), initial = leader.initial(), size = "size-5 text-[10px]")${leader.getName()}</a>
        @else
            <span class="text-fg-neutral-subtle">미지정</span>
        @endif
    </dd>
    <dt>상위 부서</dt>
    <dd>
        @if(parent != null)
            <a class="link" href="/departments?selected=${parent.id()}">${parent.getName()}</a>
        @else
            <span class="text-fg-neutral-subtle">없음</span>
        @endif
    </dd>
    <dt>사업장</dt>
    <dd>
        @if(site != null)
            <a class="link" href="/sites/${site.id()}" title="${site.getLocation().fullAddress()}">${site.getName()}</a>
        @else
            <span class="text-fg-neutral-subtle">미지정</span>
        @endif
    </dd>
    <dt>소속 인원</dt>
    <dd class="tabular-nums">${members.size()}명</dd>
    <dt>하위 포함</dt>
    <dd class="tabular-nums">${subtreeMemberCount}명</dd>
</dl>
