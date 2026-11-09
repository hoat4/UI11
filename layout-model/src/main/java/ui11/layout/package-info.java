/**
 * Base package for layout containers. Layouts are {@linkplain ui11.Widget widgets} that
 * lays out their children, but they don't add additional graphics or behavior. The children are specified
 * at the creation of the layout containers.
 * <p>
 * This package only provide provides some utility classes, the layout containers are in the
 * {@link ui11.layout.singlechild} {@link ui11.layout.multichild} subpackages.
 * <p>
 * The layouts can be implemented by the native platform (as in the case in the {@code platform-dom}), or can rely on a
 * pure Java implementation e.g. {@code layout-constraint}.
 * <p>
 * A related package is {@link ui11.decoration}, which contains widget that not only lays out children but also
 * add graphics.
 *
 * @see ui11.decoration
 */
package ui11.layout;