package ui11.provide;

import ui11.Widget;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// TODO le kéne írni valahol, hogy ez működik Element.instantiate-dal, míg más decoratorannotáció nem
// TODO kéne tudnia működni nem Observable final fielddel is
// TODO nullok kezelését dokumentálni kéne, illetve azt is hogy okoz-e rebuildet
// TODO meg kéne nézni, hogy mi történik, ha az annotált függvény folyton más értéket ad vissza, de
//      nem értesít observert

/**
 * All descendants in the widget containing the annotated field/method will receive an inherited value,
 * which will have a type of the annotated method's type (or field's type), and value of the annotated method's
 * return value (or in case or fields, the value in the {@linkplain ui11.observable.Observable}).
 * <p>
 * The widget itself that has the annotated field or method the value won't receive the provided value via
 * {@code @Inject}.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Provide {
}
