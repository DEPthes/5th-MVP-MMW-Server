package com.wvw.mmw.global.config;

import com.wvw.mmw.domain.auth.jwt.JwtAuthenticationFilter;
import com.wvw.mmw.global.security.JwtAccessDeniedHandler;
import com.wvw.mmw.global.security.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint authenticationEntryPoint,
            JwtAccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .requestCache(cache -> cache.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/v1/auth/signup",
                                "/api/v1/auth/login",
                                "/api/v1/auth/reissue"
                        ).permitAll()
                        .requestMatchers(
                                "/api/v1/users/me",
                                "/api/v1/auth/session",
                                "/api/v1/auth/logout",
                                "/api/v1/auth/password",
                                "/api/v1/application-profiles/**",
                                "/api/v1/interviews/**"
                        ).authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
