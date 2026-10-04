package kr.co.abacus.abms.party;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.party.PartyContact.ContactInfo;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 협력사 담당자 등록·수정(모달)·삭제. 협력사 관리 권한이 필요하다.
 */
@Controller
@RequestMapping("/parties/{partyId}/contacts")
public class PartyContactController {

    private final PartyService partyService;

    public PartyContactController(PartyService partyService) {
        this.partyService = partyService;
    }

    @GetMapping("/new")
    public String createModal(@AuthenticationPrincipal LoginUser user, @PathVariable Long partyId, Model model) {
        requireWrite(user);
        return modal(model, partyService.get(partyId), null, new ContactForm(null, ContactRole.SALES, null, null, null, null, false), FormErrors.none());
    }

    @PostMapping
    public String create(@AuthenticationPrincipal LoginUser user, @PathVariable Long partyId, ContactForm form, Model model,
                         HttpServletRequest request, HttpServletResponse response) {
        requireWrite(user);
        try {
            PartyContact contact = partyService.addContact(partyId, form.toInfo());
            return Htmx.redirect(request, response, "/parties/" + partyId, new Toast("success", contact.getName() + " 담당자를 추가했습니다."));
        } catch (BusinessException e) {
            response.setStatus(422);
            return modal(model, partyService.get(partyId), null, form, FormErrors.global(e.getMessage()));
        }
    }

    @GetMapping("/{contactId}/edit")
    public String editModal(@AuthenticationPrincipal LoginUser user, @PathVariable Long partyId, @PathVariable Long contactId, Model model) {
        requireWrite(user);
        PartyContact c = partyService.contact(partyId, contactId);
        return modal(model, partyService.get(partyId), c,
                new ContactForm(c.getName(), c.getRole(), c.getTitle(), c.getPhone(), c.getEmail(), c.getMemo(), c.isPrimary()), FormErrors.none());
    }

    @PostMapping("/{contactId}")
    public String update(@AuthenticationPrincipal LoginUser user, @PathVariable Long partyId, @PathVariable Long contactId, ContactForm form,
                         Model model, HttpServletRequest request, HttpServletResponse response) {
        requireWrite(user);
        PartyContact contact = partyService.contact(partyId, contactId);
        try {
            partyService.updateContact(partyId, contactId, form.toInfo());
            return Htmx.redirect(request, response, "/parties/" + partyId, new Toast("success", "담당자 정보를 수정했습니다."));
        } catch (BusinessException e) {
            response.setStatus(422);
            return modal(model, partyService.get(partyId), contact, form, FormErrors.global(e.getMessage()));
        }
    }

    @PostMapping("/{contactId}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long partyId, @PathVariable Long contactId,
                         RedirectAttributes redirect) {
        requireWrite(user);
        PartyContact contact = partyService.contact(partyId, contactId);
        partyService.deleteContact(partyId, contactId, user.accountId());
        Toast.success(redirect, contact.getName() + " 담당자를 삭제했습니다.");
        return "redirect:/parties/" + partyId;
    }

    private String modal(Model model, Party party, @Nullable PartyContact contact, ContactForm form, FormErrors errors) {
        model.addAttribute("party", party);
        model.addAttribute("contact", contact);
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        return "party/contactModal";
    }

    private static void requireWrite(LoginUser user) {
        if (!user.has(PermissionCode.PARTY_WRITE)) {
            throw new AccessDeniedException("협력사 관리 권한이 없습니다.");
        }
    }

    public record ContactForm(@Nullable String name, @Nullable ContactRole role, @Nullable String title, @Nullable String phone,
                              @Nullable String email, @Nullable String memo, @Nullable Boolean primary) {

        /** 체크박스를 해제하면 값이 전송되지 않는다. */
        boolean isPrimary() {
            return Boolean.TRUE.equals(primary);
        }

        ContactInfo toInfo() {
            if (email != null && !email.isBlank() && !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                throw new BusinessException("이메일 형식이 올바르지 않습니다: " + email);
            }
            if (name != null && name.trim().length() > 30) {
                throw new BusinessException("담당자 이름은 30자 이하로 입력하세요.");
            }
            return new ContactInfo(name, role, title, phone, email, memo, isPrimary());
        }

    }

}
