package ui11.i18n;

import ui11.Widget;
import ui11.layout.singlechild.Align;
import ui11.provide.Provider;
import ui11.text.Text;
import ui11.window.Window;

import java.util.Locale;
import java.util.Optional;

public class LocalizedResourceTest {

    public static void main(String[] args) {
        LocalizationTextSource resourceBundle = new LocalizationTextSource() {
            @Override
            public Optional<String> find(String name) {
                return switch (name) {
                    case "greeting" -> Optional.of("bigyó {0}");
                    default -> Optional.empty();
                };
            }

            @Override
            public Locale locale() {
                return Locale.of("hu", "HU");
            }
        };

        Widget content = Align.center(new Widget() {
            @Inject private LocalizedResourceTestI i18n;

            @Override
            protected Widget build() {
                return i18n.greeting("izé");
            }
        });
        content = new Provider<>(LocalizationTextSource.class, resourceBundle, content);
        // TODO content = new Provider<>(DynamicProvider.class, new LocalizationInterfaceDP(resourceBundle), content);
        Window.open(content);
    }

    public interface LocalizedResourceTestI extends LocalizedResources {
        @Text("Hello {0}!")
        LocalizedText greeting(String name);
    }
}
