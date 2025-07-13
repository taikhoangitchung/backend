package app.service;

import app.entity.User;
import app.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final Environment environment;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();
        User user = userRepository.findByEmail(email).orElseThrow();

        // xoa het refresh token cu
        tokenService.deleteAllRefreshTokensByUser(user);

        String token = jwtService.generateToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        // Redirect về frontend kèm theo token
        String redirectUrl = buildRedirectUrl(user, token, refreshToken);
        response.sendRedirect(redirectUrl);
    }

    private String buildRedirectUrl(User user, String accessToken, String refreshToken) {
        String rolePath = user.isAdmin() ? "admin/dashboard" : "users/dashboard";
        String clientUrl = Optional.ofNullable(environment.getProperty("CLIENT_URL"))
                .orElse("http://localhost:3000/");
        return clientUrl + rolePath + "?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
    }

}
