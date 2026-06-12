package ui11;

import ui11.WidgetDefinitionParser.ProviderMethodInfo;
import ui11.observable.Observable;
import ui11.reflectutil.ReflectionUtil;
import ui11.provide.Provider;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

/**
 *
 * @param <T> widget type
 * @param <V> provided type
 */
class ProviderMethodDecorator<T extends Widget, V> {

    private final MethodHandle valueGetter; // (Node)boolean

    final ProviderMethodInfo<V> metadata;

    public ProviderMethodDecorator(ProviderMethodInfo<V> r) {
        this.metadata = r;

        // ellenőrzéseket ProviderMethodReflector már megcsinálta

        valueGetter = switch (r.member()) {
            case Method m -> {
                m.setAccessible(true);
                try {
                    yield lookup().unreflect(m).asType(methodType(Object.class, Widget.class));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("can't happen", e);
                }
            }
            case Field f -> {
                f.setAccessible(true);
                try {
                    MethodHandle fieldGetter = lookup().unreflectGetter(f);

                    MethodHandle RSP_get = lookup().findVirtual(Observable.class, "get",
                            MethodType.methodType(Object.class));
                    // TODO ha null van a mezőben, értelmesen kéne jelezni
                    yield MethodHandles.filterReturnValue(fieldGetter,
                                    RSP_get.asType(methodType(Object.class, f.getType()))).
                            asType(methodType(Object.class, Widget.class));
                } catch (IllegalAccessException | NoSuchMethodException e) {
                    throw new RuntimeException("shouldn't happen", e);
                }
            }
            default -> throw new RuntimeException("unknown member type: " +
                    ReflectionUtil.memberToShortString(r.member()));
        };
    }

    public Widget decorate(T e, Widget content) {
        Object value;
        try {
            value = valueGetter.invokeExact(e);
        } catch (Throwable ex) {
            throw new RuntimeException("Can't get value of " +
                    ReflectionUtil.memberToShortString(metadata.member()) + ": " + ex,
                    ex);
        }
        // nem kell ellenőrizni hogy value != null, mert legális a null érték
        return new Provider<>(metadata.providedType(), metadata.providedType().cast(value), content);
    }
}

