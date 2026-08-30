package ui11;

/**
 * Provides content for {@linkplain SubstitutedWidget SubstitutedWidgets}.
 * <p>
 * This interface is usually implemented by themes and platform-specific modules.
 */
// TODO multithreading specifikálása
public interface ResolverProvider {

    void configure(ResolverRegistry r);
}
