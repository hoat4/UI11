package ui11.platform.dom;

import ui11.css.StylesheetPreparer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class DOMStylesheetPreparer implements StylesheetPreparer<String> {
    @Override
    public String prepare(URL url) {
        try (InputStream in = url.openStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8); // TODO Content-Encoding figyelembe vétele
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO
        }
    }
}
