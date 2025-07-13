package app.service;

import app.entity.RefreshToken;
import app.entity.User;
import app.exception.InvalidTokenException;
import app.exception.TokenExpiredException;
import app.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${JWT_REFRESH_TOKEN_EXPIRATION}")
    private long refreshTokenExpirationMs;

    public String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpirationMs / 1000));
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public String refreshAccessToken(String refreshToken) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token không hợp lệ"));

        if (tokenEntity.isExpired()) {
            refreshTokenRepository.delete(tokenEntity); // Optional: chỉ xóa khi expired
            throw new TokenExpiredException("Refresh token đã hết hạn");
        }

        User user = tokenEntity.getUser();
        return jwtService.generateToken(user);
    }

    @Transactional
    public void deleteAllRefreshTokensByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

}