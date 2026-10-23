package ui11.output.html.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import static java.lang.System.arraycopy;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.Consumer;

/**
 * Abban különbözik {@linkplain java.io.OutputStreamWriter OutputStreamWritertől}, hogy:
 * <ul>
 *     <li>dinamikusan növekvő bufferbe tud írni, nem fix méretűbe (tehát kb. mint mondjuk OutputStreamWriter +
 *     ByteArrayOutputStream kombinációja). Ez azért hasznos, mert ha egy lap generálása közben van egy hiba,
 *     akkor már nem feltétlen tudunk error pageet írni, ha félig elküldtünk már egy választ.</li>
 *     <li>kicsit gyorsabb OutputStreamWriternél, mert csak egy kódolást kell ismernie. Legalábbis 2020 körül még
 *     gyorsabb volt, azóta nem mértem.</li>
 *     <li>Be tud szúrni előre enkódolt részleteket a kimenetbe, ld. {@link #writeBytes(byte[])}</li>
 * </ul>
 */
public class UTF8Writer extends Writer {

    private static final int BUF_SIZE = 150_000;

    // félrevezető a javadoc, de úgy tűnik hoy ezek byte offseteket fogadnak,
    // nem az int/long "elemek" indexeit (tehát nem szorozzák meg 4-gyel vagy 8-cal)
    private static final VarHandle INT_VH =
            MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.LITTLE_ENDIAN);
    private static final VarHandle LONG_VH =
            MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);

    private static final ObjPool<byte[]> pool = new ObjPool<>(100, () -> new byte[BUF_SIZE]);

    /**
     * ez akkor null, ha closed
     */
    private byte[] outBuffer;
    private int outPos;
    private List<byte[]> buffers;
    private List<Integer> bufferSizes;
    private final Set<byte[]> freeableBuffers = new HashSet<>();

    private final OutputStream out;

    public UTF8Writer(OutputStream out) {
        this.out = out;
        outBuffer = pool.acquire();
        freeableBuffers.add(outBuffer);
    }

    private UTF8Writer(byte[] buffer) {
        this.out = null;
        outBuffer = buffer;
    }

    public static UTF8Writer ofBuffer() {
        UTF8Writer w = new UTF8Writer(pool.acquire());
        w.freeableBuffers.add(w.outBuffer);
        return w;
    }

    public static UTF8Writer ofBuffer(byte[] buffer) {
        return new UTF8Writer(buffer);
    }

    public static int makeIntLE(String s) {
        int c1, c2, c3, c4;
        if (s.length() != 4 ||
                (c1 = s.charAt(0)) >= 128 ||
                (c2 = s.charAt(1)) >= 128 ||
                (c3 = s.charAt(2)) >= 128 ||
                (c4 = s.charAt(3)) >= 128)
            throw new IllegalArgumentException();

        return c1 | c2 << 8 | c3 << 16 | c4 << 24;
    }

    @Override
    public void write(String str, int off, int len) {
        len += off;
        for (int i = off; i < len; i++) {
            write(str.charAt(i));
        }
    }

    @Override
    public void write(String str) {
        for (int i = 0; i < str.length(); i++) {
            write(str.charAt(i));
        }
    }

    @Override
    public void write(char[] cbuf) {
        for (char c : cbuf) {
            write(c);
        }
    }

    @Override
    public UTF8Writer append(CharSequence csq) {
        for (int i = 0; i < csq.length(); i++)
            append(csq.charAt(i));
        return this;
    }

    @Override
    public UTF8Writer append(CharSequence csq, int start, int end) {
        for (int i = start; i < end; i++)
            append(csq.charAt(i));
        return this;
    }

    @Override
    public UTF8Writer append(char c) {
        write(c);
        return this;
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        len += off;
        for (int i = off; i < len; i++) {
            write(cbuf[i]);
        }
    }

    @Override
    public void flush() throws IOException {
        if (out != null) {
            flushImpl(out);
            out.flush();
        }
    }

    private void flushImpl(OutputStream out) throws IOException {
        if (buffers != null) {
            for (int i = 0; i < buffers.size(); i++) {
                out.write(buffers.get(i), 0, bufferSizes.get(i));
                tryReleaseBuffer(buffers.get(i));
            }
            buffers.clear();
            bufferSizes.clear();
        }
        if (outPos != 0) {
            out.write(outBuffer, 0, outPos);
            outPos = 0;
        }
    }

    @Override
    public void close() throws IOException {
        if (outBuffer == null)
            return;
        flushImpl(out);
        out.close();
        outBuffer = null;
        freeableBuffers.forEach(this::releaseBuffer);
        freeableBuffers.clear();
    }

    public void flushToAndClose(OutputStream out) throws IOException {
        if (this.out != null)
            throw new UnsupportedOperationException();
        if (outBuffer == null)
            return;
        flushImpl(out);
        outBuffer = null;
        freeableBuffers.forEach(this::releaseBuffer);
        freeableBuffers.clear();
    }

    @Override
    public void write(int ch) {
        if (outBuffer == null)
            return;
        if (outPos + 4 > outBuffer.length) {
            onOverflow();
        }
        if (ch < 0x80) {
            outBuffer[outPos++] = (byte) ch;
        } else {
            writeNonASCII(ch);
        }
    }

    public void writeByte(int b) {
        if (outPos == outBuffer.length)
            onOverflow();
        outBuffer[outPos++] = (byte) b;
    }

    public void writeBytes(char c1, char c2) {
        if (outPos + 2 > outBuffer.length)
            onOverflow();
        outBuffer[outPos++] = (byte) c1;
        outBuffer[outPos++] = (byte) c2;
    }

    public void write4BytesLE(int n) {
        if (outPos + 4 > outBuffer.length)
            onOverflow();
        INT_VH.set(outPos, n);
        outPos += 4;
    }

    public void write8BytesLE(long n) {
        if (outPos + 8 > outBuffer.length)
            onOverflow();
        LONG_VH.set(outPos, n);
        outPos += 8;
    }

    public void writeBytes(byte[] bytes) {
        if (bytes.length > BUF_SIZE) {
            addBytes(Arrays.copyOf(bytes, bytes.length));
            return;
        }

        if (outPos + bytes.length >= outBuffer.length)
            onOverflow();
        arraycopy(bytes, 0, outBuffer, outPos, bytes.length);
        outPos += bytes.length;
    }

    public void writeBytes(byte[] array, int offset, int length) {
        if (length > BUF_SIZE) {
            addBytes(Arrays.copyOfRange(array, offset, length));
            return;
        }

        if (outPos + length >= outBuffer.length)
            onOverflow();
        arraycopy(array, offset, outBuffer, outPos, length);
        outPos += length;
    }

    // copied from java.lang.Integer
    static final byte[] DigitTens = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };

    // copied from java.lang.Integer
    static final byte[] DigitOnes = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    // copied from java.lang.Integer.getChars
    public void writeInt(int i) {
        // assert BUF_SIZE >= "-2147483648".length();

        int stringSize = stringSize(i);
        if (outPos + stringSize >= outBuffer.length)
            onOverflow();

        int q, r;
        int charPos = outPos + stringSize;

        boolean negative = i < 0;
        if (!negative) {
            i = -i;
        }

        // Generate two digits per iteration
        while (i <= -100) {
            q = i / 100;
            r = (q * 100) - i;
            i = q;
            outBuffer[--charPos] = DigitOnes[r];
            outBuffer[--charPos] = DigitTens[r];
        }

        // We know there are at most two digits left at this point.
        outBuffer[--charPos] = DigitOnes[-i];
        if (i < -9) {
            outBuffer[--charPos] = DigitTens[-i];
        }

        if (negative) {
            outBuffer[--charPos] = (byte) '-';
        }

        outPos += stringSize;
    }

    // copied from java.lang.Integer.stringSize

    /**
     * Returns the string representation size for a given int value.
     *
     * @param x int value
     * @return string size
     * @implNote There are other ways to compute this: e.g. binary search, but values are biased heavily towards zero,
     * and therefore linear search wins. The iteration results are also routinely inlined in the generated code after
     * loop unrolling.
     */
    static int stringSize(int x) {
        int d = 1;
        if (x >= 0) {
            d = 0;
            x = -x;
        }
        int p = -10;
        for (int i = 1; i < 10; i++) {
            if (x > p)
                return i + d;
            p = 10 * p;
        }
        return 10 + d;
    }

    private void addBytes(byte[] bytes) {
        if (buffers == null) {
            buffers = new ArrayList<>();
            bufferSizes = new ArrayList<>();
        }
        buffers.add(outBuffer);
        bufferSizes.add(outPos);
        buffers.add(bytes);
        bufferSizes.add(bytes.length);
        outBuffer = pool.acquire();
        outPos = 0;
        freeableBuffers.add(outBuffer);
    }

    private void onOverflow() {
        if (buffers == null) {
            buffers = new ArrayList<>();
            bufferSizes = new ArrayList<>();
        }
        buffers.add(outBuffer);
        bufferSizes.add(outPos);
        outBuffer = pool.acquire();
        outPos = 0;
        freeableBuffers.add(outBuffer);
    }

    private int highSurrogate;

    private void writeNonASCII(int ch) {
        if (ch < 0x800) {
            outBuffer[outPos++] = (byte) (0xc0 | (ch >> 6));
            outBuffer[outPos++] = (byte) (0x80 | (ch & 0x3f));
        } else if (Character.isHighSurrogate((char) ch)) {
            highSurrogate = ch;
        } else if (Character.isLowSurrogate((char) ch)) {
            int uc = Character.toCodePoint((char) highSurrogate, (char) ch);
            outBuffer[outPos++] = (byte) (0xf0 | ((uc >> 18)));
            outBuffer[outPos++] = (byte) (0x80 | ((uc >> 12) & 0x3f));
            outBuffer[outPos++] = (byte) (0x80 | ((uc >> 6) & 0x3f));
            outBuffer[outPos++] = (byte) (0x80 | (uc & 0x3f));
        } else {
            outBuffer[outPos++] = (byte) (0xe0 | ((ch >> 12)));
            outBuffer[outPos++] = (byte) (0x80 | ((ch >> 6) & 0x3f));
            outBuffer[outPos++] = (byte) (0x80 | (ch & 0x3f));
        }
    }

    public void reset() {
        outPos = 0;
        if (buffers != null) {
            buffers.clear();
            bufferSizes.clear();
        }
    }

    public static String makeString(Consumer<UTF8Writer> writerConsumer) {
        UTF8Writer out = UTF8Writer.ofBuffer();
        writerConsumer.accept(out);
        return out.makeString();
    }

    public String makeString() {
        if (buffers == null) {
            String s = new String(outBuffer, 0, outPos, UTF_8);
            tryReleaseBuffer(outBuffer);
            return s;
        }

        int sum = outPos;
        for (Integer bufferSize : bufferSizes) {
            sum += bufferSize;
        }
        byte[] bytes = new byte[sum];
        int pos = 0;
        for (int i = 0; i < buffers.size(); i++) {
            int bufferSize = bufferSizes.get(i);
            byte[] b = buffers.get(i);
            arraycopy(b, 0, bytes, pos, bufferSize);
            tryReleaseBuffer(b);
            pos += bufferSize;
        }
        arraycopy(outBuffer, 0, bytes, pos, outPos);
        tryReleaseBuffer(outBuffer);
        assert pos + outPos == sum;
        return new String(bytes, UTF_8);
    }

    private void tryReleaseBuffer(byte[] b) {
        if (freeableBuffers.remove(b))
            releaseBuffer(b);
    }

    private void releaseBuffer(byte[] b) {
        if (b.length >= BUF_SIZE)
            pool.release(b);
    }

    public ByteBuffer[] asByteBuffers() {
        if (buffers == null)
            return new ByteBuffer[]{ByteBuffer.wrap(outBuffer, 0, outPos)};

        ByteBuffer[] bb = new ByteBuffer[buffers.size() + 1];
        for (int i = 0; i < buffers.size(); i++) {
            bb[i] = ByteBuffer.wrap(buffers.get(i), 0, bufferSizes.get(i));
        }
        bb[buffers.size()] = ByteBuffer.wrap(outBuffer, 0, outPos);
        return bb;
    }


    /**
     * a megadott bufferrel nem szabad csinálni semmit, ha nem hívjuk meg rajta a {@link #reset()}-et
     */
    public void write(UTF8Writer other) {
        if (other.outPos == 0) {
            if (other.buffers == null || other.buffers.isEmpty())
                return;
        } else
            other.onOverflow();

        if (other.buffers.isEmpty())
            return;
        if (outPos != 0)
            onOverflow();  // TODO így most feleslegesen lesz egy buffer acquireölve

        if (buffers == null) {
            buffers = new ArrayList<>();
            bufferSizes = new ArrayList<>();
        }

        buffers.addAll(other.buffers);
        bufferSizes.addAll(other.bufferSizes);
    }

    public boolean isEmpty() {
        return outPos == 0 && (buffers == null || buffers.isEmpty());
    }
}
