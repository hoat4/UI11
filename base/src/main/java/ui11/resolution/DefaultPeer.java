package ui11.resolution;

import ui11.Widget;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

// TODO lehet hogy ezt is törölni kéne. de vannak értelmesnek tűnő use-caseek: pl. MouseRegion, BorderLayout

@Target(TYPE)
@Retention(RUNTIME)
// @Inherited mindegy, mert interface-ekről úgyse öröklődik, mindenképp manuálisan kell nézni
@Documented
public // mert subclassoknak is kell tudni róla
@interface DefaultPeer {
    Class<? extends Widget> value();
}
