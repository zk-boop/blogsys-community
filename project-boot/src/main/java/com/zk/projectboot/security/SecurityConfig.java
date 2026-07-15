package com.zk.projectboot.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zk.projectboot.dto.ApiResponse;
import com.zk.projectboot.ratelimit.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final BlogUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;
    private final UserStatusValidationFilter userStatusValidationFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(BlogUserDetailsService userDetailsService, ObjectMapper objectMapper,
                          UserStatusValidationFilter userStatusValidationFilter,
                          RateLimitFilter rateLimitFilter) {
        this.userDetailsService = userDetailsService;
        this.objectMapper = objectMapper;
        this.userStatusValidationFilter = userStatusValidationFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookiePath("/");

        http
                .addFilterAfter(userStatusValidationFilter, SecurityContextPersistenceFilter.class)
                .addFilterAfter(rateLimitFilter, UserStatusValidationFilter.class)
                .cors()
                .and()
                .csrf()
                    .csrfTokenRepository(csrfRepository)
                    .ignoringAntMatchers("/api/auth/login", "/api/auth/register")
                .and()
                .exceptionHandling()
                    .authenticationEntryPoint((request, response, exception) ->
                            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录"))
                    .accessDeniedHandler((request, response, exception) ->
                            writeError(response, HttpServletResponse.SC_FORBIDDEN, "权限不足或 CSRF 校验失败"))
                .and()
                .sessionManagement()
                    .sessionFixation().migrateSession()
                .and()
                .authorizeRequests()
                    .antMatchers("/actuator/health").permitAll()
                    .antMatchers("/api/auth/login", "/api/auth/register", "/api/auth/user-info", "/api/auth/csrf").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/articles/**", "/api/categories/**", "/api/tags/**").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/comments/article/**", "/api/comments/replies/**", "/api/comments/*").permitAll()
                    .antMatchers(HttpMethod.GET, "/api/files/avatar/**", "/api/files/cover/**").permitAll()
                    .antMatchers("/api/admin/**").hasRole("ADMIN")
                    .antMatchers(HttpMethod.GET, "/api/comments").hasRole("ADMIN")
                    .antMatchers(HttpMethod.GET, "/api/users", "/api/users/search").hasRole("ADMIN")
                    .antMatchers(HttpMethod.PATCH, "/api/comments/*/approve", "/api/comments/*/reject").hasRole("ADMIN")
                    .antMatchers(HttpMethod.PATCH, "/api/articles/*/publish", "/api/articles/*/reject").hasRole("ADMIN")
                    .antMatchers(HttpMethod.PUT, "/api/categories/**", "/api/tags/**").hasRole("ADMIN")
                    .antMatchers(HttpMethod.DELETE, "/api/categories/**", "/api/tags/**").hasRole("ADMIN")
                    .antMatchers(HttpMethod.DELETE, "/api/files/**").hasRole("ADMIN")
                    .antMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
                .and()
                .formLogin().disable()
                .httpBasic().disable();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(message));
    }
}
