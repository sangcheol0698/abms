package kr.co.abacus.abms.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.web.Htmx;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error", "/favicon.ico", "/favicon.svg").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers("/admin/permission-groups/**").hasAuthority(PermissionCode.PERMISSION_GROUP_MANAGE.code())
                        .requestMatchers("/admin/accounts/**").hasAuthority(PermissionCode.ACCOUNT_MANAGE.code())
                        .requestMatchers("/admin/cost-policies/**").hasAuthority(PermissionCode.SUMMARY_MANAGE.code())
                        .requestMatchers("/reports/**").hasAuthority(PermissionCode.REPORT_READ.code())
                        .requestMatchers("/actuator/**").hasAuthority(PermissionCode.PERMISSION_GROUP_MANAGE.code())
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .failureHandler(loginFailureHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(htmxAwareEntryPoint())
                        .accessDeniedHandler(htmxAwareAccessDeniedHandler()))
                .headers(headers -> headers
                        .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
        return new DefaultAuthenticationEventPublisher(publisher);
    }

    private AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            String reason = switch (exception) {
                case LockedException e -> "locked";
                case DisabledException e -> "disabled";
                default -> "error";
            };
            response.sendRedirect(request.getContextPath() + "/login?" + reason);
        };
    }

    /** HTMX 요청에서 세션이 만료되면 부분 응답 대신 로그인 페이지로 전체 이동시킨다. */
    private AuthenticationEntryPoint htmxAwareEntryPoint() {
        LoginUrlAuthenticationEntryPoint delegate = new LoginUrlAuthenticationEntryPoint("/login");
        return (request, response, authException) -> {
            if (Htmx.isHtmx(request)) {
                response.setHeader("HX-Redirect", request.getContextPath() + "/login");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            delegate.commence(request, response, authException);
        };
    }

    private AccessDeniedHandler htmxAwareAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            if (Htmx.isHtmx(request)) {
                Htmx.toast(response, Htmx.ToastType.ERROR, "권한이 없습니다.");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        };
    }

}
