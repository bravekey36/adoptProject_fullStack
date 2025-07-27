package petcare.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {
    // JwtFilter와의 호환성을 위한 alias 메서드 추가
    public boolean validateToken(String token) {
        return validate(token);
    }

    public String getUsernameFromToken(String token) {
        return getUserId(token);
    }
    private final Key key;
    private final long validityMs;

    public JwtProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.validityMs = tokenValidityInSeconds * 1000;
    }

    public String create(String userId, String role) {
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("role", role);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityMs);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JWT 검증 실패 원인 로그 출력
            System.err.println("[JWT ERROR] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return false;
        }
    }

    public String getUserId(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String getRole(String token) {
        return (String) Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().get("role");
    }
}
