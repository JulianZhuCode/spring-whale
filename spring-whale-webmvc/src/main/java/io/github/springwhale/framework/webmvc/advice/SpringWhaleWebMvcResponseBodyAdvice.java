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
        addIgnore(ApiResult.class);
        addIgnore(ResponseEntity.class);
        addIgnore(HttpEntity.class);
        addIgnore(ModelAndView.class);
    }

    @Override
    public boolean supports(MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> returnClass = returnType.getParameterType();

        if (ignoreList.contains(returnClass)) {
            return false;
        }

        if (returnType.hasMethodAnnotation(AdviceIgnore.class)) {
            return false;
        }

        return !ResponseEntity.class.isAssignableFrom(returnClass) &&
                !HttpEntity.class.isAssignableFrom(returnClass);
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        Class<?> returnTypeClass = returnType.getParameterType();

        if (returnTypeClass.equals(Void.TYPE)) {
            return ApiResult.success();
        }

        return ApiResult.success(body);
    }

    public void addIgnore(Class<?> clz) {
        ignoreList.add(clz);
    }
}
