package app.service;

import app.entity.User;
import app.exception.AuthException;
import app.repository.UserRepository;
import app.util.MessageHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
        String email = oidcUser.getEmail();
        User user = userRepository.findByEmail(email).orElseThrow();

        String token = jwtService.generateToken(user);
        String refreshToken = tokenService.createRefreshToken(user);

        // Redirect về frontend kèm theo token
        String role = user.isAdmin() ? "ADMIN" : "USER";
        String redirectUrl = "http://localhost:3000/" + (role.equals("ADMIN") ? "admin/dashboard" : "users/dashboard")
                + "?accessToken=" + token + "&refreshToken=" + refreshToken;
        response.sendRedirect(redirectUrl);
    }
}
