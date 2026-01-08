package ru.itmo.is.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.itmo.is.entity.user.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtTestHelper {
    private static final String ROLE_CLAIM_KEY = "role";
    private static final String TOKEN_TYPE_CLAIM_KEY = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";

    private final SecretKey accessKey;

    public JwtTestHelper(@Value("${jwt.access.key}") String securityKey) {
        this.accessKey = Keys.hmacShaKeyFor(securityKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String login, User.Role role) {
        Date now = new Date();
        Date exp = Date.from(LocalDateTime.now().plusDays(1L).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .subject(login)
                .claim(ROLE_CLAIM_KEY, role.name())
                .claim(TOKEN_TYPE_CLAIM_KEY, ACCESS_TOKEN_TYPE)
                .issuedAt(now)
                .expiration(exp)
                .signWith(accessKey)
                .compact();
    }

    public String generateToken(User user) {
        return generateToken(user.getLogin(), user.getRole());
    }

    public String generateAuthHeader(String login, User.Role role) {
        return "Bearer " + generateToken(login, role);
    }

    public String generateAuthHeader(User user) {
        return "Bearer " + generateToken(user);
    }
}

