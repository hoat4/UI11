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
 * Az annotált metódust tartalmazó {@linkplain Widget} vagy
 * {@linkplain ui11.Component} minden leszármazotta megkapja
 * egy inherited value-t, aminek a típusa
 * az annotált metódus visszatérési típusa (vagy a mező típusa), és értéke az annotált metódus visszatérési értéke
 * (vagy a mező esetén a mezőben található {@linkplain ui11.observable.Observable}-ben lévő érték).
 * <p>
 * Minden childre vonatkozik, így az {@code Element.build} által visszaadott Widgetre, és a {@code Element.instantiate}
 * által létrehozott childekre is. Azonban az az {@code Element}, melyben van az annotált metódus vagy mező,
 * saját maga nem fogja érzékelni ezt a provideolt örökölhető értéket.
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Provide {
}
