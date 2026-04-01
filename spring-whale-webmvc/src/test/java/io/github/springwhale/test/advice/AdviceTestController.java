package io.github.springwhale.test.advice;

import io.github.springwhale.framework.core.model.ApiResult;
import io.github.springwhale.framework.webmvc.advice.AdviceIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@RestController
public class AdviceTestController {

    @GetMapping("/user")
    public User returnUser() {
        return new User(1L, "John Doe");
    }

    @GetMapping("/api-result")
    public ApiResult<User> returnApiResult() {
        return ApiResult.success(new User(1L, "John"));
    }

    @GetMapping("/response-entity")
    public ResponseEntity<User> returnResponseEntity() {
        return ResponseEntity.ok(new User(1L, "John"));
    }

    @GetMapping("/http-entity")
    public HttpEntity<User> returnHttpEntity() {
        return new HttpEntity<>(new User(1L, "John"));
    }

    @GetMapping("/model-view")
    public ModelAndView returnModelAndView() {
        return new ModelAndView("view");
    }

    @GetMapping("/string")
    public String returnString() {
        return "Hello World";
    }

    @GetMapping("/list")
    public List<User> returnList() {
        return List.of(new User(1L, "John"), new User(2L, "Jane"));
    }

    @GetMapping("/map")
    public Map<String, Object> returnMap() {
        return Map.of("key", "value");
    }

    @GetMapping("/void")
    public void returnVoid() {
    }

    @GetMapping("/int")
    public int returnInt() {
        return 42;
    }

    @GetMapping("/boolean")
    public boolean returnBoolean() {
        return true;
    }

    @GetMapping("/ignored")
    @AdviceIgnore
    public User returnIgnored() {
        return new User(99L, "Ignored User");
    }

    @GetMapping("/custom-response-entity")
    public CustomResponseEntity<User> returnCustomResponseEntity() {
        return new CustomResponseEntity<>(new User(100L, "Custom"));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class User {
        private Long id;
        private String name;
    }

    /**
     * Custom subclass of ResponseEntity for testing inheritance check
     */
    public static class CustomResponseEntity<T> extends ResponseEntity<T> {
        public CustomResponseEntity(T body) {
            super(body, org.springframework.http.HttpStatus.OK);
        }
    }
}
