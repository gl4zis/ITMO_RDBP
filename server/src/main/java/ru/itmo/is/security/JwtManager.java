package ru.itmo.is.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.itmo.is.entity.user.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtManager {
    private static final String ROLE_CLAIM_KEY = "role";
    @Value("${jwt.access.key}")
    private String securityKey;
    private SecretKey accessKey;

    @PostConstruct
    public void init() {
        accessKey = Keys.hmacShaKeyFor(securityKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(User user) {
        Date now = new Date();
        Date exp = Date.from(LocalDateTime.now().plusDays(1L).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .subject(user.getLogin())
                .claim(ROLE_CLAIM_KEY, user.getRole())
                .issuedAt(now)
                .expiration(exp)
                .signWith(accessKey)
                .compact();
    }

    public String getLogin(String jwt) {
        Claims claims = getClaims(jwt);
        return claims == null ? null : claims.getSubject();
    }

    public User.Role getRole(String jwt) {
        Claims claims = getClaims(jwt);
        return claims == null ? null : User.Role.valueOf(claims.get(ROLE_CLAIM_KEY, String.class));
    }

    private Claims getClaims(String jwt) {
        try {
            return Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
