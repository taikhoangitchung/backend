package app.service.strategy;

import app.entity.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;


@Component("google")
public class GoogleUserStrategy implements OAuth2UserStrategy {

    @Override
    public User createUser(OidcUser oidcUser) {

        String email = oidcUser.getEmail();
        String username = oidcUser.getFullName() != null ? oidcUser.getFullName() : oidcUser.getEmail();
        String googleId = oidcUser.getSubject();
        String avatarUrl = oidcUser.getPicture() != null ? oidcUser.getPicture() : "https://www.gravatar.com/avatar/" + UUID.randomUUID().toString() + "?d=mp";
        System.out.println(avatarUrl);
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setGoogleId(googleId);
        user.setPassword(UUID.randomUUID().toString());
        user.setActive(true);
        user.setCreateAt(LocalDateTime.now());
        user.setAvatar(avatarUrl);
        return user;
    }
}
