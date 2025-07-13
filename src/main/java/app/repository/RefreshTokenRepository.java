package app.repository;

import app.entity.RefreshToken;
import app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    // Xóa tất cả token của một người dùng
    void deleteByUser(User user);
}