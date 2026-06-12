package ui11.i18n;

import java.util.Locale;
import java.util.Optional;

public interface LocalizationTextSource {

    Optional<String> find(String name);

    Locale locale();
}
