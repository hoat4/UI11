package ui11.text;

import ui11.resolution.SubstitutedWidget;

import java.util.Objects;

public final class Text extends SubstitutedWidget {

    private final String text;

    public Text(String text) {
        Objects.requireNonNull(text);
        this.text = text;
    }

    public Text(int i) {
        this(Integer.toString(i));
    }

    public String text() {
        return text;
    }
}
