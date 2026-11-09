package ui11.platform.awt.audio;

import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;
import java.util.concurrent.Semaphore;

public class AudioBuffer {

    /**
     * ring buffer
     */
    private final FloatBuffer forWrite, forRead; // csak azért kettő hogy külön legyen a position

    private final Semaphore readSemaphore;
    private final Semaphore writeSemaphore;
    private final int channels;
    private final int sampleCount;

    private int written, read;

    public AudioBuffer(int channels, int sampleCount) {
        assert channels > 0 && sampleCount > 0;
        this.channels = channels;
        this.sampleCount = sampleCount;
        readSemaphore = new Semaphore(0);
        writeSemaphore = new Semaphore(sampleCount);

        forWrite = FloatBuffer.allocate(channels * sampleCount);
        forRead = forWrite.asReadOnlyBuffer();
    }

    public int sampleCount() {
        return sampleCount;
    }

    public int waitUntilWritePermitted() throws InterruptedException {
        writeSemaphore.acquire();
        writeSemaphore.release();
        return writeSemaphore.availablePermits();
    }

    public void write(FloatBuffer values) {
        if (values.remaining() % channels != 0)
            throw new IllegalArgumentException();

        int samples = values.remaining() / channels;
        if (!writeSemaphore.tryAcquire(samples))
            throw new IllegalArgumentException("No enough space");
        readSemaphore.release(samples);

        // erre elég lenne egy vagy két memcpy is
        while (values.hasRemaining()) {
            if (!forWrite.hasRemaining())
                forWrite.rewind();
            forWrite.put(values.get());
            //System.out.println("written: "+(++written));
        }
    }

    public int waitUntilReadPermitted() throws InterruptedException {
        readSemaphore.acquire();
        readSemaphore.release();
        return readSemaphore.availablePermits();
    }

    public float read() throws BufferUnderflowException {
        if (forRead.position() == forWrite.position())
            throw new BufferUnderflowException();
        if (!readSemaphore.tryAcquire()) // TODO ez lassú
            throw new RuntimeException();
        writeSemaphore.release();
        if (!forRead.hasRemaining())
            forRead.rewind();
        //System.out.println("read: "+(++read));
        return forRead.get();
    }
}
