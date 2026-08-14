package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Tartalmat biztosít az általa ismert típusú elemekhez. Megvalósítja egyrészt minden renderer, másrészt a jövőben
 * megvalósíthatják majd "look and feel"/"theme"-szerű modulok.
 */
public abstract class WidgetResolver {

    protected abstract @Nullable Widget tryResolveGeneric(@NonNull SubstitutedWidget widget);

    protected abstract @Nullable Widget tryResolveRequestSpecific(@NonNull SubstitutedWidget widget,
                                                                  @NonNull PeerRequest<?> request);
}
