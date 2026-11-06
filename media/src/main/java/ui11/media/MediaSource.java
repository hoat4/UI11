package ui11.media;

import org.jspecify.annotations.NonNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public sealed interface MediaSource {

    default URI toURI() {
        return URI.create(toURIString());
    }

    String toURIString();

    sealed interface TextualMediaSource extends MediaSource {}

    sealed interface BinaryMediaSource extends MediaSource {}

    record URIMediaSource(URI uri) implements TextualMediaSource, BinaryMediaSource {

        public URIMediaSource {
            Objects.requireNonNull(uri);
        }

        public URIMediaSource(String uri) {
            this(URI.create(uri));
        }

        public URIMediaSource(URL url) {
            this(toURI(url));
        }

        private static URI toURI(URL url) {
            try {
                return url.toURI();
            } catch (URISyntaxException e) {
                // ez az exception üzenet se jó, mert valamit ellenőriz authority-kkel kapcsolatban a toURI
                throw new IllegalArgumentException("URL not compliant to RFC 2396: " + url, e);
            }
        }

        @Override
        public URI toURI() {
            return uri;
        }

        @Override
        public String toURIString() {
            return uri.toString();
        }

        @Override
        public @NonNull String toString() {
            String urlStr = uri.toString();
            if ("data".equals(uri.getScheme()) && urlStr.length() > 33)
                urlStr = urlStr.substring(0, 30) + "...";
            return URIMediaSource.class.getSimpleName() + "{" + urlStr + "}";
        }
    }

    // TODO érthetőbb osztálynév
    record InlineStringSource(String content, String mimeType) implements TextualMediaSource {

        public InlineStringSource {
            Objects.requireNonNull(content);
        }

        @Override
        public String toURIString() {
            // RFC-8259 szerint UTF-8 kell JSON-hoz (régebbi engedett UTF-16-ot és UTF-32-t)
            byte[] bytes = content.getBytes(UTF_8);
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        }

        @Override
        public @NonNull String toString() {
            String s = this.content;
            if (s.length() > 33)
                s = s.substring(0, 30) + "...";
            return InlineStringSource.class.getSimpleName() + "{mimeType=" + mimeType + ", source=" + s + "}";
        }
    }

    // TODO érthetőbb osztálynév
    record InlineBinarySource(byte[] bytes, String mimeType) implements BinaryMediaSource {

        // TODO mime typeot ellenőrizni hogy nem hülyeség-e

        public InlineBinarySource {
            Objects.requireNonNull(bytes);
            Objects.requireNonNull(mimeType);
        }

        @Override
        public String toURIString() {
            return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        }
    }
}
