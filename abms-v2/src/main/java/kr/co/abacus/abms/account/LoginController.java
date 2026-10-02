package kr.co.abacus.abms.account;

import java.util.Arrays;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final boolean demo;

    public LoginController(Environment environment) {
        this.demo = Arrays.stream(environment.getActiveProfiles()).anyMatch(p -> p.equals("demo") || p.equals("local"));
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, @RequestParam(required = false) String locked,
                        @RequestParam(required = false) String disabled, @RequestParam(required = false) String logout,
                        Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }
        if (error != null) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
        } else if (locked != null) {
            model.addAttribute("error", "로그인 실패가 " + Account.MAX_LOGIN_FAILURES + "회 누적되어 계정이 잠겼습니다. 관리자에게 문의하세요.");
        } else if (disabled != null) {
            model.addAttribute("error", "비활성화된 계정입니다. 관리자에게 문의하세요.");
        } else if (logout != null) {
            model.addAttribute("info", "로그아웃되었습니다.");
        }
        model.addAttribute("demo", demo);
        return "auth/login";
    }

}
