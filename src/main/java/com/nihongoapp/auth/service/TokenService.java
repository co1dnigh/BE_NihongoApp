package com.nihongoapp.auth.service;

import com.nihongoapp.auth.model.RefreshToken;
import com.nihongoapp.auth.repository.RefreshTokenRepository;
import com.nihongoapp.common.util.CryptoUtil;
import com.nihongoapp.user.model.User;
import com.nihongoapp.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenService {

  private final CryptoUtil cryptoUtil;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  public TokenService(CryptoUtil cryptoUtil, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
    this.cryptoUtil = cryptoUtil;
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
  }

  public record TokenPair(String accessToken, String refreshToken) {}

  @Transactional
  public TokenPair generateTokens(User user) {
    String accessToken = cryptoUtil.createAccessToken(user.getId(), user.getUsername(), user.getRole());
    String refreshToken = cryptoUtil.createRefreshToken(user.getId());

    RefreshToken entity = new RefreshToken();
    entity.setUserId(user.getId());
    entity.setTokenHash(refreshToken);
    entity.setExpiresAt(LocalDateTime.now().plusDays(7));
    entity.setRevoked(false);
    refreshTokenRepository.save(entity);

    return new TokenPair(accessToken, refreshToken);
  }

  @Transactional
  public Optional<TokenPair> rotateRefreshToken(String rawRefreshToken) {
    RefreshToken stored = refreshTokenRepository.findByTokenHash(rawRefreshToken).orElse(null);
    if (stored == null || stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
      return Optional.empty();
    }
    stored.setRevoked(true);
    refreshTokenRepository.save(stored);

    User user = userRepository.findById(stored.getUserId()).orElse(null);
    if (user == null) return Optional.empty();
    return Optional.of(generateTokens(user));
  }

  @Transactional
  public void revokeRefreshToken(String rawRefreshToken) {
    refreshTokenRepository.findByTokenHash(rawRefreshToken).ifPresent(token -> {
      token.setRevoked(true);
      refreshTokenRepository.save(token);
    });
  }

  @Transactional
  public void revokeAllByUser(Long userId) {
    refreshTokenRepository.findByUserIdAndRevokedFalse(userId).ifPresent(token -> {
      token.setRevoked(true);
      refreshTokenRepository.save(token);
    });
  }
}
