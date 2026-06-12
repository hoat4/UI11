package ui11.designtoken.model.parser;

import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class JSONParser implements Closeable {

    private int line = 1, col = 1;
    private int pushedBackChar;
    private boolean isFirstElement; // in array or object

    private final InputStream utf8Input;
    private final Reader utf16Input;

    private final StringBuilder stringDecodeBuffer = new StringBuilder();

    public JSONParser(String s) {
        this.utf8Input = null;
        this.utf16Input = new StringReader(s);
    }

    /**
     * Decodes the specified input stream as UTF-8
     */
    public JSONParser(InputStream s) {
        this.utf8Input = s;
        this.utf16Input = null;
    }

    /**
     * Decodes the specified input stream as UTF-8
     */
    public JSONParser(Path file) throws IOException {
        this.utf8Input = Files.newInputStream(file);
        this.utf16Input = null;
    }

    private boolean isUTF8() {
        return utf8Input != null;
    }

    /**
     * Reads a UTF-16 code unit if the source is a {@link Reader}, or reads a raw byte (probably UTF-8 encoded) if the
     * source is an {@link InputStream}.
     *
     * @return -1 if end of file
     */
    private int readCharOrByte() throws IOException {
        if (pushedBackChar != 0) {
            int c = pushedBackChar;
            pushedBackChar = 0;
            return c;
        }

        return readCharOrByte_noPushBack();
    }

    /**
     * @return -1 if end of file
     */
    private int readCharOrByte_noPushBack() throws IOException {
        col++;
        if (isUTF8())
            return utf8Input.read();
        else
            return utf16Input.read();
    }

    private void pushBack(int c) {
        pushedBackChar = c;
    }

    public ValueType nextValueType() throws IOException, JSONSyntaxException {
        while (true) {
            int c = readCharOrByte();
            switch (c) {
                case ' ', '\t', '\r' -> {
                }
                case '\n' -> {
                    line++;
                    col = 1;
                }
                case '{' -> {
                    isFirstElement = true;
                    return ValueType.OBJECT;
                }
                case '[' -> {
                    isFirstElement = true;
                    return ValueType.ARRAY;
                }
                case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    pushBack(c);
                    return ValueType.NUMBER;
                }
                case '"' -> {
                    return ValueType.STRING;
                }
                case 'f' -> {
                    expect('a');
                    expect('l');
                    expect('s');
                    expect('e');
                    return ValueType.FALSE;
                }
                case 't' -> {
                    expect('r');
                    expect('u');
                    expect('e');
                    return ValueType.TRUE;
                }
                case 'n' -> {
                    expect('u');
                    expect('l');
                    expect('l');
                    return ValueType.NULL;
                }
                default -> {
                    throw unexpectedCharacter(c, "'{', '[', '-', '\"', " +
                            "a number, false, true, null, or whitespace");
                }
            }
        }
    }

    private void expect(char expected) throws IOException, JSONSyntaxException {
        int actual = readCharOrByte_noPushBack();
        if (actual != expected) {
            throw unexpectedCharacter(actual, new String(new char[]{expected}));
        }
    }

    /**
     * Only applicable in an {@link ValueType#OBJECT}.
     *
     * @return {@code null} if there are no remaining properties in this object, otherwise the name of the next property
     */
    public String nextPropertyName() throws IOException, JSONSyntaxException {
        boolean foundComma = false;
        String propName = null;
        while (true) {
            int c = readCharOrByte();
            switch (c) {
                case ' ', '\t', '\r' -> {
                }
                case '\n' -> {
                    line++;
                    col = 1;
                }
                case '}' -> {
                    if (propName != null)
                        throw unexpectedCharacter(c, "':' or whitespace");
                    if (foundComma)
                        throw unexpectedCharacter(c, "'\"' or whitespace");
                    return null;
                }

                case '"' -> {
                    if (propName != null)
                        throw unexpectedCharacter(c, "':' or whitespace");
                    if (isFirstElement || foundComma) {
                        isFirstElement = false;
                        propName = readString();
                    } else
                        throw unexpectedCharacter(c, "',', '}' or whitespace");
                }
                case ':' -> {
                    if (propName == null)
                        throw unexpectedCharacter(c,
                                "property name string, '}' or whitespace");
                    return propName;
                }
                case ',' -> {
                    if (isFirstElement || foundComma || propName != null)
                        throw unexpectedCharacter(c,
                                propName == null ?
                                        "property name string, '}' or whitespace" :
                                        "':' or whitespace");
                    foundComma = true;
                }
                default -> {
                    throw unexpectedCharacter(c,
                            (isFirstElement || foundComma) ?
                                    "property name string, '}' or whitespace" :
                                    "',', '}' or whitespace");
                }
            }
        }
    }

    /**
     * Only applicable in a {@link ValueType#ARRAY}.
     */
    public boolean hasMoreElements() throws IOException, JSONSyntaxException {
        boolean foundComma = false;
        while (true) {
            int c = readCharOrByte();
            switch (c) {
                case ' ', '\t', '\r' -> {
                }
                case '\n' -> {
                    line++;
                    col = 1;
                }
                case ']' -> {
                    return false;
                }
                case ',' -> {
                    if (isFirstElement || foundComma)
                        throw unexpectedCharacter(c, "'\"', number, " +
                                "'[', ']', '{', false, true, null or whitespace");
                    foundComma = true;
                }
                default -> {
                    isFirstElement = false;
                    pushBack(c);
                    return true;
                }
            }
        }
    }

    /**
     * Can be only called if {@link #nextValueType()} returned {@link ValueType#OBJECT} or {@link #nextPropertyName()} ()}
     * returned non null or {@link #hasMoreElements()} returned {@code true}.
     */
    public String readString() throws IOException, JSONSyntaxException {
        int ch;
        while ((ch = readCharOrByte()) != '"') {
            if (ch == '\\') {
                stringDecodeBuffer.append(strDecodeEscapeSequence());
            } else if (ch >= 128 && isUTF8()) {
                strDecodeUtf8Char((byte) ch, stringDecodeBuffer);
            } else {
                if (ch < 32)
                    throw syntaxError("Control character in string literal");
                stringDecodeBuffer.append((char) ch);
            }
        }

        String s = stringDecodeBuffer.toString();
        stringDecodeBuffer.setLength(0);

        return s;
    }

    /**
     * Only callable if {@link #nextValueType()} returned {@link ValueType#STRING}.
     */
    public void skipString() throws IOException, JSONSyntaxException {
        int ch;
        while ((ch = readCharOrByte()) != '"') {
            if (ch == '\\') {
                strDecodeEscapeSequence();
            } else if (ch >= 128 && isUTF8()) {
                strDecodeUtf8Char((byte) ch, null);
            } else {
                if (ch < 32)
                    throw syntaxError("Control character in string literal");
            }
        }
    }

    private char strDecodeEscapeSequence() throws IOException, JSONSyntaxException {
        int c = readASCII_noPushBack();
        if (c == 'u') {
            // azért +1, hogy EOF-ra ne dobjon AIOOBE-t
            int d1 = HEX_DIGIT_VALUES[readASCII_noPushBack() + 1];
            int d2 = HEX_DIGIT_VALUES[readASCII_noPushBack() + 1];
            int d3 = HEX_DIGIT_VALUES[readASCII_noPushBack() + 1];
            int d4 = HEX_DIGIT_VALUES[readASCII_noPushBack() + 1];
            if ((d1 | d2 | d3 | d4) < 0)
                throw syntaxError("Invalid escape sequence");
            return (char) (d1 << 12 | d2 << 8 | d3 << 4 | d4);
        }

        int e = ESCAPE_CHARACTERS[c + 1];
        if (e == 0)
            throw syntaxError("Invalid escape character " + c);
        return (char) e;
    }

    // ha ezt escape sequenceek beolvasásán kívül másra is használjuk, akkor
    // módosítsuk az exception messageet
    private int readASCII_noPushBack() throws IOException, JSONSyntaxException {
        int c = readCharOrByte_noPushBack();
        if ((c & ~127) != 0)
            throw syntaxError("Invalid escape character " + c);
        return c;
    }

    private void strDecodeUtf8Char(byte b1, @Nullable StringBuilder out) throws IOException, JSONSyntaxException {
        // b1 < 0

        if ((b1 >> 5) == -2 && (b1 & 0x1e) != 0) {
            int b2 = readCharOrByte_noPushBack();
            if (isNotContinuation(b2))
                throw syntaxError((b2 == -1 ? "Unfinished" : "Malformed") +
                        " two-byte UTF-8 byte sequence: " + b1 + ", " + b2);
            if (out != null)
                out.append(decode2(b1, b2));
        } else if ((b1 >> 4) == -2) {
            int b2 = readCharOrByte_noPushBack();
            int b3 = readCharOrByte_noPushBack();
            if (b3 == -1)
                throw syntaxError("Unfinished three-byte UTF-8 byte sequence: " +
                        b1 + ", " + b2 + ", " + b3);

            if (isMalformed3(b1, b2, b3))
                throw syntaxError("Malformed three-byte UTF-8 byte sequence: " +
                        b1 + ", " + b2 + ", " + b3);

            char c = decode3(b1, b2, b3);
            if (Character.isSurrogate(c))
                throw syntaxError("Invalid three-byte UTF-8 byte sequence, " +
                        "decoded character is a surrogate: " + (int) c);
            if (out != null)
                out.append(c);
        } else if ((b1 >> 3) == -2) {
            int b2 = readCharOrByte_noPushBack();
            int b3 = readCharOrByte_noPushBack();
            int b4 = readCharOrByte_noPushBack();
            if (b4 == -1)
                throw syntaxError("Unfinished four-byte UTF-8 byte sequence" +
                        b1 + ", " + b2 + ", " + b3 + ", " + b4);

            int uc = decode4(b1, b2, b3, b4);
            if (isMalformed4(b2, b3, b4) ||
                    !Character.isSupplementaryCodePoint(uc))
                throw syntaxError("Malformed four-byte UTF-8 byte sequence: " +
                        b1 + ", " + b2 + ", " + b3 + ", " + b4);

            if (out != null) {
                out.append(Character.highSurrogate(uc));
                out.append(Character.lowSurrogate(uc));
            }
        } else {
            throw syntaxError("Invalid UTF-8 byte sequence");
        }
    }

    private static boolean isNotContinuation(int b) {
        return (b & 0xc0) != 0x80;
    }

    private static boolean isMalformed3(int b1, int b2, int b3) {
        return (b1 == (byte) 0xe0 && (b2 & 0xe0) == 0x80) ||
                (b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80;
    }

    private static boolean isMalformed4(int b2, int b3, int b4) {
        return (b2 & 0xc0) != 0x80 || (b3 & 0xc0) != 0x80 ||
                (b4 & 0xc0) != 0x80;
    }

    // j.l.Stringből másolva ez a 3 függvény
    @SuppressWarnings("PointlessBitwiseExpression")
    private static char decode2(int b1, int b2) {
        b1 = (byte) b1;
        b2 = (byte) b2;
        return (char) (((b1 << 6) ^ b2) ^
                (((byte) 0xC0 << 6) ^
                        ((byte) 0x80 << 0)));
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    private static char decode3(int b1, int b2, int b3) {
        b1 = (byte) b1;
        b2 = (byte) b2;
        b3 = (byte) b3;
        return (char) ((b1 << 12) ^
                (b2 << 6) ^
                (b3 ^
                        (((byte) 0xE0 << 12) ^
                                ((byte) 0x80 << 6) ^
                                ((byte) 0x80 << 0))));
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    private static int decode4(int b1, int b2, int b3, int b4) {
        b1 = (byte) b1;
        b2 = (byte) b2;
        b3 = (byte) b3;
        b4 = (byte) b4;
        return ((b1 << 18) ^
                (b2 << 12) ^
                (b3 << 6) ^
                (b4 ^
                        (((byte) 0xF0 << 18) ^
                                ((byte) 0x80 << 12) ^
                                ((byte) 0x80 << 6) ^
                                ((byte) 0x80 << 0))));
    }

    /**
     * Can be only called after {@link #nextValueType()} returned {@link ValueType#NUMBER}.
     */
    public double readDouble() throws IOException, JSONSyntaxException {
        boolean sign = false;
        int number = pushedBackChar - '0';
        pushedBackChar = 0;
        if (number == '-' - '0') {
            sign = true;
            number = readCharOrByte() - '0';
            if (number < 0 || number > 9)
                throw syntaxError("not a digit after '-'");
        }

        // fast-path for ints
        int c = 0;
        for (int i = 0; i < 8; i++) {
            c = readCharOrByte_noPushBack();
            if (c >= '0' && c <= '9') {
                if (number == 0)
                    throw syntaxError("Digit after leading zero digit");
                number = number * 10 + c - '0';
            } else if (c == '.' || (c | 0x20) == 'e' || c == '-' || c == '+')
                break;
            else {
                pushBack(c);
                if (sign)
                    number = -number;
                return number;
            }
        }

        // slow-path
        StringBuilder sb = new StringBuilder();
        if (sign)
            sb.append('-');
        sb.append(number);
        sb.append((char) c);

        while (true) {
            c = readCharOrByte();
            if (c >= '0' && c <= '9' || c == '.' || (c | 0x20) == 'e' || c == '-' || c == '+')
                sb.append((char) c);
            else {
                pushBack(c);
                break;
            }
        }

        String s = sb.toString();

        double d;
        try {
            d = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            JSONSyntaxException jsonSyntaxException = syntaxError("Not a number: " + s.toString());
            jsonSyntaxException.initCause(e);
            throw jsonSyntaxException;
        }
        return d;
    }

    public int currentLine() {
        return line;
    }

    public int currentColumn() {
        // TODO ez a token végét mutatja, nem az elejét
        return col - (pushedBackChar == 0 ? 0 : 1);
    }

    @Override
    public void close() throws IOException {
        if (isUTF8())
            utf8Input.close();
        else
            utf16Input.close();
    }

    private JSONSyntaxException unexpectedCharacter(int c, String expected) {
        if (c == -1)
            return syntaxError("unexpected end of document, expected " + expected + " instead");
        else
            return syntaxError("unexpected '" + ((char) c) + "' (char code: " + c + "), " +
                    "expected " + expected + " instead");
    }

    private JSONSyntaxException syntaxError(String msg) {
        return new JSONSyntaxException("JSON syntax error at line " + currentLine() +
                " column " + currentColumn() + ": " + msg);
    }

    public enum ValueType {
        OBJECT, ARRAY, NUMBER, STRING, FALSE, TRUE, NULL
    }

    private static final byte[] ESCAPE_CHARACTERS = new byte[257];
    private static final byte[] HEX_DIGIT_VALUES = new byte[257];

    static {
        ESCAPE_CHARACTERS['"' + 1] = '"';
        ESCAPE_CHARACTERS['\\' + 1] = '\\';
        ESCAPE_CHARACTERS['/' + 1] = '/';
        ESCAPE_CHARACTERS['b' + 1] = '\b'; // U+0008
        ESCAPE_CHARACTERS['f' + 1] = '\f';
        ESCAPE_CHARACTERS['n' + 1] = '\n';
        ESCAPE_CHARACTERS['r' + 1] = '\r';
        ESCAPE_CHARACTERS['t' + 1] = '\t';

        Arrays.fill(HEX_DIGIT_VALUES, (byte) -1);
        HEX_DIGIT_VALUES['0' + 1] = 0;
        HEX_DIGIT_VALUES['1' + 1] = 1;
        HEX_DIGIT_VALUES['2' + 1] = 2;
        HEX_DIGIT_VALUES['3' + 1] = 3;
        HEX_DIGIT_VALUES['4' + 1] = 4;
        HEX_DIGIT_VALUES['5' + 1] = 5;
        HEX_DIGIT_VALUES['6' + 1] = 6;
        HEX_DIGIT_VALUES['7' + 1] = 7;
        HEX_DIGIT_VALUES['8' + 1] = 8;
        HEX_DIGIT_VALUES['9' + 1] = 9;
        HEX_DIGIT_VALUES['A' + 1] = 10;
        HEX_DIGIT_VALUES['B' + 1] = 11;
        HEX_DIGIT_VALUES['C' + 1] = 12;
        HEX_DIGIT_VALUES['D' + 1] = 13;
        HEX_DIGIT_VALUES['E' + 1] = 14;
        HEX_DIGIT_VALUES['F' + 1] = 15;
        HEX_DIGIT_VALUES['a' + 1] = 10;
        HEX_DIGIT_VALUES['b' + 1] = 11;
        HEX_DIGIT_VALUES['c' + 1] = 12;
        HEX_DIGIT_VALUES['d' + 1] = 13;
        HEX_DIGIT_VALUES['e' + 1] = 14;
        HEX_DIGIT_VALUES['f' + 1] = 15;
    }

    public static class JSONSyntaxException extends Exception {
        private JSONSyntaxException(String message) {
            super(message);
        }
    }
}
