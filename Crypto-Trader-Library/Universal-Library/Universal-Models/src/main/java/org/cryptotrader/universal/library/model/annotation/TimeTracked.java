package org.cryptotrader.universal.library.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeTracked {
    boolean isLogged() default true;

    long expectedMillis() default -1L;

    boolean shouldPersist() default false;
}
