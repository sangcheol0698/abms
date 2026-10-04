@import java.util.List
@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Icons
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.common.web.Ui.SelectOption
@import kr.co.abacus.abms.common.web.ViewContext
@import kr.co.abacus.abms.project.Project
@import kr.co.abacus.abms.project.ProjectForm
@import kr.co.abacus.abms.project.ProjectStatus
@import kr.co.abacus.abms.common.web.Seed
@param ViewContext ctx
@param ProjectForm form
@param FormErrors errors
@param Project project = null
@param List<SelectOption> departmentOptions
@param List<SelectOption> partyOptions
!{boolean editing = project != null;}
!{String title = editing ? project.getName() + " 수정" : "프로젝트 등록";}
@template.layout.app(ctx = ctx, title = title, content = @`
    @template.components.pageHeader(title = title, breadcrumb = @`
        <a href="/projects" class="hover:text-fg-neutral-muted">프로젝트</a>${Icons.of("chevron-right", "size-3")}
        @if(editing)<a href="/projects/${project.id()}" class="hover:text-fg-neutral-muted">${project.getName()}</a>${Icons.of("chevron-right", "size-3")}@endif
        <span>${editing ? "수정" : "등록"}</span>
    `)
    <form method="post" action="${editing ? "/projects/" + project.id() : "/projects"}" class="card max-w-3xl" novalidate>
        <div class="card-body grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div class="sm:col-span-2">@template.components.globalError(errors = errors)</div>
            @if(editing)
                <div>
                    <label class="label" for="code">프로젝트 코드</label>
                    <input id="code" class="input" value="${project.getCode()}" disabled>
                </div>
            @else
                @template.components.input(name = "code", label = "프로젝트 코드", value = Ui.value(form.code()), errors = errors, required = true, help = "자동 제안된 코드를 그대로 써도 됩니다.")
            @endif
            @template.components.select(name = "status", label = "상태", options = Ui.options(ProjectStatus.values()), value = Ui.value(form.status()), errors = errors, required = true)
            <div class="sm:col-span-2">
                @template.components.input(name = "name", label = "프로젝트명", value = Ui.value(form.name()), errors = errors, required = true)
            </div>
            @template.components.select(name = "partyId", label = "협력사", options = partyOptions, value = Ui.value(form.partyId()), errors = errors, required = true, emptyLabel = "협력사 선택")
            @template.components.select(name = "leadDepartmentId", label = "주관 부서", options = departmentOptions, value = Ui.value(form.leadDepartmentId()), errors = errors, required = true, emptyLabel = "주관 부서 선택")
            @template.components.input(name = "contractAmount", label = "계약금액 (원)", value = Ui.value(form.contractAmount()), errors = errors, type = "number", required = true)
            <div></div>
            @template.components.input(name = "startDate", label = "시작일", value = Ui.value(form.startDate()), errors = errors, type = "date", required = true)
            <div>
                @template.components.input(name = "endDate", label = "종료일", value = Ui.value(form.endDate()), errors = errors, type = "date", required = true)
                @if(errors.has("periodValid"))<p class="field-error">${errors.get("periodValid")}</p>@endif
            </div>
            <div class="sm:col-span-2">
                <label for="description" class="label">설명</label>
                <textarea id="description" name="description" rows="3" class="textarea">${Ui.value(form.description())}</textarea>
            </div>
            <p class="text-xs text-fg-neutral-subtle sm:col-span-2">손익은 투입 직원의 소속 부서가 아니라 <b>주관 부서</b>에 귀속됩니다.</p>
        </div>
        <div class="flex justify-end gap-2 rounded-b-xl border-t border-stroke-neutral-subtle bg-bg-neutral-muted px-5 py-3.5">
            <a href="${editing ? "/projects/" + project.id() : "/projects"}" class="${Seed.button("neutralWeak")}">취소</a>
            <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "등록"}</button>
        </div>
    </form>
`)
