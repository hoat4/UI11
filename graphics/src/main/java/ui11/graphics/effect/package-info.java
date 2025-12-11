/**
 * Widgets which takes a child widget and applies some graphical effect on it.
 */
package ui11.graphics.effect;

// blending mode widgetet nem kell csinálni.
// problémák vele:
// 1. Sok composite operation befolyásolja a widgeten kívüli területet (Clear, Copy, Destination, Source Out, stb.).
//    tehát ha nem akarjuk hogy a descendantok belepiszkáljanak olyan területre ami nem is az övék, akkor
//    folyton composition layereket (vagy ahogy W3C hívja: "isolated group") kell létrehozni.
//    JavaFX ezt úgy oldotta meg, hogy BlendModeból kiszedték a problémásakat.
//    Ellenvetés, hogy ez a probléma így is megvan, Transform által bele tudunk piszkálni más területére.
//    El kéne dönteni hogy ClipPath az tényleg clippeljen, vagy csak valami layout iránymutatás.
// 2. Be kéne vezetni CompositionLayer widgetet.
//    Ezt JavaFX úgy csinálta meg, hogy minden Group egy layernek számított. Ez itt nem lenne jó,
//    mert
// 3. Zavaros, hogy a blending mode-ok és compositing operátorok kombinálhatóak-e.
//    Elvileg igen, gyakorlatilag mindenhol csak Source-Overrel használták a blend modeokat.
// 4. Ha bevezetünk egy CompositionMode interface-t, akkor azzal egy alternatív SPI rendszert csinálnánk,
//    a Widget hierarchia mellett.
//
// helyette minden blending vagy compositing mode legyen egy widget, ami megkapja a
// "source" és "destination" image-eknek megfelelő widgeteket (lásd példának Mask osztályt).