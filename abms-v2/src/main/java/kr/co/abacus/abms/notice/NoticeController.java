package kr.co.abacus.abms.notice;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.web.FormErrors;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.PageView;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.notice.Notice.NoticeInfo;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 공지사항 목록·상세·작성과 안내 팝업.
 */
@Controller
@RequestMapping("/notices")
public class NoticeController {

    /** '닫기'로 이번 로그인 동안 숨긴 팝업 공지 id (세션) */
    static final String CLOSED_POPUPS = NoticeController.class.getName() + ".closedPopups";

    private static final int PAGE_SIZE = 20;

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal LoginUser user, @RequestParam(defaultValue = "0") int page, HttpServletRequest request, Model model) {
        var result = noticeService.list(user, PageRequest.of(Math.max(page, 0), PAGE_SIZE));
        String baseUrl = UriComponentsBuilder.fromPath("/notices").query(request.getQueryString()).build().toUriString();
        model.addAttribute("page", PageView.of(result, baseUrl));
        model.addAttribute("readIds", noticeService.readIds(user.accountId(), result.getContent().stream().map(Notice::id).toList()));
        model.addAttribute("canManage", NoticeService.canManage(user));
        model.addAttribute("now", LocalDateTime.now());
        return "notice/list";
    }

    @GetMapping("/{id}")
    public String detail(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, HttpSession session, Model model) {
        model.addAttribute("notice", noticeService.open(user, id));
        // 상세를 연 공지는 이번 로그인 동안 안내 팝업으로 다시 띄우지 않는다.
        closedPopups(session).add(id);
        model.addAttribute("canManage", NoticeService.canManage(user));
        model.addAttribute("now", LocalDateTime.now());
        return "notice/detail";
    }

    @GetMapping("/new")
    public String createForm(@AuthenticationPrincipal LoginUser user, Model model) {
        requireManage(user);
        return form(model, NoticeForm.empty(), FormErrors.none(), null);
    }

    @PostMapping
    public String create(@AuthenticationPrincipal LoginUser user, @ModelAttribute("form") NoticeForm form, Model model,
                         HttpServletResponse response, RedirectAttributes redirect) {
        requireManage(user);
        try {
            Notice notice = noticeService.create(user, form.toInfo());
            Toast.success(redirect, "공지를 등록했습니다." + (notice.isPopup() ? " 게시 기간 동안 안내 팝업으로 보여줍니다." : ""));
            return "redirect:/notices/" + notice.id();
        } catch (BusinessException e) {
            response.setStatus(422);
            return form(model, form, FormErrors.global(e.getMessage()), null);
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, Model model) {
        requireManage(user);
        Notice notice = noticeService.get(id);
        return form(model, NoticeForm.of(notice), FormErrors.none(), notice);
    }

    @PostMapping("/{id}")
    public String update(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, @ModelAttribute("form") NoticeForm form, Model model,
                         HttpServletResponse response, RedirectAttributes redirect) {
        requireManage(user);
        Notice notice = noticeService.get(id);
        try {
            noticeService.update(user, id, form.toInfo());
            Toast.success(redirect, "공지를 수정했습니다.");
            return "redirect:/notices/" + id;
        } catch (BusinessException e) {
            response.setStatus(422);
            return form(model, form, FormErrors.global(e.getMessage()), notice);
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, RedirectAttributes redirect) {
        noticeService.delete(user, id);
        Toast.success(redirect, "공지를 삭제했습니다.");
        return "redirect:/notices";
    }

    /** 지금 띄울 안내 팝업 (모달 내용). 없으면 204 로 아무것도 띄우지 않는다. */
    @GetMapping("/popup")
    public String popup(@AuthenticationPrincipal LoginUser user, HttpSession session, HttpServletResponse response, Model model) {
        return renderNextPopup(user, session, response, model, false);
    }

    /**
     * 팝업 닫기. mode: close(이번 로그인 동안) / today(오늘 하루) / forever(다시 보지 않기).
     * 닫은 뒤 다음 팝업이 있으면 이어서 보여주고, 없으면 모달을 닫는다.
     */
    @PostMapping("/{id}/popup/dismiss")
    public String dismiss(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, @RequestParam(defaultValue = "close") String mode,
                          HttpSession session, HttpServletResponse response, Model model) {
        switch (mode) {
            case "today" -> noticeService.hidePopupToday(user.accountId(), id);
            case "forever" -> noticeService.hidePopupForever(user.accountId(), id);
            default -> { }
        }
        closedPopups(session).add(id);
        return renderNextPopup(user, session, response, model, true);
    }

    /** @param closeWhenEmpty 팝업을 닫는 요청일 때만 모달을 닫는다. (처음 확인할 때 다른 모달을 닫지 않도록) */
    private String renderNextPopup(LoginUser user, HttpSession session, HttpServletResponse response, Model model, boolean closeWhenEmpty) {
        var next = noticeService.nextPopup(user.accountId(), closedPopups(session));
        if (next.isEmpty()) {
            if (closeWhenEmpty) {
                Htmx.trigger(response, "{\"closeModal\":true}");
            }
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return "fragments/empty";
        }
        model.addAttribute("notice", next.get());
        return "notice/popup";
    }

    @SuppressWarnings("unchecked")
    public static Set<Long> closedPopups(HttpSession session) {
        Object value = session.getAttribute(CLOSED_POPUPS);
        if (value instanceof Set<?> set) {
            return (Set<Long>) set;
        }
        Set<Long> created = new HashSet<>();
        session.setAttribute(CLOSED_POPUPS, created);
        return created;
    }

    private String form(Model model, NoticeForm form, FormErrors errors, @Nullable Notice notice) {
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        model.addAttribute("notice", notice);
        return "notice/form";
    }

    private static void requireManage(LoginUser user) {
        if (!NoticeService.canManage(user)) {
            throw new org.springframework.security.access.AccessDeniedException("'계정 관리' 권한이 있어야 공지를 관리할 수 있습니다.");
        }
    }

    public record NoticeForm(
            @Nullable String title,
            @Nullable String body,
            @Nullable NoticeImportance importance,
            @Nullable Boolean pinned,
            @Nullable Boolean popup,
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") @Nullable LocalDateTime startsAt,
            @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") @Nullable LocalDateTime endsAt
    ) {

        static NoticeForm empty() {
            return new NoticeForm(null, null, NoticeImportance.NORMAL, false, false, null, null);
        }

        static NoticeForm of(Notice n) {
            return new NoticeForm(n.getTitle(), n.getBody(), n.getImportance(), n.isPinned(), n.isPopup(), n.getStartsAt(), n.getEndsAt());
        }

        /** datetime-local 입력값 (yyyy-MM-ddTHH:mm) */
        public static String inputValue(@Nullable LocalDateTime value) {
            return value == null ? "" : value.withSecond(0).withNano(0).toString();
        }

        NoticeInfo toInfo() {
            return new NoticeInfo(title, body, importance, Boolean.TRUE.equals(pinned), Boolean.TRUE.equals(popup), startsAt, endsAt);
        }

    }

}
