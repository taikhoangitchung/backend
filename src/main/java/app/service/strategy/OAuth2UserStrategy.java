package app.service.strategy;

import app.entity.User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public interface OAuth2UserStrategy {
    User createUser(OidcUser oidcUser);
}
