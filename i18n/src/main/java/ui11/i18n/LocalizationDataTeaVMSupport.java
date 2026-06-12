package ui11.i18n;

import org.teavm.metaprogramming.Value;

public interface LocalizationDataTeaVMSupport {

    Value<String> makeLocalizedStringGetter(String resid);
}
