package com.nihongoapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class BannedUserFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(BannedUserFilter.class);
    private static final String BANNED_SET_KEY = "banned_user_ids";

    private static final String[] FINANCIAL_PREFIXES = {
            "/api/v1/wallet/",
            "/api/v1/gacha/",
            "/api/v1/iap/"
    };

    private final StringRedisTemplate redisTemplate;

    public BannedUserFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String prefix : FINANCIAL_PREFIXES) {
            if (path.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = extractUserId(auth);
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Boolean isBanned = redisTemplate.opsForSet().isMember(BANNED_SET_KEY, userId.toString());
            if (Boolean.TRUE.equals(isBanned)) {
                String correlationId = (String) request.getAttribute("correlationId");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                String body = "{\"error_code\":\"ACCOUNT_BANNED\",\"message\":\"Tai khoan da bi khoa.\",\"correlation_id\":\"%s\"}"
                        .formatted(correlationId != null ? correlationId : "unknown");
                response.getWriter().write(body);
                return;
            }
        } catch (Exception e) {
            log.warn("Redis banned set check unavailable, fail-open: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private Long extractUserId(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (principal instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
