package ui11.input.focus;

public class FocusHolder {

    public final boolean autofocus; // ez majd lehet hogy ne legyen publikus

    public FocusHolder() {
        this(false);
    }

    public FocusHolder(boolean autofocus) {
        this.autofocus = autofocus;
    }
}
