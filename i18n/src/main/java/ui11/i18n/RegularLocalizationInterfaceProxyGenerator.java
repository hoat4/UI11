package ui11.i18n;

import ui11.i18n.LocalizedResourceDefinitionReflector.LocalizationResourceDefinitionException;
import ui11.i18n.LocalizedResourceDefinitionReflector.MethodWrapper;
import ui11.i18n.LocalizedResourceDefinitionReflector.MethodWrapper.ReflectionMethodWrapper;
import ui11.i18n.LocalizedRichText.Decorator;
import ui11.i18n.LocalizedRichText.ElementFunction;
import ui11.i18n.LocalizedRichText.LeafElementFunction;

import org.jspecify.annotations.NonNull;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

class RegularLocalizationInterfaceProxyGenerator {

    private RegularLocalizationInterfaceProxyGenerator() {
        throw new RuntimeException("should not reach here");
    }

    @SuppressWarnings("unchecked")
    public static <I extends LocalizedResources> I makeI18NProxyObject(Class<I> localizationInterface,
                                                                       Locale locale,
                                                                       Function<String, String> translationsByResid) {
        ClassLoader cl = localizationInterface.getClassLoader()  /* TODO */;
        return (I) Proxy.newProxyInstance(cl, new Class[]{localizationInterface}, (proxy, method, args) -> {
            if (method.getName().equals("toString") && method.getReturnType().isAssignableFrom(String.class)
                    && method.getParameterCount() == 0) {
                return "Localization Interface Proxy " + localizationInterface.getName();
            }
            if (method.getName().equals("equals") && method.getReturnType() == boolean.class &&
                    method.getParameterCount() == 1)
                return args[0] == proxy;
            if (method.getName().equals("hashCode") && method.getReturnType() == int.class &&
                    method.getParameterCount() == 0)
                return System.identityHashCode(proxy);

            MethodWrapper mw = new ReflectionMethodWrapper(method);
            String name = LocalizedResourceDefinitionReflector.getName(mw);
            String defaultLocaleText = LocalizedResourceDefinitionReflector.getDefaultValue(mw);
            String formatString = translationsByResid.apply(name);
            if (formatString == null)
                formatString = defaultLocaleText;
            if (method.getReturnType().getName().equals(String.class.getName())) {
                // TODO ha nincs format stringben '{', akkor ki lehet hagyni MessageFormatot
                //      (ha csak args.length == 0, akkor nem feltétlen, lásd
                //       RichTextPatternParser.setPatternben kommentet)
                MessageFormat messageFormat =
                        new MessageFormat(formatString, locale);
                return messageFormat.format(args);
            } else if (method.getReturnType().getName().equals(LocalizedRichText.class.getName())) {
                RichTextPatternParser parser = new RichTextPatternParser(locale);

                AnnotatedType rteType = LocalizedResourceDefinitionReflector.findRichTextElementType(
                        method.getAnnotatedReturnType(), new ReflectionMethodWrapper(method));

                Map<String, ElementFunction<?>> elementFunctionsVar = new HashMap<>();

                int paramIndex = 0;
                for (Parameter parameter : method.getParameters()) {
                    int paramIndex0 = paramIndex;
                    String paramName = parameter.getName();
                    if (parameter.getType() == String.class)
                        parser.addInlineStringVar();
                    else if (parameter.getAnnotatedType().equals(rteType)) {
                        parser.addWidgetVar(paramName);
                        Object value = args[paramIndex0];
                        LeafElementFunction<?> leafElementFunction = () -> value;
                        elementFunctionsVar.put(paramName, leafElementFunction);
                    } else if (parameter.getType() == Decorator.class &&
                            ((AnnotatedParameterizedType) parameter.getAnnotatedType()).getAnnotatedActualTypeArguments()[0].equals(rteType)) {
                        parser.addSpanDecoratorVar(paramName);
                        Decorator<?> value = (Decorator<?>) args[paramIndex0];
                        Objects.requireNonNull(value, "RTE decorator");
                        elementFunctionsVar.put(paramName, value);
                    } else
                        throw new LocalizationResourceDefinitionException(mw,
                                "unknown parameter type for parameter \"" + parameter.getName() + "\": " + parameter.getType().getName());
                    paramIndex++;
                }

                parser.setPattern(formatString);
                AnnotatedTextToken rootToken = parser.evaluate(args);
                return makeLocalizedRichText(rootToken, elementFunctionsVar);
            } else {
                throw new LocalizationResourceDefinitionException(mw,
                        "unknown return type for method in a localization interface: " +
                                method.getReturnType().getName());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <E> @NonNull LocalizedRichText<E> makeLocalizedRichText(
            AnnotatedTextToken rootToken, Map<String, ? extends ElementFunction<?>> elementFunctions) {
        return new LocalizedRichText<>(rootToken, (Map<String, ElementFunction<E>>) elementFunctions);
    }
}
