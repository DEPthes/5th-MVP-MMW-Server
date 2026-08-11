package com.wvw.mmw.domain.auth.jwt;

import com.wvw.mmw.global.security.JwtAuthenticationEntryPoint;
import com.wvw.mmw.domain.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            UserRepository userRepository,
            JwtAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(
                HttpHeaders.AUTHORIZATION
        );

        if (authorizationHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            authenticationEntryPoint.commence(request, response, null);
            return;
        }

        String token = authorizationHeader
                .substring(BEARER_PREFIX.length())
                .trim();

        if (!jwtProvider.validateAccessToken(token)) {
            authenticationEntryPoint.commence(request, response, null);
            return;
        }

        Long userId;

        try {
            userId = jwtProvider.getUserId(token);
        } catch (IllegalArgumentException exception) {
            authenticationEntryPoint.commence(request, response, null);
            return;
        }

        if (!userRepository.existsById(userId)) {
            authenticationEntryPoint.commence(request, response, null);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.emptyList()
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
}
