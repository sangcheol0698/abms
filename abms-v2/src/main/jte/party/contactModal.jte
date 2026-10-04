@import kr.co.abacus.abms.common.web.FormErrors
@import kr.co.abacus.abms.common.web.Seed
@import kr.co.abacus.abms.common.web.Ui
@import kr.co.abacus.abms.party.ContactRole
@import kr.co.abacus.abms.party.Party
@import kr.co.abacus.abms.party.PartyContact
@import kr.co.abacus.abms.party.PartyContactController.ContactForm
@param Party party
@param PartyContact contact = null
@param ContactForm form
@param FormErrors errors
!{boolean editing = contact != null;}
<form hx-post="${editing ? "/parties/" + party.id() + "/contacts/" + contact.id() : "/parties/" + party.id() + "/contacts"}" hx-target="#modal-body">
    @template.components.modal(title = editing ? "담당자 수정" : "담당자 추가", context = party.getName(), hint = editing ? "저장" : "추가", body = @`
        @template.components.globalError(errors = errors)
        <input name="name" value="${Ui.value(form.name())}" required autofocus maxlength="30" placeholder="담당자 이름" aria-label="담당자 이름"
               class="mb-x4 w-full bg-transparent t7-bold text-fg-neutral outline-none placeholder:text-fg-placeholder">
        <div class="prop-form">
            @template.components.propRow(label = "역할", forId = "role", control = @`
                <select id="role" name="role" class="select">
                    @for(var o : Ui.options(ContactRole.values()))
                        <option value="${o.value()}" selected="${o.value().equals(Ui.value(form.role()))}">${o.label()}</option>
                    @endfor
                </select>
            `)
            @template.components.propRow(label = "부서·직책", forId = "title", control = @`
                <input id="title" name="title" class="input" maxlength="30" value="${Ui.value(form.title())}" placeholder="예: 구매팀 과장">
            `)
            @template.components.propRow(label = "연락처", forId = "phone", control = @`
                <input id="phone" name="phone" type="tel" class="input" maxlength="20" value="${Ui.value(form.phone())}" placeholder="010-0000-0000">
            `)
            @template.components.propRow(label = "이메일", forId = "email", control = @`
                <input id="email" name="email" type="email" class="input" maxlength="100" value="${Ui.value(form.email())}">
            `)
            @template.components.propRow(label = "메모", forId = "memo", control = @`
                <textarea id="memo" name="memo" rows="2" maxlength="500" class="textarea" placeholder="연락 가능 시간, 업무 범위 등">${Ui.value(form.memo())}</textarea>
            `)
            @template.components.propRow(label = "대표 담당자", forId = "primary", control = @`
                <label class="inline-flex items-center gap-x2 t4-regular text-fg-neutral-muted">
                    <input id="primary" name="primary" type="checkbox" value="true" checked="${Boolean.TRUE.equals(form.primary())}"> 목록에 표시되는 대표 연락 창구
                </label>
            `)
        </div>
    `, footer = @`
        <button type="button" class="${Seed.button("ghost")}" data-close-modal>취소</button>
        <button type="submit" class="${Seed.button("brandSolid")}">${editing ? "저장" : "추가"}</button>
    `)
</form>
