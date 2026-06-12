package ui11.asset;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.core.json.JsonFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

class LottieDetector {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    public static boolean isLikelyLottie(Path jsonFile) throws IOException {
        try (JsonParser parser = JSON_FACTORY.createParser(ObjectReadContext.empty(), jsonFile)) {

            // Root must be an object
            if (parser.nextToken() != JsonToken.START_OBJECT)
                return false;

            int foundFields = 0;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.currentName();

                parser.nextToken(); // move to field value

                JsonToken token = parser.currentToken();
                switch (fieldName) {
                    case "v" -> {
                        foundFields |= 1;
                        if (token != JsonToken.VALUE_STRING)
                            return false;
                    }
                    case "fr" -> {
                        foundFields |= 2;
                        if (!token.isNumeric())
                            return false;
                    }
                    case "ip" -> {
                        foundFields |= 4;
                        if (!token.isNumeric())
                            return false;
                    }
                    case "op" -> {
                        foundFields |= 8;
                        if (!token.isNumeric())
                            return false;
                    }
                    case "w" -> {
                        foundFields |= 16;
                        if (!token.isNumeric())
                            return false;
                    }
                    case "h" -> {
                        foundFields |= 32;
                        if (!token.isNumeric())
                            return false;
                    }
                    case "layers" -> {
                        foundFields |= 64;

                        // Heuristic: layers should be a non-empty array

                        if (token != JsonToken.START_ARRAY)
                            return false;

                        int count = 0;

                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            count++;
                            parser.skipChildren();
                        }

                        if (count == 0)
                            return false;
                    }
                    default -> parser.skipChildren();
                }
            }

            return foundFields == 127;
        } catch (JacksonIOException e) {
            throw e.getCause(); // IOException
        }
    }
}