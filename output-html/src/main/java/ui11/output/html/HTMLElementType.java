package ui11.output.html;

import ui11.output.html.util.UTF8Writer;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

public enum HTMLElementType {

    TABLE, INPUT, IMG, DIV, A, BODY, TD;

    private final byte[] begin = ("<" + name().toLowerCase(Locale.ROOT)).getBytes(StandardCharsets.UTF_8);
    private final byte[] end = ("</" + name().toLowerCase(Locale.ROOT) + "\n>").getBytes(StandardCharsets.UTF_8);

    public void writeBeginElement(UTF8Writer out) {
        out.writeBytes(begin);
    }

    public void writeEndElement(UTF8Writer out) {
        out.writeBytes(end);
    }
}
