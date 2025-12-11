package ui11;

import ui11.observable.Observable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * equals/hashCode-ot implementálniuk kell subclassoknak
 */
@org.teavm.metaprogramming.CompileTime
interface WidgetAccessor<T extends Widget> {

    /**
     * ennek csak {@linkplain Element} és {@linkplain Widget RSW} esetén kell működnie, sima {@linkplain Widget} esetén
     * nem
     */
    Widget decorate(T e, @Nonnull Widget content, boolean isDelegate);

    Class<T> clazz();

    void initAndCopyState(@Nullable T oldWidget, @Nonnull T newWidget);

    Observable<?>[] observeInheritedValues(RSWStateHolder<T> stateHolder);

    void checkStateEmpty(T w);

    /**
     * ez {@linkplain ui11.Widget.Listener}-rel annotált mezőket is néz
     */
    boolean inputFieldsEquals(T a, T b);

    /**
     * ez nem néz {@linkplain ui11.Widget.Listener}-rel annotált mezőket
     */
    boolean inputFieldsEqualsAndTransferListeners(T a, T b);

    int inputFieldsHashCode(T w);

    Object[] inputFieldsToString(T t);
}
