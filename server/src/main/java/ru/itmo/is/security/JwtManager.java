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
    private static final String TOKEN_TYPE_CLAIM_KEY = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final Logger log = LogManager.getLogger(JwtManager.class);

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtManager(@Value("${jwt.access.key}") String accessKey,
                      @Value("${jwt.refresh.key}") String refreshKey
    ) {
        this.accessKey = Keys.hmacShaKeyFor(accessKey.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(User user) {
        Date now = new Date();
        Date exp = Date.from(LocalDateTime.now().plusMinutes(10L).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .subject(user.getLogin())
                .claim(ROLE_CLAIM_KEY, user.getRole())
                .claim(TOKEN_TYPE_CLAIM_KEY, ACCESS_TOKEN_TYPE)
                .issuedAt(now)
                .expiration(exp)
                .signWith(accessKey)
                .compact();
    }

    public String createRefreshToken(User user) {
        Date now = new Date();
        Date exp = Date.from(LocalDateTime.now().plusDays(30L).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .subject(user.getLogin())
                .claim(TOKEN_TYPE_CLAIM_KEY, REFRESH_TOKEN_TYPE)
                .issuedAt(now)
                .expiration(exp)
                .signWith(refreshKey)
                .compact();
    }

    public Optional<String> getLoginFromAccessToken(String jwt) {
        return getAccessTokenClaims(jwt)
                .map(Claims::getSubject);
    }

    public Optional<User.Role> getRoleFromAccessToken(String jwt) {
        return getAccessTokenClaims(jwt)
                .map(claims -> User.Role.valueOf(claims.get(ROLE_CLAIM_KEY, String.class)));
    }

    public Optional<String> getLoginFromRefreshToken(String jwt) {
        return getRefreshTokenClaims(jwt)
                .map(Claims::getSubject);
    }

    private Optional<Claims> getAccessTokenClaims(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            String tokenType = claims.get(TOKEN_TYPE_CLAIM_KEY, String.class);
            if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
                log.error("Token is not an access token");
                return Optional.empty();
            }

            return Optional.of(claims);
        } catch (Exception e) {
            log.error("Cannot parse access jwt", e);
            return Optional.empty();
        }
    }

    private Optional<Claims> getRefreshTokenClaims(String jwt) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(refreshKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            String tokenType = claims.get(TOKEN_TYPE_CLAIM_KEY, String.class);
            if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
                log.error("Token is not a refresh token");
                return Optional.empty();
            }

            return Optional.of(claims);
        } catch (Exception e) {
            log.error("Cannot parse refresh jwt", e);
            return Optional.empty();
        }
    }
}
