package ru.itmo.is.utils;

import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;
import ru.itmo.is.security.Anonymous;
import ru.itmo.is.security.RolesAllowed;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AnnotationUtilsTest {

    @Test
    void testGetEndpointAnnotation_WhenMethodHasAnnotation_ShouldReturnMethodAnnotation() {
        HandlerMethod handlerMethod = mock(HandlerMethod.class);
        Anonymous methodAnnotation = mock(Anonymous.class);

        when(handlerMethod.getMethodAnnotation(any())).thenReturn(methodAnnotation);

        Annotation result = AnnotationUtils.getEndpointAnnotation(handlerMethod, Anonymous.class);

        assertNotNull(result);
        assertEquals(methodAnnotation, result);
    }

    @Test
    void testGetEndpointAnnotation_WhenMethodHasNoAnnotation_ShouldReturnClassAnnotation() {
        HandlerMethod handlerMethod = mock(HandlerMethod.class);

        when(handlerMethod.getMethodAnnotation(any())).thenReturn(null);
        doReturn(TestBeanType.class).when(handlerMethod).getBeanType();

        Annotation result = AnnotationUtils.getEndpointAnnotation(handlerMethod, Anonymous.class);

        assertNotNull(result);
        assertInstanceOf(Anonymous.class, result);
    }

    @Test
    void testGetEndpointAnnotation_WhenNeitherHasAnnotation_ShouldReturnNull() {
        HandlerMethod handlerMethod = mock(HandlerMethod.class);

        when(handlerMethod.getMethodAnnotation(any())).thenReturn(null);
        doReturn(TestBeanType.class).when(handlerMethod).getBeanType();

        Annotation result = AnnotationUtils.getEndpointAnnotation(handlerMethod, RolesAllowed.class);

        assertNull(result);
    }

    @Anonymous
    static class TestBeanType { }
}

