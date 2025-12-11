package ui11;

import javax.annotation.Nonnull;
import java.util.Objects;

// TODO név
public final class KeyWrapper extends Widget /*ProxyWidget*/ {

    @Nonnull final Element container;
    @Nonnull final Object key;
    @Nonnull final Widget content;

    KeyWrapper(@Nonnull Element container, @Nonnull Object key, @Nonnull Widget content) {
        Objects.requireNonNull(container);
        Objects.requireNonNull(key);
        Objects.requireNonNull(content);
        this.container = container;
        this.key = key;
        this.content = content;
    }

    @Override
    public String toString() {
        // container toStringjét nem hívjuk meg, mert nagy eséllyel rekurzió lenne,
        // pl. WidgetState.toString kiírja a hozzá tartozó widgetet
        // TODO WidgetState már nincs. ez a komment még mindig érvényes?
        return "KeyWrapper[container=" + container.getClass().getSimpleName() + "@" +
                Integer.toHexString(container.hashCode()) +
                (container instanceof RSWStateHolder<?> re ? " for " +
                        (re.modelType().getSimpleName().isEmpty() ? re.modelType().getName() :
                                re.modelType().getSimpleName()) : "") +
                ", " +
                "key=" + key + ", content=" + content + "]";
    }

    @Override
    protected Widget build() {
        throw new RuntimeException("should not reach here (KW b)");
    }
}