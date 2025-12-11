package ui11.document;

import org.teavm.interop.PlatformMarker;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.fill.RasterImageView;
import ui11.graphics.fill.RasterImage;
import ui11.observable.MutableObservable;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Objects;

// TODO méretezést definiálni kéne (DOMEnv esetén most nem méreteződik át ha kevés a hely, hanem széthúzza a parentjét)

public final class URLImageView extends SubstitutedWidget {

    private final URI url;
    private final boolean interactive;

    public URLImageView(URI url, boolean interactive) {
        Objects.requireNonNull(url);
        this.url = url;
        this.interactive = interactive;
    }

    public URLImageView(URI url) {
        this(url, false);
    }

    public URI url() {
        return url;
    }

    public boolean interactive() {
        return interactive;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return new DefaultURLImageViewImpl(this);
    }

    @Override
    public String toString() {
        String urlStr = url.toString();
        if ("data".equals(url.getScheme()) && urlStr.length() > 33)
            urlStr = urlStr.substring(0, 30) + "...";
        return "URLImageView{url=" + urlStr + ", interative=" + interactive + "}";
    }

    private static class DefaultURLImageViewImpl extends Widget {

        private final URLImageView urlImageView;

        @State private MutableObservable<RasterImage> loadedImage;
        @State private MutableObservable<Boolean> error;
        @State private boolean loadBegan;

        public DefaultURLImageViewImpl(URLImageView urlImageView) {
            this.urlImageView = urlImageView;
        }

        @Override
        protected void initState() {
            loadedImage = MutableObservable.ofNullable();
            error = MutableObservable.withInitial(false);
        }

        @Override
        protected Widget build() {
            if (isTeaVM())
                throw new RuntimeException("should not reach here (UIV)");

            if (!loadBegan) {
                loadImageAsyncImpl();
                loadBegan = true;
            }

            RasterImage img = loadedImage.get();
            if (img != null)
                return new RasterImageView(img);
            else {
                URI url = urlImageView.url;
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
                    URI url = urlImageView.url;
                    // TODO mi legyen relatív URL esetén?
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

        @PlatformMarker
        private static boolean isTeaVM() {
            return false;
        }
    }
}
