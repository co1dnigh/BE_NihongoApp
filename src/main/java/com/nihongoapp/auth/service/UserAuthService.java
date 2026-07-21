package com.nihongoapp.auth.service;

import com.nihongoapp.common.exception.BusinessException;
import com.nihongoapp.common.util.CryptoUtil;
import com.nihongoapp.user.model.User;
import com.nihongoapp.user.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserAuthService {

    private final CryptoUtil cryptoUtil;
    private final UserRepository userRepository;

    public UserAuthService(CryptoUtil cryptoUtil, UserRepository userRepository) {
        this.cryptoUtil = cryptoUtil;
        this.userRepository = userRepository;
    }

    public User authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("INVALID_REQUEST", "Sai thong tin dang nhap."));
        if (!cryptoUtil.matchesPassword(rawPassword, user.getPasswordHash())) {
            throw new BusinessException("INVALID_REQUEST", "Sai thong tin dang nhap.");
        }
        if (user.isBanned()) {
            throw new BusinessException("ACCOUNT_BANNED", "Tai khoan da bi khoa.");
        }
        return user;
    }

    public void logoutCurrentSession(Long userId) {
        SecurityContextHolder.clearContext();
    }
}
