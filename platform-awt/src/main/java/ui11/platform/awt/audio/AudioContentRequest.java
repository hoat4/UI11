package ui11.platform.awt.audio;

import ui11.ExposeRequest;

import java.nio.FloatBuffer;
import java.util.concurrent.Semaphore;

public class AudioContentRequest extends ExposeRequest<AudioContentRequest.AudioBuffer> {

    protected AudioContentRequest() {
        super(AudioBuffer.class);
    }

    public static class AudioBuffer {

        /**
         * ring buffer
         */
        private final FloatBuffer data;

        private final Semaphore readSemaphore;
        private final Semaphore writeSemaphore;
        private final int channels;
        private final int sampleCount;

        public AudioBuffer(int channels, int sampleCount) {
            this.channels = channels;
            this.sampleCount = sampleCount;
            readSemaphore = new Semaphore(0);
            writeSemaphore = new Semaphore(sampleCount);

            data = FloatBuffer.allocate(channels * sampleCount);
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
                if (!data.hasRemaining())
                    data.rewind();
                data.put(values.get());
            }
        }
    }
}
