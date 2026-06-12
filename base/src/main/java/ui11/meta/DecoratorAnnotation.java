package ui11.meta;

import org.teavm.metaprogramming.Value;
import ui11.Widget;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Az ezzel annotált annotációk alkalmazhatóak Widgetekre és Elementekre is.
 * <p>
 * Az ezzel annotált annotációkkal annotált osztályok/interfaceek leszármazottjai is öröklődik
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Deprecated
public @interface DecoratorAnnotation {

    Class<? extends DecoratorAnnotationHandler<?>> handler();

    interface DecoratorAnnotationHandler<ANN extends Annotation> {

        /**
         * @param <M> Widget vagy Element leszármazottja
         */
        <M> Decorator<M> makeDecorator(AnnotatedElement type, ANN annotation);

        /**
         * @param <M> Widget vagy Element leszármazottja
         */
        interface Decorator<M> {

            default boolean applies(M m) {
                return true;
            }

            Widget decorate(M m, Widget content);
        }
    }

    /**
     * legyen egy statikus innerclassa a DecoratorAnnotationHandler implementációnak, aminek a neve "TeaVMSupport"
     * legyen. így sikerült workaroundolni a metaprogramming classloaderes hülyeségeiket.
     * <p>
     * Az implementációkat annotáljon @{link org.teavm.metaprogramming.CompileTime}-mal
     */
    interface TeaVMSupportingDecoratorAnnotationHandler<ANN extends Annotation> {

        /**
         * @param elementDefinition Widget vagy Element leszármazottja
         */
        Value<? extends Widget> generateDecoratorCode(AnnotatedElement elementDefinition, ANN annotation,
                                                      Value<?> modelRef, Value<? extends Widget> contentRef);
    }
}
