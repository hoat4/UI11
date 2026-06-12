package ui11.i18n;

import org.teavm.interop.PlatformMarker;
import ui11.provide.DynamicProvider;

import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

// TODO értelmesebb név
public class LocalizationInterfaceDP implements DynamicProvider {

    // mert provideOrNull akkor is meghívódik újra, ha fölösleges, ld. DynmicProvider::provideOrNullnál komment
    private final Map<Class<? extends LocalizedResources>, LocalizedResources> proxyCache = new HashMap<>();

    private final Function<Class<? extends LocalizedResources>, LocalizedResources> proxyObjectFactory;

    public LocalizationInterfaceDP(Locale locale) {
        if (isTeaVM()) {
            this.proxyObjectFactory = interfaceType ->
                    TeaVMLocalizedResourceInterfaceProxyGenerator.makeI18NProxyObject(
                            interfaceType, locale);
        } else {
            throw new RuntimeException("only usable on TeaVM. on JVM, use the other constructor instead.");
        }
    }

    public LocalizationInterfaceDP(Locale locale, LocalizableTextEditingContext editingContext) {
        if (isTeaVM()) {
            this.proxyObjectFactory = interfaceType ->
                    TeaVMLocalizedResourceInterfaceProxyGenerator.makeI18NProxyObject(
                            interfaceType, locale, editingContext);
        } else {
            throw new RuntimeException("only usable on TeaVM. on JVM, use the other constructor instead.");
        }
    }

    public LocalizationInterfaceDP(Locale locale, Function<String, String> translationsByResid,
                                   LocalizableTextEditingContext editingContext) {
        if (isTeaVM()) {
            throw new RuntimeException("not usable on TeaVM. on TeaVM, use the other constructor instead.");
        } else {
            this.proxyObjectFactory = interfaceType ->
                    RegularLocalizationInterfaceProxyGenerator.makeI18NProxyObject(
                            interfaceType, locale, translationsByResid);
        }
    }

    @PlatformMarker
    private static boolean isTeaVM() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T provideOrNull(Class<T> type) {
        if (type != LocalizedResources.class && LocalizedResources.class.isAssignableFrom(type)) {
            Class<? extends LocalizedResources> type2 = type.asSubclass(LocalizedResources.class);
            return (T) proxyCache.computeIfAbsent(type2, proxyObjectFactory);
        }
        return null;
    }
}
