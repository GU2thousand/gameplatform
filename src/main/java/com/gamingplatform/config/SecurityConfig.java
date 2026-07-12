package com.gamingplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamingplatform.repository.UserProfileRepository;
import com.gamingplatform.exception.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    UserDetailsService userDetailsService(UserProfileRepository users) {
        return username -> users.findByUsername(username)
                .map(profile -> User.withUsername(profile.getUsername())
                        .password(profile.getPasswordHash() == null ? "{noop}!disabled!" : profile.getPasswordHash())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));
    }

    @Bean
    DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        repository.setCookieCustomizer(cookie -> cookie.sameSite("Strict"));
        return repository;
    }

    @Bean
    SessionAuthenticationStrategy sessionAuthenticationStrategy(CookieCsrfTokenRepository csrfRepository) {
        return new CompositeSessionAuthenticationStrategy(List.of(
                new ChangeSessionIdAuthenticationStrategy(),
                new CsrfAuthenticationStrategy(csrfRepository)
        ));
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper,
                                            CookieCsrfTokenRepository csrfRepository) throws Exception {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null);

        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(csrfHandler)
                        .ignoringRequestMatchers("/api/debug/**"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.migrateSession())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/app.js", "/styles.css", "/favicon.ico",
                                "/error", "/actuator/health/**", "/api/auth/register", "/api/auth/login",
                                "/api/auth/claim-legacy", "/api/auth/csrf", "/api/debug/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, failure) ->
                                writeError(objectMapper, response, 401, "Unauthorized", "Authentication required",
                                        request.getRequestURI()))
                        .accessDeniedHandler((request, response, failure) ->
                                writeError(objectMapper, response, 403, "Forbidden", "Access denied",
                                        request.getRequestURI())))
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .logoutSuccessHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT)));
        return http.build();
    }

    private void writeError(ObjectMapper mapper, HttpServletResponse response, int status, String error,
                            String message, String path) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), new ApiError(Instant.now(), status, error, message, path));
    }
}
