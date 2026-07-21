package com.nihongoapp.config;

import com.nihongoapp.common.util.CryptoUtil;
import com.nihongoapp.user.model.User;
import com.nihongoapp.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final CryptoUtil cryptoUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(CryptoUtil cryptoUtil, UserRepository userRepository) {
        this.cryptoUtil = cryptoUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        Long userId;
        try {
            userId = cryptoUtil.extractUserId(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (userId == null || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null || !cryptoUtil.isTokenValid(token, String.valueOf(user.getId()))) {
            filterChain.doFilter(request, response);
            return;
        }

        var authToken = new UsernamePasswordAuthenticationToken(
                user.getId().toString(), null, List.of());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}
