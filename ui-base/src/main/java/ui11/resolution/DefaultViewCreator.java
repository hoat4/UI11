package ui11.resolution;

import ui11.SubstitutedWidget;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// TODO át kéne nevezni FallbackContent-té és @Content mellé rakni

/**
 * @deprecated use {@link SubstitutedWidget} instead
 */
@Target(METHOD)
@Retention(RUNTIME)
@Deprecated
public @interface DefaultViewCreator {
}
