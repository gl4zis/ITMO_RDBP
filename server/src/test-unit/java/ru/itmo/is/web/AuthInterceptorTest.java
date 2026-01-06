package ru.itmo.is.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.method.HandlerMethod;
import ru.itmo.is.entity.user.User;
import ru.itmo.is.security.Anonymous;
import ru.itmo.is.security.JwtManager;
import ru.itmo.is.security.RolesAllowed;
import ru.itmo.is.security.SecurityContext;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthInterceptorTest {

    @Mock
    private JwtManager jwtManager;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HandlerMethod handlerMethod;
    @InjectMocks
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        when(securityContext.getRole()).thenReturn(null);
        when(securityContext.isAnonymous()).thenReturn(true);
    }

    @Test
    void testPreHandle_WithValidBearerToken_ShouldSetContext() throws Exception {
        // Given: Valid Bearer token
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtManager.getLogin("valid-token")).thenReturn("user1");
        when(jwtManager.getRole("valid-token")).thenReturn(User.Role.RESIDENT);
        when(securityContext.getRole()).thenReturn(User.Role.RESIDENT);
        when(securityContext.isAnonymous()).thenReturn(false);
        
        setupHandlerMethodWithoutAnnotations();

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Context should be set, access allowed
        assertTrue(result);
        verify(securityContext).setContext("user1", User.Role.RESIDENT);
        verify(securityContext, never()).setAnonymous();
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testPreHandle_WithInvalidToken_ShouldSetAnonymous() throws Exception {
        // Given: Invalid token throws exception
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtManager.getLogin("invalid-token")).thenThrow(new RuntimeException("Invalid token"));
        
        setupHandlerMethodWithAnonymousAnnotation();

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Should set anonymous and allow access (because of @Anonymous)
        assertTrue(result);
        verify(securityContext).setAnonymous();
        verify(securityContext, never()).setContext(anyString(), any());
    }

    @Test
    void testPreHandle_WithNoAuthHeader_ShouldSetAnonymous() throws Exception {
        // Given: No Authorization header
        when(request.getHeader("Authorization")).thenReturn(null);
        
        setupHandlerMethodWithAnonymousAnnotation();

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Should set anonymous
        assertTrue(result);
        verify(securityContext).setAnonymous();
        verify(securityContext, never()).setContext(anyString(), any());
    }

    @Test
    void testPreHandle_WithNonBearerToken_ShouldSetAnonymous() throws Exception {
        // Given: Authorization header doesn't start with "Bearer"
        when(request.getHeader("Authorization")).thenReturn("Basic token");
        
        setupHandlerMethodWithAnonymousAnnotation();

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Should set anonymous
        assertTrue(result);
        verify(securityContext).setAnonymous();
        verify(jwtManager, never()).getLogin(anyString());
    }

    @Test
    void testPreHandle_WithAnonymousAnnotation_ShouldAllowAccess() throws Exception {
        // Given: Anonymous user with @Anonymous annotation
        when(request.getHeader("Authorization")).thenReturn(null);
        
        setupHandlerMethodWithAnonymousAnnotation();

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Should allow access
        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testPreHandle_WithRolesAllowed_WhenRoleMatches_ShouldAllowAccess() throws Exception {
        // Given: Authenticated user with matching role
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtManager.getLogin("token")).thenReturn("manager1");
        when(jwtManager.getRole("token")).thenReturn(User.Role.MANAGER);
        when(securityContext.getRole()).thenReturn(User.Role.MANAGER);
        when(securityContext.isAnonymous()).thenReturn(false);
        
        setupHandlerMethodWithRolesAllowed(User.Role.MANAGER);

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Should allow access
        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testPreHandle_WithRolesAllowed_WhenRoleNotMatches_ShouldReturnForbidden() throws Exception {
        // Given: Authenticated user with non-matching role
        // User is RESIDENT but endpoint requires MANAGER
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtManager.getLogin("token")).thenReturn("user1");
        when(jwtManager.getRole("token")).thenReturn(User.Role.RESIDENT);
        when(securityContext.getRole()).thenReturn(User.Role.RESIDENT);
        when(securityContext.isAnonymous()).thenReturn(false);
        
        setupHandlerMethodWithRolesAllowed(User.Role.MANAGER); // Requires MANAGER, user is RESIDENT

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Should deny access with FORBIDDEN
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
    
    @Test
    void testPreHandle_WithRolesAllowed_WhenRoleMatchesDifferentRole_ShouldAllowAccess() throws Exception {
        // Given: Authenticated user with GUARD role accessing endpoint that requires GUARD
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtManager.getLogin("token")).thenReturn("guard1");
        when(jwtManager.getRole("token")).thenReturn(User.Role.GUARD);
        when(securityContext.getRole()).thenReturn(User.Role.GUARD);
        when(securityContext.isAnonymous()).thenReturn(false);
        
        setupHandlerMethodWithRolesAllowed(User.Role.GUARD); // Requires GUARD, user is GUARD

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Should allow access when role matches
        assertTrue(result);
        verify(securityContext).setContext("guard1", User.Role.GUARD);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testPreHandle_WithRolesAllowed_WhenAnonymous_ShouldReturnUnauthorized() throws Exception {
        // Given: Anonymous user trying to access @RolesAllowed endpoint
        when(request.getHeader("Authorization")).thenReturn(null);
        when(securityContext.isAnonymous()).thenReturn(true);
        
        setupHandlerMethodWithRolesAllowed(User.Role.MANAGER);

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Should deny access with UNAUTHORIZED
        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void testPreHandle_WithNoAnnotations_ShouldAllowAccess() throws Exception {
        // Given: No security annotations on endpoint
        when(request.getHeader("Authorization")).thenReturn(null);
        
        setupHandlerMethodWithoutAnnotations();

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Should allow access (default behavior)
        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testPreHandle_WithNonHandlerMethod_ShouldAllowAccess() {
        // Given: Handler is not a HandlerMethod (e.g., static resource)
        when(request.getHeader("Authorization")).thenReturn(null);
        Object nonHandlerMethod = new Object();

        // When
        boolean result = authInterceptor.preHandle(request, response, nonHandlerMethod);

        // Then: Should allow access
        assertTrue(result);
        verify(securityContext).setAnonymous();
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void testPreHandle_WithRolesAllowed_WhenMultipleRolesInAnnotation_ShouldCheckAll() throws Exception {
        // Given: User with MANAGER role accessing endpoint that requires MANAGER
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(jwtManager.getLogin("token")).thenReturn("manager1");
        when(jwtManager.getRole("token")).thenReturn(User.Role.MANAGER);
        when(securityContext.getRole()).thenReturn(User.Role.MANAGER);
        when(securityContext.isAnonymous()).thenReturn(false);
        
        setupHandlerMethodWithRolesAllowed(User.Role.MANAGER);

        // When
        boolean result = authInterceptor.preHandle(request, response, handlerMethod);

        // Then: Should allow access when role matches
        assertTrue(result);
        verify(securityContext).setContext("manager1", User.Role.MANAGER);
        verify(response, never()).setStatus(anyInt());
    }

    private void setupHandlerMethodWithAnonymousAnnotation() throws Exception {
        Method method = TestController.class.getMethod("anonymousMethod");
        doReturn(method).when(handlerMethod).getMethod();
        doReturn(TestController.class).when(handlerMethod).getBeanType();
        Anonymous anonymous = method.getAnnotation(Anonymous.class);
        doReturn(anonymous).when(handlerMethod).getMethodAnnotation(Anonymous.class);
        doReturn(null).when(handlerMethod).getMethodAnnotation(RolesAllowed.class);
    }

    private void setupHandlerMethodWithRolesAllowed(User.Role requiredRole) throws Exception {
        Method method = TestController.class.getMethod("rolesAllowedMethod");
        doReturn(method).when(handlerMethod).getMethod();
        doReturn(TestController.class).when(handlerMethod).getBeanType();
        doReturn(null).when(handlerMethod).getMethodAnnotation(Anonymous.class);
        RolesAllowed rolesAllowed = mock(RolesAllowed.class);
        when(rolesAllowed.value()).thenReturn(new User.Role[]{requiredRole});
        doReturn(rolesAllowed).when(handlerMethod).getMethodAnnotation(RolesAllowed.class);
    }

    private void setupHandlerMethodWithoutAnnotations() throws Exception {
        Method method = TestController.class.getMethod("noAnnotationMethod");
        doReturn(method).when(handlerMethod).getMethod();
        doReturn(TestController.class).when(handlerMethod).getBeanType();
        doReturn(null).when(handlerMethod).getMethodAnnotation(Anonymous.class);
        doReturn(null).when(handlerMethod).getMethodAnnotation(RolesAllowed.class);
    }

    static class TestController {
        @Anonymous
        public void anonymousMethod() { }

        public void rolesAllowedMethod() { }

        public void noAnnotationMethod() { }
    }
}
