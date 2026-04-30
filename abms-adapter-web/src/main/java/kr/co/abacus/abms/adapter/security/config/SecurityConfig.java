package kr.co.abacus.abms.adapter.security.config;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import kr.co.abacus.abms.adapter.observability.RequestTracingFilter;
import kr.co.abacus.abms.adapter.observability.SecurityEventRecorder;
import kr.co.abacus.abms.adapter.security.filter.CsrfCookieFilter;

@EnableMethodSecurity
@Configuration
public class SecurityConfig {

    private final SessionRegistry sessionRegistry;
    private final RequestTracingFilter requestTracingFilter;
    private final SecurityEventRecorder securityEventRecorder;
    private final List<IpAddressMatcher> actuatorIpMatchers;

    public SecurityConfig(
            SessionRegistry sessionRegistry,
            RequestTracingFilter requestTracingFilter,
            SecurityEventRecorder securityEventRecorder,
            @Value("${app.observability.actuator.allowed-ip-ranges:127.0.0.1/32,::1/128,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16}") String actuatorAllowedIpRanges
    ) {
        this.sessionRegistry = sessionRegistry;
        this.requestTracingFilter = requestTracingFilter;
        this.securityEventRecorder = securityEventRecorder;
        this.actuatorIpMatchers = Arrays.stream(actuatorAllowedIpRanges.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(IpAddressMatcher::new)
                .toList();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()))
                .csrf(csrfConfig -> csrfConfig
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                )
                .addFilterBefore(requestTracingFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                .securityContext(securityContext -> securityContext
                        .requireExplicitSave(false)
                )
                .sessionManagement(sessionConfig -> sessionConfig
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.changeSessionId())
                        .maximumSessions(2)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry)
                        .expiredSessionStrategy(event -> {
                            securityEventRecorder.recordSessionExpired(event.getRequest());
                            event.getResponse().sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session Expired");
                        })
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/registration-requests",
                                "/api/auth/registration-confirmations",
                                "/api/auth/password-reset-requests",
                                "/api/auth/password-reset-confirmations",
                                "/api/auth/logout",
                                "/actuator/health",
                                "/actuator/info",
                                "/api/csrf"
                        ).permitAll()
                        .requestMatchers("/actuator/health/**", "/actuator/prometheus", "/actuator/metrics", "/actuator/metrics/**")
                        .access(actuatorIpRangeOnly())
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            securityEventRecorder.recordUnauthorized(request);
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            securityEventRecorder.recordAccessDenied(request);
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                        })
                );

        return http.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://3.37.131.192"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/assets/**")
                .requestMatchers("/favicon.ico")
                .requestMatchers("/error");
    }

    private AuthorizationManager<RequestAuthorizationContext> actuatorIpRangeOnly() {
        return new AuthorizationManager<>() {
            @Override
            public AuthorizationDecision authorize(
                    Supplier<? extends Authentication> authentication,
                    RequestAuthorizationContext object
            ) {
                boolean allowed = actuatorIpMatchers.stream()
                        .anyMatch(matcher -> matcher.matches(object.getRequest()));
                return new AuthorizationDecision(allowed);
            }
        };
    }

}
