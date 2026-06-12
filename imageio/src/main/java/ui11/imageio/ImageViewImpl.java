package ui11.imageio;

import ui11.Widget;
import ui11.color.Color;
import ui11.media.ImageSource;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.fill.RasterImage;
import ui11.graphics.fill.RasterImageView;
import ui11.observable.MutableObservable;
import ui11.window.Shell.URLResolver;

import java.net.URI;

public class ImageViewImpl extends Widget {

    private final ImageSource imageSource;

    @Inject private URLResolver urlResolver;

    @Remember private MutableObservable<RasterImage> loadedImage;
    @Remember private MutableObservable<Boolean> error;
    @Remember private boolean loadBegan;

    public ImageViewImpl(ImageSource imageSource) {
        this.imageSource = imageSource;
    }

    @Override
    protected Widget build() {
        // TODO nem-raszteres képek

        if (!loadBegan) {
            loadImageAsyncImpl();
            loadBegan = true;
        }

        RasterImage img = loadedImage.get();
        if (img != null)
            return new RasterImageView(img);
        else {
            URI url = imageSource.toURI();
                /* TODO
                return SizingUtil.passiveSize(
                        Background.withBackground(
                                error.get() ? Color.RED : Color.YELLOW,
                                new Text(url.getFile().isEmpty() ? url.toString() : url.getFile())
                        )
                );
                 */

            // amíg nincs betöltve, a fájl nevét jelezzük ki
            // // return new Text(url.getPath().isEmpty() ? url.toString() : url.getPath());
            // de ez sem lehet, mióta text külön modul lett (2024-11-09)

            return new ColorFill(Color.GRAY);
        }
    }

    private void loadImageAsyncImpl() {
        new Thread(() -> {
            try {
                URI url = imageSource.toURI();
                if (!url.isAbsolute())
                    url = urlResolver.toAbsoluteURL(url);
                final RasterImage img = RasterImage.load(url.toURL());
                // TODO uiEnvironment.runLater(() -> {
                loadedImage.set(img);
                //});
            } catch (Throwable e) {
                error.set(true);
                e.printStackTrace(); // TODO
            }
        }, "Image Loader").start();
    }
}
