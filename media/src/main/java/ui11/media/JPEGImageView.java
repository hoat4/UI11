package ui11.media;

import ui11.SubstitutedWidget;
import ui11.media.ImageSource.BinaryImageSource;
import ui11.media.ImageSource.InlineBinarySource;
import ui11.media.ImageSource.URIImageSource;

import java.net.URI;
import java.util.Objects;

public class JPEGImageView extends SubstitutedWidget {

    private final BinaryImageSource source;

    private JPEGImageView(BinaryImageSource source) {
        this.source = Objects.requireNonNull(source);
    }

    public static JPEGImageView from(BinaryImageSource source) {
        // ha source instanceof InlineBinarySource, akkor lehetne mime typeot ellenőrizni
        return new JPEGImageView(source);
    }

    public static JPEGImageView fromURI(URI uri) {
        return new JPEGImageView(new URIImageSource(uri));
    }

    public static JPEGImageView fromURI(String uri) {
        return new JPEGImageView(new URIImageSource(uri));
    }

    public static JPEGImageView fromBytes(byte[] jpgBytes) {
        return new JPEGImageView(new InlineBinarySource(jpgBytes, "image/jpeg"));
    }

    public BinaryImageSource source() {
        return source;
    }
}
