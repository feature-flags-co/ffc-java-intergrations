package co.featureflags.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FeatureGate {
    String feature();

    String value() default "";

    String fallback() default "";

    RouteMapping[] others() default {};

}
