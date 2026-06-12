package ui11;

import org.jspecify.annotations.NonNull;

/**
 * equals/hashCode-ot implementálniuk kell subclassoknak (pl. Widget.attachStateHolder használja)
 */
@org.teavm.metaprogramming.CompileTime
interface WidgetAccessor<W extends Widget> {

    Class<W> clazz();

    boolean prepareListenerProxies(W modelWidget);

    InputFieldChangeDetectionResult areInputFieldsChanged(W oldModel, W newModel);

    void checkStateEmptyAndPrepareState(W newState, WidgetState<W> widgetState, W model);

    void transferState(W fromState, W toState);

    void retrieveInheritedValues(W w);

    /**
     * Listener proxy-khoz van használva, azért tudjuk hogy nem primitív.
     */
    Object readNonPrimitiveInputField(W w, int inputField);

    /**
     * ez listener proxykat tartalmazó mezőket is néz
     */
    boolean inputFieldsEquals(W a, W b);

    int inputFieldsHashCode(W w);

    Object[] inputFieldsToString(W w);

    Widget decorate(W w, @NonNull Widget content);

    WidgetAccessor<W> asDetachedMarker(boolean detached);

    enum InputFieldChangeDetectionResult {

        NEEDS_UPDATE, NOT_NEEDS_UPDATE, NEEDS_LISTENER_PROXY_BACKPROPAGATION
    }
}
