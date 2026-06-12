package ui11;

import ui11.provide.UpValue;
import ui11.provide.UpValueWrapper;

// TODO javadoc frissítése
/**
 * Egy szülő Element életciklusához kapcsolódik, de saját maga nem tartalmaz widgetet.
 */
public abstract class Component extends Widget {

    private static final UpValue CHAIN_END = new UpValue() {
    };

    @Override
    protected final Widget build() {
        update();
        return new UpValueWrapper(CHAIN_END);
    }

    protected void update() {
    }
}
