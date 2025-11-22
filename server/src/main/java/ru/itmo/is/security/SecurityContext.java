package ru.itmo.is.security;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import ru.itmo.is.entity.user.User;

import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

@Component
@Scope(scopeName = SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
@Data
public class SecurityContext {
    private String username;
    private User.Role role;

    public void setContext(@NotNull String username, @NotNull User.Role role) {
        if (username == null || role == null) {
            throw new IllegalArgumentException("Null credentials in SecurityContext");
        }
        this.username = username;
        this.role = role;
    }

    public void setAnonymous() {
        this.username = null;
        this.role = null;
    }

    public boolean isAnonymous() {
        return this.username == null;
    }
}