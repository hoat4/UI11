package ui11.designtoken.model;

import ui11.designtoken.model.Element.ValueElement;
import ui11.designtoken.model.tokenvalue.TokenValue;

public sealed interface ValueOrRef<V extends ValueElement> {

    final class Value<V extends ValueElement> implements ValueOrRef<V> {
        public V value;
    }

    abstract sealed class Ref<V extends ValueElement> implements ValueOrRef<V> {

        public Document document;

        public Class<V> valueType;

        public abstract V resolve();
    }

    final class TokenRef<V extends TokenValue> extends Ref<V> {

        /**
         * Without '{' and '}'
         */
        public String tokenPath;

        @Override
        public V resolve() {
            Node node = document.findGroupOrToken(tokenPath);
            switch (node) {
                case Group _ -> {
                    throw new IllegalArgumentException("path refers to a group instead" +
                            " of token: {" + tokenPath + "}");
                }
                case Token<?> token -> {
                    return valueType.cast(token.value);
                }
            }
        }
    }

    final class JsonPointerRef<V extends ValueElement> extends Ref<V> {

        public String jsonPointer;

        @Override
        public V resolve() {
            ValueElement valueElement = document.findElement(jsonPointer);
            return valueType.cast(valueElement);
        }
    }
}
