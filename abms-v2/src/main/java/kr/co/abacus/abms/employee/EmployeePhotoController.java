package kr.co.abacus.abms.employee;

import java.io.InputStream;
import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 프로필 사진 보기·등록·삭제. 사진 주소에 버전(v)이 붙어 있어 브라우저가 오래 캐시해도 된다.
 */
@Controller
@RequestMapping("/employees/{id}/photo")
public class EmployeePhotoController {

    private final EmployeePhotoService photoService;
    private final HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public EmployeePhotoController(EmployeePhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping
    public ResponseEntity<InputStreamResource> photo(@PathVariable Long id) {
        InputStream content = photoService.open(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePrivate().immutable())
                .body(new InputStreamResource(content));
    }

    @PostMapping
    public String change(@AuthenticationPrincipal LoginUser user, @PathVariable Long id,
                         @RequestParam(required = false) @Nullable MultipartFile file, @RequestParam(required = false) @Nullable String returnTo,
                         HttpServletRequest request, HttpServletResponse response) {
        byte[] jpeg = ProfileImages.normalize(file);
        Employee employee = photoService.change(user, id, jpeg);
        refreshSelf(user, employee, request, response);
        return Htmx.redirect(request, response, returnUrl(id, returnTo), new Toast("success", "프로필 사진을 바꿨습니다."));
    }

    @PostMapping("/delete")
    public String remove(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, @RequestParam(required = false) @Nullable String returnTo,
                         HttpServletRequest request, HttpServletResponse response) {
        Employee employee = photoService.remove(user, id);
        refreshSelf(user, employee, request, response);
        return Htmx.redirect(request, response, returnUrl(id, returnTo), new Toast("success", "프로필 사진을 삭제했습니다. 기본 아바타가 표시됩니다."));
    }

    private static String returnUrl(Long id, @Nullable String returnTo) {
        return "me".equals(returnTo) ? "/me" : "/employees/" + id;
    }

    /** 본인 사진이면 세션의 로그인 정보(헤더 아바타)도 바로 바꾼다. */
    private void refreshSelf(LoginUser user, Employee employee, HttpServletRequest request, HttpServletResponse response) {
        if (!employee.id().equals(user.employeeId())) {
            return;
        }
        LoginUser updated = user.withPhotoUrl(employee.photoUrl());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(updated, null, updated.getAuthorities()));
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
    }

}
