//package quiz.service;
//
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import quiz.entity.User;
//import quiz.repository.UserRepository;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class JwtService {
//    private final UserRepository userRepository;
//    public static final String ROLE_CLAIM = "roles";
//
//    @Value("${jwt.secret}")
//    private String secretKey;
//
//    @Value("${jwt.expiration}")
//    private long jwtExpirationMs;
//
//    public String generateToken(User user) {
//        List<String> roles = user.getAuthorities().stream().map(item -> item.getRole().name()).toList();
//        return Jwts.builder()
//                .setSubject(user.getUsername())
//                .claim(ROLE_CLAIM, roles)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
//                .signWith(signKey())
//                .compact();
//    }
//
//    public SecretKey signKey() {
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        return Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    public Claims extractToken(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(signKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    public String extractSubject(String token) {
//        return extractToken(token).getSubject();
//    }
//
//    public boolean isTokenValid(String token, String username) {
//        return (extractSubject(token).equals(username) && !isTokenExpired(token));
//    }
//
//    public boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    public Date extractExpiration(String token) {
//        return extractToken(token).getExpiration();
//    }
//}
//
