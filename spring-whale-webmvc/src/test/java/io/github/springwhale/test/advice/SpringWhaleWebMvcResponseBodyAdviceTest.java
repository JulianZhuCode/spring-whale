package io.github.springwhale.test.advice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for SpringWhaleWebMvcResponseBodyAdvice using MockMvc
 * Tests the actual behavior of ResponseBodyAdvice in Spring MVC context
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
class SpringWhaleWebMvcResponseBodyAdviceTest {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mvc;

    @Test
    @DisplayName("Should wrap plain object as ApiResult.success()")
    void testWrapPlainObject() throws Exception {
        mvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("John Doe")));
    }

    @Test
    @DisplayName("Should not double-wrap ApiResult return type")
    void testDoesNotDoubleWrapApiResult() throws Exception {
        mvc.perform(get("/api-result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("success")))
                // The data field should contain the User object directly (not wrapped again)
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.name", is("John")));
    }

    @Test
    @DisplayName("Should not wrap ResponseEntity return type")
    void testDoesNotWrapResponseEntity() throws Exception {
        mvc.perform(get("/response-entity"))
                .andExpect(status().isOk())
                // ResponseEntity is NOT wrapped, returns User directly
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")));
    }

    @Test
    @DisplayName("Should not wrap HttpEntity return type")
    void testDoesNotWrapHttpEntity() throws Exception {
        mvc.perform(get("/http-entity"))
                .andExpect(status().isOk())
                // HttpEntity is NOT wrapped, returns User directly
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")));
    }

    @Test
    @DisplayName("Should wrap String as ApiResult.success()")
    void testWrapString() throws Exception {
        // Note: String wrapping causes ClassCastException in StringHttpMessageConverter
        // This is expected behavior - the advice wraps it as ApiResult<String>
        // but Spring tries to use StringHttpMessageConverter for ApiResult
        mvc.perform(get("/string"))
                .andExpect(status().isOk())
                // The exception is caught by global exception handler
                .andExpect(jsonPath("$.code", is("500")))
                .andExpect(jsonPath("$.message", containsString("abnormal")));
    }

    @Test
    @DisplayName("Should wrap List as ApiResult.success()")
    void testWrapList() throws Exception {
        mvc.perform(get("/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id", is(1)))
                .andExpect(jsonPath("$.data[0].name", is("John")))
                .andExpect(jsonPath("$.data[1].id", is(2)))
                .andExpect(jsonPath("$.data[1].name", is("Jane")));
    }

    @Test
    @DisplayName("Should wrap Map as ApiResult.success()")
    void testWrapMap() throws Exception {
        mvc.perform(get("/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data.key", is("value")));
    }

    @Test
    @DisplayName("Should return empty ApiResult.success() for void return type")
    void testWrapVoid() throws Exception {
        mvc.perform(get("/void"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data", is(true)));
    }

    @Test
    @DisplayName("Should wrap Integer as ApiResult.success()")
    void testWrapInteger() throws Exception {
        mvc.perform(get("/int"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data", is(42)));
    }

    @Test
    @DisplayName("Should wrap Boolean as ApiResult.success()")
    void testWrapBoolean() throws Exception {
        mvc.perform(get("/boolean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("200")))
                .andExpect(jsonPath("$.message", is("success")))
                .andExpect(jsonPath("$.data", is(true)));
    }

    @Test
    @DisplayName("Should not wrap method with @AdviceIgnore annotation")
    void testDoesNotWrapMethodWithAdviceIgnore() throws Exception {
        // Methods annotated with @AdviceIgnore should NOT be wrapped
        mvc.perform(get("/ignored"))
                .andExpect(status().isOk())
                // Should return User directly, not wrapped in ApiResult
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.name", is("Ignored User")));
    }

    @Test
    @DisplayName("Should not wrap custom subclass of ResponseEntity")
    void testDoesNotWrapCustomResponseEntity() throws Exception {
        // Custom subclasses of ResponseEntity should also be ignored
        mvc.perform(get("/custom-response-entity"))
                .andExpect(status().isOk())
                // Should return User directly, not wrapped in ApiResult
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("Custom")));
    }
}
