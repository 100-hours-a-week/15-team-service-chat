package com.sipomeokjo.commitme.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final AccessTokenProvider accessTokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!accessTokenProvider.validateToken(token)) {
            log.debug(
                    "[JWT] invalid token method={} uri={}",
                    request.getMethod(),
                    request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = accessTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        List<String> candidates =
                Arrays.stream(cookies)
                        .filter(cookie -> "access_token".equals(cookie.getName()))
                        .map(Cookie::getValue)
                        .filter(value -> !value.isBlank())
                        .toList();

        if (candidates.isEmpty()) {
            return null;
        }

        if (candidates.size() == 1) {
            return candidates.getFirst();
        }

        log.debug(
                "[JWT] duplicated_access_token_cookie method={} uri={} count={}",
                request.getMethod(),
                request.getRequestURI(),
                candidates.size());
        return selectLatestIssuedToken(candidates);
    }

    private String selectLatestIssuedToken(List<String> candidates) {
        String selectedToken = null;
        Instant selectedIssuedAt = null;

        for (String candidate : candidates) {
            if (!accessTokenProvider.validateToken(candidate)) {
                continue;
            }

            Instant issuedAt = accessTokenProvider.getIssuedAt(candidate);
            if (issuedAt == null) {
                continue;
            }

            if (selectedToken == null || issuedAt.isAfter(selectedIssuedAt)) {
                selectedToken = candidate;
                selectedIssuedAt = issuedAt;
            }
        }

        return selectedToken != null ? selectedToken : candidates.getFirst();
    }
}
