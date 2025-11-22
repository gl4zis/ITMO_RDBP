package ru.itmo.is.utils;

import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Annotation;

public class AnnotationUtils {
    private AnnotationUtils() {}

    public static <A extends Annotation> A getEndpointAnnotation(HandlerMethod method, Class<A> annotationClass) {
        A annotation = method.getMethodAnnotation(annotationClass);
        if (annotation == null) {
            annotation = method.getBeanType().getAnnotation(annotationClass);
        }
        return annotation;
    }
}
