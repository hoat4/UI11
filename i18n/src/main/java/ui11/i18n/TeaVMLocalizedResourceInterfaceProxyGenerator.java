package ui11.i18n;

import org.teavm.metaprogramming.*;
import org.teavm.metaprogramming.reflect.ReflectMethod;
import ui11.i18n.LocalizedResourceDefinitionReflector.LocalizationResourceDefinitionException;
import ui11.i18n.LocalizedResourceDefinitionReflector.MethodWrapper.ReflectionMethodWrapper;
import ui11.i18n.LocalizedResourceDefinitionReflector.MethodWrapper.TeaVMMetaprogrammingMethodWrapper;
import ui11.i18n.LocalizedRichText.Decorator;
import ui11.i18n.LocalizedRichText.ElementFunction;
import ui11.i18n.LocalizedRichText.LeafElementFunction;

import org.jspecify.annotations.NonNull;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.observable.ObserverHolder;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.ServiceLoader.Provider;

import static java.util.Arrays.stream;

@CompileTime
class TeaVMLocalizedResourceInterfaceProxyGenerator {

    private TeaVMLocalizedResourceInterfaceProxyGenerator() {
        throw new Error();
    }

    @Meta
    public static native <I extends LocalizedResources> I makeI18NProxyObject(Class<I> localizationInterface,
                                                                              Locale locale);

    @SuppressWarnings({"Convert2MethodRef", "unused"})
    private static <I> void makeI18NProxyObject(ReflectClass<I> localizationInterface, Value<Locale> locale) {
        if (isNotLocalizationInterface(localizationInterface)) {
            Metaprogramming.unsupportedCase();
            return;
        }

        LocalizationDataTeaVMSupport localizationStringGetterGenerator = localizationStringGetterGenerator();

        Value<I> proxy = Metaprogramming.proxy(localizationInterface, (proxy2, method, args) -> {
            // TODO toString, equals, hashCode?

            TeaVMMetaprogrammingMethodWrapper mw = new TeaVMMetaprogrammingMethodWrapper(method);
            String name = LocalizedResourceDefinitionReflector.getName(mw);
            String defaultLocaleText = LocalizedResourceDefinitionReflector.getDefaultValue(mw);
            int argCount = args.length;
            Value<Object[]> argArray = Metaprogramming.emit(() -> new Object[argCount]);
            for (int i = 0; i < argCount; i++) {
                int i0 = i;
                Value<Object> argValue = args[i];
                Metaprogramming.emit(() -> argArray.get()[i0] = argValue.get());
            }
            Value<String> formatString = localizationStringGetterGenerator.makeLocalizedStringGetter(name);
            if (method.getReturnType().getName().equals(LocalizedText.class.getName())) {
                Metaprogramming.exit(() -> {
                    return new LocalizedText.NonEditableLocalizedText(locale.get(), argArray.get(), formatString.get());
                });
            } else if (method.getReturnType().getName().equals(LocalizedRichText.class.getName())) {
                Value<RichTextPatternParser> parser = Metaprogramming.emit(() -> {
                    return new RichTextPatternParser(locale.get());
                });

                // ReflectMethodban nincsenek AnnotatedType-ok, muszáj megkeresni java.lang.Class-t
                Method m = toReflectionMethod(method);

                AnnotatedType rteType =
                        LocalizedResourceDefinitionReflector.findRichTextElementType(m.getAnnotatedReturnType(),
                                new ReflectionMethodWrapper(m));

                Value<Map<String, ElementFunction<?>>> elementFunctionsVar = toElementFunctions(method, m, parser, rteType, argArray);

                Metaprogramming.exit(() -> {
                    parser.get().setPattern(formatString.get());
                    AnnotatedTextToken rootToken = parser.get().evaluate(argArray.get());
                    return makeLocalizedRichText(rootToken, elementFunctionsVar.get());
                });
            } else {
                throw new LocalizationResourceDefinitionException(new TeaVMMetaprogrammingMethodWrapper(method),
                        "unknown return type for method in a localization interface: " +
                                method.getReturnType().getName());
            }
        });
        Metaprogramming.exit(() -> proxy.get());
    }

    @Meta
    public static native <I extends LocalizedResources> I makeI18NProxyObject(Class<I> localizationInterface,
                                                                              Locale locale,
                                                                              LocalizableTextEditingContext editingContext);

    @SuppressWarnings({"Convert2MethodRef", "unused"})
    private static <I> void makeI18NProxyObject(ReflectClass<I> localizationInterface, Value<Locale> locale,
                                                Value<LocalizableTextEditingContext> editingContext) {
        if (isNotLocalizationInterface(localizationInterface)) {
            Metaprogramming.unsupportedCase();
            return;
        }

        LocalizationDataTeaVMSupport localizationStringGetterGenerator = localizationStringGetterGenerator();

        Value<I> proxy = Metaprogramming.proxy(localizationInterface, (proxy2, method, args) -> {
            // TODO toString, equals, hashCode?

            TeaVMMetaprogrammingMethodWrapper mw = new TeaVMMetaprogrammingMethodWrapper(method);
            String name = LocalizedResourceDefinitionReflector.getName(mw);
            String defaultLocaleText = LocalizedResourceDefinitionReflector.getDefaultValue(mw);
            int argCount = args.length;
            Value<Object[]> argArray = Metaprogramming.emit(() -> new Object[argCount]);
            for (int i = 0; i < argCount; i++) {
                int i0 = i;
                Value<Object> argValue = args[i];
                Metaprogramming.emit(() -> argArray.get()[i0] = argValue.get());
            }
            Value<String> formatString = localizationStringGetterGenerator.makeLocalizedStringGetter(name);
            if (method.getReturnType().getName().equals(LocalizedText.class.getName())) {
                Metaprogramming.exit(() -> {
                    LocalizableTextEditingContext ctx = editingContext.get();
                    if (ctx == null)
                        return new LocalizedText.NonEditableLocalizedText(locale.get(), argArray.get(),
                                formatString.get());

                    MutableObservable<String> obs = ctx.stringFor(name, formatString.get());
                    return new LocalizedText.EditableLocalizedText(locale.get(), argArray.get(), name, obs);
                });
            } else if (method.getReturnType().getName().equals(LocalizedRichText.class.getName())) {
                Value<RichTextPatternParser> parser = Metaprogramming.emit(() -> {
                    return new RichTextPatternParser(locale.get());
                });

                // ReflectMethodban nincsenek AnnotatedType-ok, muszáj megkeresni java.lang.Class-t
                Method m = toReflectionMethod(method);

                AnnotatedType rteType =
                        LocalizedResourceDefinitionReflector.findRichTextElementType(m.getAnnotatedReturnType(),
                                new ReflectionMethodWrapper(m));

                Value<Map<String, ElementFunction<?>>> elementFunctionsVar = toElementFunctions(method, m, parser, rteType, argArray);

                Metaprogramming.exit(() -> {
                    LocalizableTextEditingContext ctx = editingContext.get();
                    if (ctx == null)
                        return new LocalizedText.NonEditableLocalizedText(locale.get(), argArray.get(),
                                formatString.get());

                    MutableObservable<String> obs = ctx.stringFor(name, formatString.get());

                    return makeMutableLocalizedRichText(obs, name, parser.get(), argArray.get(), elementFunctionsVar.get());
                });
            } else {
                throw new LocalizationResourceDefinitionException(new TeaVMMetaprogrammingMethodWrapper(method),
                        "unknown return type for method in a localization interface: " +
                                method.getReturnType().getName());
            }
        });
        Metaprogramming.exit(() -> proxy.get());
    }

    private static @NonNull Value<Map<String, ElementFunction<?>>> toElementFunctions(ReflectMethod method, Method m, Value<RichTextPatternParser> parser, AnnotatedType rteType, Value<Object[]> argArray) {
        Value<Map<String, ElementFunction<?>>> elementFunctionsVar =
                Metaprogramming.emit(() -> new HashMap<>());

        int paramIndex = 0;
        for (Parameter parameter : m.getParameters()) {
            int paramIndex0 = paramIndex;
            String paramName = parameter.getName();
            if (parameter.getType() == String.class)
                Metaprogramming.emit(() -> parser.get().addInlineStringVar());
            else if (parameter.getAnnotatedType().equals(rteType)) {
                Metaprogramming.emit(() -> {
                    parser.get().addWidgetVar(paramName);
                    Object value = argArray.get()[paramIndex0];
                    LeafElementFunction<?> leafElementFunction = () -> value;
                    elementFunctionsVar.get().put(paramName, leafElementFunction);
                });
            } else if (parameter.getType() == Decorator.class &&
                    ((AnnotatedParameterizedType) parameter.getAnnotatedType()).getAnnotatedActualTypeArguments()[0].equals(rteType))
                Metaprogramming.emit(() -> {
                    parser.get().addSpanDecoratorVar(paramName);
                    Decorator<?> value = (Decorator<?>) argArray.get()[paramIndex0];
                    Objects.requireNonNull(value, "RTE decorator");
                    elementFunctionsVar.get().put(paramName, value);
                });
            else
                throw new LocalizationResourceDefinitionException(new TeaVMMetaprogrammingMethodWrapper(method),
                        "unknown parameter type for parameter \"" + parameter.getName() + "\": " + parameter.getType().getName());
            paramIndex++;
        }
        return elementFunctionsVar;
    }

    private static <I> boolean isNotLocalizationInterface(ReflectClass<I> localizationInterface) {

        ReflectClass<LocalizedResources> baseInterface = Metaprogramming.findClass(LocalizedResources.class);
        return baseInterface.equals(localizationInterface) || !baseInterface.isAssignableFrom(localizationInterface) ||
                localizationInterface.getName().contains("$proxy$"); /* TODO teavm proxy, nem interface */
    }

    private static @NonNull Method toReflectionMethod(ReflectMethod method) {
        Method m;
        try {
            Class<?> c = Class.forName(method.getDeclaringClass().getName(), false,
                    TeaVMLocalizedResourceInterfaceProxyGenerator.class.getClassLoader());
            // Metaprogramming::findClass nem lehet method reference, mert Stream classloaderét használná
            // reportolni kéne teavm-nek
            List<Method> candidates = stream(c.getDeclaredMethods()).
                    filter(m2 -> m2.getName().equals(method.getName()) &&
                            method.getReturnType().equals(Metaprogramming.findClass(m2.getReturnType())) &&
                            Arrays.equals(method.getParameterTypes(),
                                    stream(m2.getParameterTypes()).map(c2 -> Metaprogramming.findClass(c2)).
                                            toArray(ReflectClass[]::new))).
                    toList();

            if (candidates.size() != 1)
                // nem lehetséges
                throw new RuntimeException("no single reflect method found for " + method.toString() + ": " +
                        candidates);

            m = candidates.getFirst();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e); // TODO
        }
        return m;
    }

    private static LocalizationDataTeaVMSupport localizationStringGetterGenerator() {
        // thread context class loader TeaVM-es valamire van átállítva
        ClassLoader classLoader = TeaVMLocalizedResourceInterfaceProxyGenerator.class.getClassLoader();
        List<Provider<LocalizationDataTeaVMSupport>> providers =
                ServiceLoader.load(LocalizationDataTeaVMSupport.class, classLoader).stream().toList();
        if (providers.isEmpty())
            throw new RuntimeException("no " + LocalizationDataTeaVMSupport.class.getName() + " available");
        else if (providers.size() > 1)
            throw new RuntimeException("multiple " + LocalizationDataTeaVMSupport.class.getName() + " implementations" +
                    " available: " + providers);
        return providers.getFirst().get();
    }

    @SuppressWarnings("unchecked")
    private static <E> @NonNull LocalizedRichText<E> makeLocalizedRichText(
            AnnotatedTextToken rootToken, Map<String, ? extends ElementFunction<?>> elementFunctions) {
        return new LocalizedRichText<>(rootToken, (Map<String, ElementFunction<E>>) elementFunctions);
    }

    @SuppressWarnings("unchecked")
    private static <E> @NonNull LocalizedRichText<E> makeMutableLocalizedRichText(
            Observable<String> formatStringObservable, String resid,
            RichTextPatternParser parser, Object[] argArray,
            Map<String, ? extends ElementFunction<?>> elementFunctions) {
        return new LocalizedRichText<>(formatStringObservable, resid, parser, argArray, (Map<String, ElementFunction<E>>) elementFunctions);
    }
}
