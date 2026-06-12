package ui11.i18n;

import org.teavm.metaprogramming.CompileTime;
import org.teavm.metaprogramming.Metaprogramming;
import org.teavm.metaprogramming.ReflectClass;
import org.teavm.metaprogramming.reflect.ReflectMethod;
import ui11.i18n.LocalizedResources.Name;
import ui11.i18n.LocalizedResources.NotResource;
import ui11.i18n.LocalizedResources.Prefix;
import ui11.i18n.LocalizedResources.Text;
import ui11.i18n.LocalizedResourceDefinitionReflector.ClassWrapper.ReflectionClassWrapper;
import ui11.i18n.LocalizedResourceDefinitionReflector.ClassWrapper.TeaVMMetaprogrammingClassWrapper;
import ui11.i18n.LocalizedResourceDefinitionReflector.MethodWrapper.ReflectionMethodWrapper;
import ui11.reflectutil.ReflectionUtil;
import ui11.reflectutil.Types;
import ui11.reflectutil.Types.AnnotatedTypeVisitor;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@org.teavm.metaprogramming.CompileTime
public class LocalizedResourceDefinitionReflector {

    private LocalizedResourceDefinitionReflector() {
        throw new Error();
    }

    public static boolean isLocalizationInterface(ClassWrapper c) {
        return c.isInterface() && c.isAssignableTo(LocalizedResources.class) && !c.is(LocalizedResources.class);
    }

    public static Collection<LocalizationInterfaceLocalizableTextDefinition>
    listDefinitionsIfIsLocalizationResourceDefinitionInterface(Class<?> c) {

        if (!LocalizedResources.class.isAssignableFrom(c) || c == LocalizedResources.class)
            return null;

        if (!c.isInterface()) {
            throw new RuntimeException("extends " + LocalizedResources.class.getName() + " but not an " +
                    "interface: " + c.getName());
        }

        List<LocalizationInterfaceLocalizableTextDefinition> defs = new ArrayList<>();
        for (Method m : c.getMethods()) {
            if (Modifier.isStatic(m.getModifiers()) || Modifier.isPrivate(m.getModifiers()))
                continue;
            if (m.isAnnotationPresent(NotResource.class))
                continue;
            if (m.getReturnType() == void.class)
                throw new LocalizationResourceDefinitionException(
                        new ReflectionMethodWrapper(m), "void returning method");

            List<String> paramNames = stream(m.getParameters()).map(Parameter::getName).toList();

            ReflectionMethodWrapper mw = new ReflectionMethodWrapper(m);
            // LocationInSource azért nem m, mert akkor folyton új kommentet kezdene LocalizedTextExporter
            defs.add(new LocalizationInterfaceLocalizableTextDefinition(
                    getName(mw),
                    getDefaultValue(mw),
                    paramNames,
                    LocationInSource.of(c),
                    m,
                    getNameTokens(mw)
            ));
        }
        return defs;
    }

    public static class LocalizationInterfaceLocalizableTextDefinition extends LocalizableTextDefinition {

        public final Method method;
        public final List<String> propNameTokens;
        private List<String> classNameTokens;

        public LocalizationInterfaceLocalizableTextDefinition(String name, String defaultValue,
                                                              List<String> arguments, LocationInSource location,
                                                              Method method, List<String> propNameTokens) {
            super(name, defaultValue, arguments, location);
            this.method = method;
            this.propNameTokens = propNameTokens;
        }

        public List<String> memberPathTokens() {
            if (classNameTokens == null)
                classNameTokens = Stream.concat(
                        stream(method.getDeclaringClass().getName().split("\\.")),
                        Stream.of(method.getName())
                ).toList();
            return classNameTokens;
        }
    }

    /**
     *
     * @return nem tartalmaz üres stringet
     */
    private static List<String> getNameTokens(MethodWrapper mw) {
        Prefix prefixAnn = mw.getDeclaringClass().getAnnotation(Prefix.class);
        Name nameAnn = mw.getAnnotation(Name.class);

        String selfName = nameAnn == null ? mw.getName() : nameAnn.value();
        if (selfName.isEmpty())
            throw new LocalizationResourceDefinitionException(mw, "empty value in @" + Name.class.getName());

        if (prefixAnn == null)
            return List.of(selfName);
        else {
            String prefix = prefixAnn.value();
            if (prefix.isEmpty())
                throw new LocalizationResourceDefinitionException(mw, "empty value in @" + Prefix.class.getName());

            return List.of(prefix, selfName);
        }
    }

    public static String getName(MethodWrapper method) {
        return getNameTokens(method).stream().collect(joining("."));
    }

    public static String getDefaultValue(MethodWrapper method) {
        Text textAnn = method.getAnnotation(Text.class);
        if (textAnn == null)
            throw new LocalizationResourceDefinitionException(method,
                    "no @" + Text.class.getSimpleName() + " annotation");
        return textAnn.value();
    }


    // CB PropertyDefinitionből másolva
    static AnnotatedType findRichTextElementType(AnnotatedType t, MethodWrapper m) {
        return AnnotatedTypeVisitor.visit(t, new AnnotatedTypeVisitor<>() {

            @Override
            public AnnotatedType visitClass(AnnotatedType type, Class<?> clazz) {
                return null;
            }

            @Override
            public AnnotatedType visitParameterizedType(AnnotatedParameterizedType type, Class<?> rawType) {
                if (rawType == LocalizedRichText.class)
                    return type.getAnnotatedActualTypeArguments()[0];

                AnnotatedType t = null;
                for (AnnotatedType p : type.getAnnotatedActualTypeArguments())
                    t = merge(t, findRichTextElementType(p, m));
                return t;
            }

            @Override
            public AnnotatedType visitTypeVariable(AnnotatedTypeVariable type) {
                return null;
            }

            @Override
            public AnnotatedType visitArrayType(AnnotatedArrayType type) {
                return findRichTextElementType(type.getAnnotatedGenericComponentType(), m);
            }

            @Override
            public AnnotatedType visitWildcardType(AnnotatedWildcardType type) {
                AnnotatedType t = null;
                for (AnnotatedType p : type.getAnnotatedLowerBounds())
                    t = merge(t, findRichTextElementType(p, m));
                for (AnnotatedType p : type.getAnnotatedUpperBounds())
                    t = merge(t, findRichTextElementType(p, m));
                return t;
            }

            private AnnotatedType merge(AnnotatedType a, AnnotatedType b) {
                if (a == null)
                    return b;
                else if (b == null)
                    return a;
                else if (Types.equals(a, b))
                    return a;
                else
                    throw new LocalizationResourceDefinitionException(m, "incompatible types for rich text elements");
            }
        });
    }

    static class LocalizationResourceDefinitionException extends RuntimeException {
        public LocalizationResourceDefinitionException(MethodWrapper method, String message) {
            super(method.toShortString() + ": " + message);
        }
    }

    public interface AnnotatedElementWrapper {

        <A extends Annotation> A getAnnotation(Class<A> annotationType);

        /**
         * member esetén deklaráló teljes név és a member név lesz benne, class esetén a teljes osztálynév
         * @apiNote kompatibilis {@link ReflectionUtil#memberToShortString(AnnotatedElement)}-gel
         */
        String toShortString();
    }

    public interface ClassWrapper extends AnnotatedElementWrapper {

        boolean isAssignableTo(Class<?> c);

        boolean is(Class<?> c);

        boolean isInterface();

        @Override
        default String toShortString() { // ennek inkább finalnak kéne lennie, de interfaceben vagyunk
            return getName();
        }

        String getName();

        record ReflectionClassWrapper(Class<?> c) implements ClassWrapper {

            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                return c.getAnnotation(annotationType);
            }

            @Override
            public boolean isAssignableTo(Class<?> c) {
                return c.isAssignableFrom(this.c);
            }

            @Override
            public boolean is(Class<?> c) {
                return c == this.c;
            }

            @Override
            public boolean isInterface() {
                return c.isInterface();
            }

            @Override
            public String getName() {
                return c.getName();
            }
        }

        @CompileTime
        record TeaVMMetaprogrammingClassWrapper(ReflectClass<?> c) implements ClassWrapper {

            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                return c.getAnnotation(annotationType);
            }

            @Override
            public String toShortString() {
                return c.getName();
            }

            @Override
            public boolean isAssignableTo(Class<?> c) {
                return Metaprogramming.findClass(c).isAssignableFrom(this.c);
            }

            @Override
            public boolean is(Class<?> c) {
                return this.c.equals(Metaprogramming.findClass(c));
            }

            @Override
            public boolean isInterface() {
                return c.isInterface();
            }

            @Override
            public String getName() {
                return c.getName();
            }
        }
    }

    public interface MethodWrapper extends AnnotatedElementWrapper {

        String getName();

        ClassWrapper getDeclaringClass();

        record ReflectionMethodWrapper(Method m) implements MethodWrapper {

            @Override
            public String getName() {
                return m.getName();
            }

            @Override
            public ClassWrapper getDeclaringClass() {
                return new ReflectionClassWrapper(m.getDeclaringClass());
            }

            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                return m.getAnnotation(annotationType);
            }

            @Override
            public String toShortString() {
                return m.getDeclaringClass().getName() + "::" + m.getName();
            }
        }

        record TeaVMMetaprogrammingMethodWrapper(ReflectMethod teavmMethod) implements MethodWrapper {

            @Override
            public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
                return teavmMethod.getAnnotation(annotationType);
            }

            @Override
            public String getName() {
                return teavmMethod.getName();
            }

            @Override
            public ClassWrapper getDeclaringClass() {
                return new TeaVMMetaprogrammingClassWrapper(teavmMethod.getDeclaringClass());
            }

            @Override
            public String toShortString() {
                return teavmMethod.getDeclaringClass().getName() + "::" + teavmMethod.getName();
            }
        }
    }
}
