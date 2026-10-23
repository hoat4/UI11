package ui11.provide;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// An alternative design would be that ExposeRequests supply IVs for widgets.
// However, this would lead to eager merging of IVs which is unnecessary work.

@Target(METHOD)
@Retention(RUNTIME)
public @interface FromPeerRequests {
}
