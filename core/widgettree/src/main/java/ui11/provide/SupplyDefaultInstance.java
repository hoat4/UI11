package ui11.provide;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// TODO TeaVM-en ez nincs tesztelve
@Target(TYPE)
@Retention(RUNTIME)
public @interface SupplyDefaultInstance {

    // Class<? extends Tag> scope() default Tag.class;
}
