package ui11.designtoken.model;

import org.jspecify.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Group or token.
 */
public abstract sealed class Node extends Element permits Group, Token {

    public Group parent;
    public String name;

    public String description;

    /**
     * null, ha örökli
     */
    public Deprecation deprecation;

    public final Map<String, Object> extensions = new HashMap<>();

    public String path() {
        if (parent == null)
            return name;

        return parent.path() + "." + name;
    }

    public static sealed abstract class Deprecation extends Element {

        public static final class NotDeprecated extends Deprecation {}

        public static final class Deprecated extends Deprecation {
            @Nullable public String reason;
        }
    }
}
