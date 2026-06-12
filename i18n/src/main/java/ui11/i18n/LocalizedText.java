package ui11.i18n;

import org.jspecify.annotations.NonNull;
import ui11.Widget;
import ui11.observable.Observable;
import ui11.text.Text;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;

public sealed abstract class LocalizedText extends Widget {

    private final Locale locale;
    private final Object[] argArray;

    public LocalizedText(Locale locale, Object[] argArray) {
        this.locale = locale;
        this.argArray = argArray;
    }

    // toStringet is lehetne ehelyett felülírni, de ott furcsa ha IP-re subscribeolunk
    public String asString() {
        // TODO ha nincs format stringben '{', akkor ki lehet hagyni MessageFormatot
        //      (ha csak args.length == 0, akkor nem feltétlen, lásd
        //       RichTextPatternParser.setPatternben kommentet)
        MessageFormat messageFormat = new MessageFormat(getFormatString(), locale);
        return messageFormat.format(argArray);
    }

    abstract String getFormatString();

    @Override
    protected Widget build() {
        return new Text(asString());
    }

    static final class NonEditableLocalizedText extends LocalizedText {

        private final @NonNull String str;

        NonEditableLocalizedText(Locale locale, Object[] argArray, @NonNull String str) {
            super(locale, argArray);
            this.str = Objects.requireNonNull(str);
        }

        @Override
        String getFormatString() {
            return str;
        }
    }

    static final class EditableLocalizedText extends LocalizedText {

        private final String resid;
        private final Observable<String> o;

        EditableLocalizedText(Locale locale, Object[] argArray, String resid, Observable<String> o) {
            super(locale, argArray);
            this.resid = resid;
            this.o = o;
        }

        @Override
        String getFormatString() {
            return o.get();
        }

        @Override
        protected Widget build() {
            return new ResidBubble(resid, super.build());
        }
    }
}
