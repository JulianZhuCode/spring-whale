package io.github.springwhale.database.datascope;


import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    DataScopeType scopeType() default DataScopeType.AUTO;

    String module() default "";
}