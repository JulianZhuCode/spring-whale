package io.github.springwhale.framework.webmvc.advice;


import io.github.springwhale.framework.core.model.ApiResult;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashSet;
import java.util.Set;

@RestControllerAdvice
public class SpringWhaleWebMvcResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    private final Set<Class<?>> ignoreList = new HashSet<>();

    public SpringWhaleWebMvcResponseBodyAdvice() {
        // Add types that should be ignored
        addIgnore(ApiResult.class);
        addIgnore(ResponseEntity.class);
        addIgnore(HttpEntity.class);
        addIgnore(ModelAndView.class);
    }

    @Override
    public boolean supports(MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        // Get the return type
        Class<?> returnClass = returnType.getParameterType();

        // Check if it's in the ignore list
        if (ignoreList.contains(returnClass)) {
            return false;
        }

        // Check if it has @AdviceIgnore annotation
        if (returnType.hasMethodAnnotation(AdviceIgnore.class)) {
            return false;
        }

        // Check if it's a subclass of ResponseEntity or HttpEntity
        if (ResponseEntity.class.isAssignableFrom(returnClass) ||
                HttpEntity.class.isAssignableFrom(returnClass)) {
            return false;
        }

        return true;
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // Get the actual method return type
        Class<?> returnTypeClass = returnType.getParameterType();

        // Return empty success response for void type
        if (returnTypeClass.equals(Void.TYPE)) {
            return ApiResult.success();
        }

        // Wrap other types as success response
        return ApiResult.success(body);
    }

    public void addIgnore(Class<?> clz) {
        ignoreList.add(clz);
    }
}
