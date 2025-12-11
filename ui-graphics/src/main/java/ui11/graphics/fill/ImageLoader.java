package ui11.graphics.fill;

import ui11.graphics.fill.RasterImage;

import java.io.IOException;
import java.net.URL;

/**
 * SPI képek betöltésére; a platformspecifikus modulok implementálják. A felhasználói program nem követlenül ezt, hanem az
 * {@link RasterImage#load(URL)} függvényt használja. Ezt jobb is lenne átalakítani úgy, hogy egy örökölt érték legyen az
 * element fában.
 *
 * @see RasterImage#load(URL)
 */
public interface ImageLoader {

    RasterImage loadImage(URL url) throws IOException;
}
