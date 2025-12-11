package ui11;

import ui11.resolution.DefaultPeer;
import ui11.resolution.GlobalViewProviders;
import ui11.resolution.WidgetResolver;
import ui11.resolution.WidgetResolver.ResolutionContext;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

/**
 * A stateless widget which does not determine its content by itself, instead a {@link WidgetResolver} is asked for
 * which content to display in it.
 */
public abstract class SubstitutedWidget extends Widget {

    // @State mezőket meg kéne tiltani ElementDefReflectorból, mert
    // buildFallbackContent csak hozzon létre egy fallback widgetet, ne csináljon bonyolult dolgokat

    // azért final, mert nincsenek @State mezők, ezért nincs értelme initStatenek
    @Override
    protected final void initState() {
    }

    @SuppressWarnings("ConstantValue")
    @Override
    protected final Widget build() {
        var resolutionContext = new ResolutionContext() {

            boolean done;

            @Override
            public <T> T inherited(Class<T> type) throws NoSuchElementException {
                if (done)
                    throw new IllegalStateException();
                return stateHolder().findInheritedValueForInjection(type, false, null);
            }

            @Override
            public <T> Optional<T> optionalInherited(Class<T> type) {
                if (done)
                    throw new IllegalStateException();
                return Optional.ofNullable(stateHolder().findInheritedValueForInjection(type, true, null));
            }
        };

        WidgetResolver widgetResolver = stateHolder().findInheritedValueForInjection(WidgetResolver.class, true, null);

        Widget resolved = null;
        try {
            if (widgetResolver != null)
                resolved = widgetResolver.resolveOrNull(this, resolutionContext);

            if (resolved == null)
                resolved = GlobalViewProviders.instance().resolveOrNull(this, resolutionContext);
        } finally {
            resolutionContext.done = true;
        }

        if (resolved == null) {
            resolved = fallbackContent();
            if (resolved == null)
                throw new RuntimeException("buildFallbackContent returned null on " + this);
        }

        return resolved;
    }

    // lehetne inkább ez nullable, és ha nullt ad vissza, akkor dobja az exceptiont build()
    @Nonnull
    protected Widget fallbackContent() {
        throw new RuntimeException("no " +
                "@" + DefaultPeer.class.getSimpleName() + " is used on " +
                getClass().getName() +
                " and no " + WidgetResolver.class.getSimpleName() + " supports it");
    }

    // azért csak SubstitutedWidgetnél van, mert itt kisebb eséllyel "szenzitív adat" az input mezők tartalma
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Object[] props = accessor().inputFieldsToString(this);
        sb.append(getClass().getSimpleName()).append(" (");
        if (stateHolderOrNull() == null)
            sb.append("no state holder");
        else
            sb.append(stateHolder().elementState);
        sb.append(") {");
        if (props.length == 0)
            sb.append("}");
        else {
            sb.append("\n");
            for (int i = 0; i < props.length; i += 2) {
                sb.append("  ").append(props[i]);
                Object val = props[i + 1];
                if (val == null)
                    // nem ugyanaz a karakter ilyenkor, mint a valós érték előtt, mert akkor nem lehetne
                    // megkülönböztetni nullt a "null" stringtől
                    sb.append(": null");
                else {
                    sb.append(" = ");
                    String valStr = switch (val) {
                        case Collection<?> coll -> coll.isEmpty() ? "[]" : coll.stream().
                                map(String::valueOf).collect(joining(", \n  ", "[\n  ", "\n]"));
                        case Map<?, ?> map -> map.isEmpty() ? "{}" : map.entrySet().stream().
                                map(String::valueOf).collect(joining(", \n  ", "{\n  ", "\n}"));
                        default -> val.toString();
                    };
                    int firstNewline = valStr.indexOf('\n');
                    if (firstNewline == -1)
                        sb.append(valStr);
                    else if (firstNewline > 0 && valStr.charAt(firstNewline-1) == '{' && valStr.endsWith("}"))
                        sb.append(valStr.replace("\n", "\n  "));
                    else // pl. Mat4
                        sb.append("\n    ").append(valStr.replace("\n", "\n    "));
                }
                sb.append(i == props.length - 2 ? "\n}" : ", \n");
            }
        }
        return sb.toString();
    }
}
