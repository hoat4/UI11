package ui11.i18n;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public interface LocalizedResources {

    @Target(METHOD)
    @Retention(RUNTIME)
    @interface Text {
        String value();
    }

    @Target(METHOD)
    @Retention(RUNTIME)
    @interface Name {
        String value();
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    @interface Prefix {
        String value();
    }

    @Target(METHOD)
    @Retention(RUNTIME)
    @interface NotResource {
    }

    /* TODO @interface Selector {} */
}
