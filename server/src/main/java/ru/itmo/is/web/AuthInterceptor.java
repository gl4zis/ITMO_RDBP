package ru.itmo.is.web;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.security.Anonymous;
import ru.itmo.is.security.JwtManager;
import ru.itmo.is.security.SecurityContext;
import ru.itmo.is.utils.AnnotationUtils;

import java.util.Arrays;

@Log4j2
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private static final String AUTH_PREFIX = "Bearer";

    private final JwtManager jwtManager;
    private final SecurityContext securityContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info("Request handled: {}", request.getRequestURI());
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith(AUTH_PREFIX)) {
            try {
                String token = authHeader.split(" ")[1];
                String username = jwtManager.getLogin(token);
                User.Role role = jwtManager.getRole(token);

                securityContext.setContext(username, role);
            } catch (Exception e) {
                securityContext.setAnonymous();
            }
        } else {
            securityContext.setAnonymous();
        }
        if (allowed(handler, securityContext.getRole())) {
            return true;
        }

        if (securityContext.isAnonymous()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        return false;
    }

    private boolean allowed(Object handler, @Nullable User.Role authenticatedRole) {
        if (handler instanceof HandlerMethod handlerMethod) {
            Anonymous anonymous = AnnotationUtils.getEndpointAnnotation(handlerMethod, Anonymous.class);
            if (anonymous != null && authenticatedRole == null) {
                return true;
            }

            RolesAllowed rolesAllowed = AnnotationUtils.getEndpointAnnotation(handlerMethod, RolesAllowed.class);
            if (rolesAllowed == null) {
                return true;
            }
            return Arrays.asList(rolesAllowed.value()).contains(authenticatedRole);
        }
        return true;
    }

}
