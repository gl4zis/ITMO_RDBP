package ru.itmo.is.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.itmo.is.entity.user.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtManager {
    private static final String ROLE_CLAIM_KEY = "role";
    private static final Logger log = LogManager.getLogger(JwtManager.class);

    private final SecretKey accessKey;

    public JwtManager(@Value("${jwt.access.key}") String securityKey) {
        this.accessKey = Keys.hmacShaKeyFor(securityKey.getBytes(StandardCharsets.UTF_8));
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

    public Optional<String> getLogin(String jwt) {
        return getClaims(jwt)
                .map(Claims::getSubject);
    }

    public Optional<User.Role> getRole(String jwt) {
        return getClaims(jwt)
                .map(claims -> User.Role.valueOf(claims.get(ROLE_CLAIM_KEY, String.class)));
    }

    private Optional<Claims> getClaims(String jwt) {
        try {
            return Optional.of(
                    Jwts.parser()
                            .verifyWith(accessKey)
                            .build()
                            .parseSignedClaims(jwt)
                            .getPayload()
            );
        } catch (Exception e) {
            log.error("Cannot parse jwt", e);
            return Optional.empty();
        }
    }
}
