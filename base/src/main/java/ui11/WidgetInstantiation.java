package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

record WidgetInstantiation(@NonNull WidgetState<?> widgetState,
                           @NonNull Map<@NonNull Class<?>, @Nullable Object> directIVs,
                           boolean shouldSetParent) {
    WidgetInstantiation {
        Objects.requireNonNull(widgetState);
        Objects.requireNonNull(directIVs);
    }
}
