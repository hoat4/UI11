package ui11.i18n;

import java.util.List;
import java.util.Objects;

public class LocalizableTextDefinition {

    public final String name;
    public final String message;
    public final List<String> arguments;
    public final LocationInSource location;

    public LocalizableTextDefinition(String name, String defaultValue, List<String> arguments, LocationInSource location) {
        this.name = Objects.requireNonNull(name);
        this.message = Objects.requireNonNull(defaultValue);
        this.arguments = List.copyOf(arguments);
        this.location = location;
    }

    @Override
    public String toString() {
        return "LocalizableTextDefinition [name=" + name + ", message=" + message
                + ", arguments=" + arguments + ", location=" + location
                + "]";
    }
}
