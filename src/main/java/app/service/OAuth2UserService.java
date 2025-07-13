package app.service;

import app.entity.User;
import app.repository.UserRepository;
import app.service.factory.OAuth2UserStrategyFactory;
import app.service.strategy.OAuth2UserStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends OidcUserService {
    private final UserRepository userRepository;
    private final OAuth2UserStrategyFactory strategyFactory;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId(); // ví dụ: "google"

        OAuth2UserStrategy strategy = strategyFactory.getStrategy(provider);

        String email = oidcUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(strategy.createUser(oidcUser)));

        // cap nhat avatar
        if (!oidcUser.getPicture().equals(user.getAvatar())) {
            user.setAvatar(oidcUser.getPicture());
        }

        // Cập nhật thông tin đăng nhập lần cuối
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        if (!user.isActive()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("USER_BLOCKED", "Tài khoản đã bị khóa", null)
            );
        }

        return new DefaultOidcUser(
                Collections.singletonList(() -> user.isAdmin() ? "ADMIN" : "USER"),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }

}