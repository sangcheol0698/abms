package kr.co.abacus.abms.assistant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.security.LoginUser;

@Controller
@RequestMapping("/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal LoginUser user, Model model) {
        model.addAttribute("sessions", assistantService.sessions(user));
        return "assistant/index";
    }

    @GetMapping("/{id}")
    public String session(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        model.addAttribute("sessions", assistantService.sessions(user));
        model.addAttribute("current", assistantService.session(user, id));
        model.addAttribute("messages", assistantService.messages(user, id));
        return "assistant/index";
    }

    /** 첫 메시지로 새 대화를 시작한다. */
    @PostMapping
    public String start(@AuthenticationPrincipal LoginUser user, @RequestParam String message,
                        HttpServletRequest request, HttpServletResponse response) {
        ChatSession session = assistantService.startSession(user, message);
        assistantService.ask(user, session.id(), message);
        return Htmx.redirect(request, response, "/assistant/" + session.id(), new Toast("info", "새 대화를 시작했습니다."));
    }

    @PostMapping("/{id}/messages")
    public String ask(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, @RequestParam String message, Model model) {
        AssistantService.Exchange exchange = assistantService.ask(user, id, message);
        model.addAttribute("question", exchange.question());
        model.addAttribute("answer", exchange.answer());
        return "assistant/exchange";
    }

    @PostMapping("/{id}/rename")
    public String rename(@AuthenticationPrincipal LoginUser user, @PathVariable Long id,
                         @RequestParam(required = false) @Nullable String title,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        if (title != null && !title.isBlank()) {
            assistantService.rename(user, id, title);
            Toast.success(redirect, "제목을 변경했습니다.");
        }
        return "redirect:/assistant/" + id;
    }

    @PostMapping("/{id}/favorite")
    public String favorite(@AuthenticationPrincipal LoginUser user, @PathVariable Long id) {
        assistantService.toggleFavorite(user, id);
        return "redirect:/assistant/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {
        assistantService.delete(user, id);
        Toast.success(redirect, "대화를 삭제했습니다.");
        return "redirect:/assistant";
    }

}
