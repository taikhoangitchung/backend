package app.service;

import app.entity.User;
import app.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends OidcUserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final TokenService tokenService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);
        String email = oidcUser.getEmail();
        String username = oidcUser.getFullName() != null ? oidcUser.getFullName() : email;
        String googleId = oidcUser.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setUsername(username);
                    newUser.setPassword(UUID.randomUUID().toString()); // ✅ sinh mật khẩu giả
                    newUser.setGoogleId(googleId);
                    newUser.setActive(true);
                    newUser.setCreateAt(LocalDateTime.now());
                    newUser.setAvatar("/media/default-avatar.png");
                    return userRepository.save(newUser);
                });

        // Tạo JWT và Refresh Token
        String accessToken = jwtService.generateToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        // Trả về thông tin người dùng với token (có thể gửi qua header hoặc response)
        return new DefaultOidcUser(
                Collections.singletonList(() -> user.isAdmin() ? "ADMIN" : "USER"),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }
}