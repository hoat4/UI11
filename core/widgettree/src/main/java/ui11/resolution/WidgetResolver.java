package ui11.resolution;

import ui11.Widget;
import ui11.provide.UpValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Tartalmat biztosít az általa ismert típusú elemekhez. Megvalósítja egyrészt minden renderer, másrészt a jövőben
 * megvalósíthatják majd "look and feel"/"theme"-szerű modulok.
 */
public interface WidgetResolver {

    /**
     * @return ha null, az azt jelenti hogy nem tudott vele mit kezdeni, nem azt hogy ürességre decomposeolta
     */
    @Nullable
    Widget resolveOrNull(Widget widget, ResolutionContext resolutionContext);

    // TODO mit csináljon a hívó kód, ha ez nullt ad vissza?
    @Nonnull
    default Widget resolveAdditional(@Nonnull Widget widget, @Nonnull Widget content) {
        return content;
    }

    static WidgetResolver composite(@Nonnull WidgetResolver defaults, @Nonnull WidgetResolver override) {
        Objects.requireNonNull(defaults, "WRc d");
        Objects.requireNonNull(override, "WRc o");

        return new WidgetResolver() {

            @Nullable
            @Override
            public Widget resolveOrNull(Widget widget, ResolutionContext resolutionContext) {
                Widget e2 = override.resolveOrNull(widget, resolutionContext);
                if (e2 != null)
                    return e2;
                else
                    return defaults.resolveOrNull(widget, resolutionContext);
            }

            @Nonnull
            @Override
            public Widget resolveAdditional(@Nonnull Widget widget, @Nonnull Widget content) {
                Objects.requireNonNull(widget);
                Objects.requireNonNull(content);

                content = defaults.resolveAdditional(widget, content);
                Objects.requireNonNull(content, "WRc rA d");
                content = override.resolveAdditional(widget, content);
                Objects.requireNonNull(content, "WRc rA o");
                return content;
            }
        };
    }

    interface ResolutionContext {

        <T> T inherited(Class<T> type) throws NoSuchElementException;

        <T> Optional<T> optionalInherited(Class<T> type);

        // TODO ez egyelőre kikommentezve, mert nem tudom, hogy key-ekkel mi legyen (r24198-ban került be).
        //      lehet hogy az egész ResolutionContext interfacet meg lehet szüntetni, mert ha @Content-et
        //      optimalizáljuk végre, akkor az használható lesz.
        // /**
        // *
        // * @throws NullPointerException ha a megadott widget {@code null}
        // */
        // WidgetInstantiation instantiate(Widget widget);
    }

    /*
    sealed interface DecompositionStep {}

    sealed abstract class WidgetProducingDecompositionStep implements DecompositionStep {

        public static <W extends Widget> WidgetProducingDecompositionStep of(Function<W, Widget> function) {

        }

        public static <W extends Widget> WidgetProducingDecompositionStep of(MethodHandle mh) {

        }

    }

    sealed abstract class ElementProducingDecompositionStep implements DecompositionStep {

        /**
         * @param elementFactory method type must be (Widget)Widget
         * /
        public static ElementProducingDecompositionStep of(MethodHandle elementFactory) {

        }

        public static ElementProducingDecompositionStep of(Class<? extends Node> elementType) {
            // TODO szebb hibaüzenet, ha mégsem Node leszármazott
            ConcreteElementAccessor concreteElementAccessor = (ConcreteElementAccessor) ElementAccessorFactory.accessorFor(elementType);
            return of(w -> concreteElementAccessor.createInstance());
        }

        public static <W extends Widget> ElementProducingDecompositionStep<W> of(Function<W, Node> elementFactory) {

        }


        static class MHBasedElementProducingDecompositionStep extends ElementProducingDecompositionStep {

        }
    }
    */
}

/*
interface TeaVMSupportingWidgetProducingDecompositionStep extends WidgetProducingDecompositionStep {

    Value<?> makeDecompositionCode(Class<?> type, Value<Widget> widgetRef);
}

 */