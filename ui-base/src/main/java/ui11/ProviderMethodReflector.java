package ui11;

import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.reflectutil.ReflectionUtil;
import ui11.ElementDefReflector.DecoratorReflector;
import ui11.meta.DecoratorAnnotation.DecoratorAnnotationHandler.Decorator;
import ui11.provide.Provider;
import org.teavm.metaprogramming.Metaprogramming;
import org.teavm.metaprogramming.Value;
import org.teavm.metaprogramming.reflect.ReflectField;
import org.teavm.metaprogramming.reflect.ReflectMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Objects;

@org.teavm.metaprogramming.CompileTime
final class ProviderMethodReflector<V> implements DecoratorReflector {

    final AnnotatedElement member;
    final Annotation ann;
    final Class<V> providedType;

    public ProviderMethodReflector(AnnotatedElement member, Annotation ann) {
        this.member = member;
        this.ann = ann;

        // TODO detektálni kéne ha van kettő is, ami ugyanolyan type-ú
        switch (member) {
            // Classon nem alkalmazható
            case Method m -> {
                if (Modifier.isStatic(m.getModifiers()))
                    throw error(memberStr() + " must be non static");
                if (m.getReturnType() == void.class)
                    // TODO ki kéne írni hogy miért nem lehet
                    throw error("return type of " + memberStr() + " is void");
                if (m.getParameterCount() != 0)
                    throw error(memberStr() + " has parameters");
                providedType = (Class<V>) m.getReturnType();
            }
            case Field f -> {
                if (f.getType() != MutableObservable.class && f.getType() != Observable.class)
                    throw error("a field which is annotated with " + ann.annotationType() + " " +
                            "must have type " + MutableObservable.class.getSimpleName() + "<...>, " +
                            Observable.class.getSimpleName() + "<...>, " +
                            "but " + ReflectionUtil.memberToShortString(f) + " has type: " +
                            ReflectionUtil.typeToString(f.getAnnotatedType()));
                if (Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers()))
                    throw error("a field which is annotated with " + ann.annotationType() + " " +
                            "must be final and non static");
                // TODO jelezni kéne értelmes üzenettel, ha rawtype
                providedType = (Class<V>) ReflectionUtil.rawType(((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]);
                System.out.println("typeof " + f + " is " + providedType);
            }
            default -> throw new RuntimeException("unknown member type: " + memberStr());
        }
    }

    @Override
    public boolean neededForAllChildren() {
        return true;
    }

    @Override
    public Decorator<?> makeDecorator() {
        return new ProviderMethodDecorator<>(this);
    }

    @Override
    public Value<? extends Widget> makeTeaVMCode(Value<? extends Widget> o, Value<?> e, int decoratorIndex) {
        // TeaVM bug: Metaprogramming.emit nem működik, ha a lambda capture-öli this-t. jelenteni kéne nekik.
        // ezért van kiszedve lokál varba a providedType
        final Class<V> providedType = this.providedType;

        Value<V> valRef;
        switch (member) {
            case Method m -> {
                ReflectMethod teavmMethod = TeaVMElementAccessorFactory.findMethod(m);
                valRef = Metaprogramming.emit(() ->
                        providedType.cast(teavmMethod.invoke(e.get())));
            }
            case Field f -> {
                ReflectField teavmField = TeaVMElementAccessorFactory.findField(f,
                        Metaprogramming.findClass(f.getDeclaringClass()));
                valRef = Metaprogramming.emit(() -> {
                    Observable<?> rsp = (Observable<?>) teavmField.get(e.get());
                    // TODO mi legyen, ha rsp null?
                    Objects.requireNonNull(rsp, "rsp");
                    return providedType.cast(rsp.get());
                });
            }
            default -> throw new RuntimeException("unknown member for " + this);
        }
        return Metaprogramming.emit(() -> {
            return new Provider<>(providedType, valRef.get(), o.get());
        });
    }

    private RuntimeException error(String msg) {
        throw new RuntimeException(msg + " (used at " + ReflectionUtil.memberToShortString(member) + ", " +
                "which is annotated by " + ann + ")");
    }

    String memberStr() {
        return ReflectionUtil.memberToShortString(this.member);
    }
}
