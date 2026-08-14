// TODO ez a javadoc évek óta elavult, és lehet hogy nem is kell ilyen közös module

//import ui11.Node;

/**
 * Felhasználói felület library. Részben deklaratív, ami alatt azt értjük, hogy nem azt írjuk le a felhasználói kódban,
 * hogy milyen műveletek hatására épüljön fel az element fa, hanem hogy milyen elementek legyenek benne milyen
 * tartalommal. Bővíthetőbb mint például a JavaFX.
 * <p>
 * A keretrendszert használó program egy {@link Widget} fát állít elő és módosít, amit aztán ez a
 * keretrendszer kijelez. A kijelzést egy platform specifikus render végzi. Például a DOM renderer a böngészőben fut
 * Javascriptre fordított kódként, és DOM objektumokat állít elő és update-eli őket az element fa alapján. Ilyenkor a
 * kliens program is Javascriptre van fordítva. A modult használó program input, timer, network stb. eseményekre
 * reagálva az element fát módosítja.
 * <p>
 * Beépített renderer-ek:
 *     <ul>
 *     <li>AWT: desktop alkalmazás a Java beépített AWT frameworkjével.
 *     <li>DOM: Javascript kód manipulálja a HTML elemeket dinamikusan.
 *     <li>HTML: statikus html-t előállít egy pillanatnyi állapotra.
 *     </ul>
 * <p>
 * A library része a {@link com.flyordie.ui} és az alatta lévő package-ek. Erősen épít a
 * {@link ui11.observable} modulra.
 * <p>
 * <h3>Hello world példa</h3>
 * Hello world alkalmazás példa a test osztályok között: ui11.HelloWorld
 * <p>
 * {@snippet :
 * import ui11.window.Desktop;
 *
 * Desktop.getDesktop().add("Hello world!");
 * }
 *
 * <h3>Beépített Elemek</h3>
 * Beépített elemek a következő package-ekben találhatók: <ul>
 * <li>{@code ui11.graphics graphics}: grafikai elemek, mint {@code Color}, {@code String},
 *     {@code Image}.
 * </li>
 * <li>{@code ui11.layout layout}: olyan konténerek, ami elrendezik valamilyen módon a gyerekeiket. Például
 *      {@code LinearLayout}, {@code Grid}.
 * </li>
 * </ul>
 */
module ui {}