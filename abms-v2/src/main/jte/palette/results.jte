@import java.util.List
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.department.Department
@import kr.co.abacus.abms.department.DepartmentTree
@import kr.co.abacus.abms.employee.Employee
@import kr.co.abacus.abms.palette.CommandPaletteController.Command
@import kr.co.abacus.abms.party.Party
@import kr.co.abacus.abms.project.Project
@param String q
@param List<Command> commands
@param List<Employee> employees
@param List<Project> projects
@param List<Department> departments
@param List<Party> parties
@param DepartmentTree tree = null
!{String group = null;}
@if(commands.isEmpty() && employees.isEmpty() && projects.isEmpty() && departments.isEmpty() && parties.isEmpty())
    <p class="px-x4 py-x8 text-center t4-regular text-fg-neutral-subtle">'${q}'에 해당하는 결과가 없습니다.</p>
@endif

@if(!employees.isEmpty())
    <p class="palette-group">직원</p>
    @for(Employee e : employees)
        <a href="/employees/${e.id()}" class="palette-item" data-palette-item>
            @template.components.avatar(photoUrl = e.photoUrl(), name = e.getName(), size = "size-5")
            <span class="truncate">${e.getName()}</span>
            <span class="truncate t3-regular text-fg-neutral-subtle">${tree == null ? "" : tree.nameOf(e.getDepartmentId())} · ${e.getEmail()}</span>
        </a>
    @endfor
@endif
@if(!projects.isEmpty())
    <p class="palette-group">프로젝트</p>
    @for(Project p : projects)
        <a href="/projects/${p.id()}" class="palette-item" data-palette-item>
            ${Icons.of("folder", "size-4 text-fg-neutral-subtle")}
            <span class="truncate">${p.getName()}</span>
            <span class="t3-regular text-fg-neutral-subtle">${p.getCode()}</span>
            <span class="ml-auto ${Ui.badge(p.getStatus())}">${p.getStatus().label()}</span>
        </a>
    @endfor
@endif
@if(!departments.isEmpty())
    <p class="palette-group">부서</p>
    @for(Department d : departments)
        <a href="/departments/${d.id()}" class="palette-item" data-palette-item>
            ${Icons.of("building", "size-4 text-fg-neutral-subtle")}
            <span class="truncate">${d.getName()}</span>
            <span class="t3-regular text-fg-neutral-subtle">${d.getCode()}</span>
        </a>
    @endfor
@endif
@if(!parties.isEmpty())
    <p class="palette-group">협력사</p>
    @for(Party p : parties)
        <a href="/parties/${p.id()}" class="palette-item" data-palette-item>
            ${Icons.of("briefcase", "size-4 text-fg-neutral-subtle")}
            <span class="truncate">${p.getName()}</span>
        </a>
    @endfor
@endif

@for(Command c : commands)
    @if(!c.group().equals(group))
        !{group = c.group();}
        <p class="palette-group">${c.group()}</p>
    @endif
    @if(c.href() != null)
        <a href="${c.href()}" class="palette-item" data-palette-item>
            ${Icons.of(c.icon(), "size-4 text-fg-neutral-subtle")}
            <span class="truncate">${c.label()}</span>
            @if(c.shortcut() != null)
                <span class="ml-auto flex gap-x1">@for(String key : c.shortcut().split(" "))<span class="kbd">${key}</span>@endfor</span>
            @endif
        </a>
    @else
        <button type="button" class="palette-item" data-palette-item data-palette-action="${c.action()}">
            ${Icons.of(c.icon(), "size-4 text-fg-neutral-subtle")}
            <span class="truncate">${c.label()}</span>
        </button>
    @endif
@endfor
