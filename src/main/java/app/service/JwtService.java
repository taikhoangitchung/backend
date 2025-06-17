package app.service;

import app.entity.User;
import app.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    public String generateToken(User user) {
        String role = user.isAdmin() ? "ADMIN" : "USER";
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", role)
                .claim("id", user.getId())
                .claim("username", user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signKey())
                .compact();
    }

    public SecretKey signKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractSubject(String token) {
        return extractToken(token).getSubject();
    }

    public boolean isTokenValid(String token, String email) {
        return (extractSubject(token).equals(email) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractToken(token).getExpiration();
    }
}

