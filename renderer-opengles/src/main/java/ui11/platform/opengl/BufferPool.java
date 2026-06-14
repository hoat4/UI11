package ui11.platform.opengl;

import ui11.geom.Vec2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class BufferPool {

    private final NavigableMap<Integer, List<ByteBuffer>> bufLists = new TreeMap<>();

    public GrowableVertexBuffer allocate(int minSize) {
        return new GrowableVertexBuffer(allocateImpl(minSize));
    }

    private ByteBuffer allocateImpl(int minSize) {
        Map.Entry<Integer, List<ByteBuffer>> entry = bufLists.ceilingEntry(minSize);
        if (entry == null || entry.getValue().isEmpty())
            return ByteBuffer.allocateDirect(minSize).order(ByteOrder.nativeOrder());
        else
            return entry.getValue().removeLast();
    }

    private void release(ByteBuffer buf) {
        buf.clear();
        bufLists.computeIfAbsent(buf.capacity(), __ -> new ArrayList<>()).add(buf);
    }

    public class GrowableVertexBuffer {

        private ByteBuffer buf;

        private GrowableVertexBuffer(ByteBuffer buf) {
            this.buf = buf;
        }

        public void ensureRemaining(int bytes) {
            if (buf.remaining() < bytes) {
                ByteBuffer larger = allocateImpl(buf.capacity() * 2);
                buf.flip();
                larger.put(buf);
                BufferPool.this.release(buf);
                buf = larger;
            }
        }

        public void put(Vec2 v) {
            buf.putFloat((float) v.x());
            buf.putFloat((float) v.y());
        }

        public void put(int i) {
            buf.putInt(i);
        }

        public void put(ByteBuffer src) {
            buf.put(src);
        }

        public ByteOrder order() {
            return ByteOrder.nativeOrder();
        }

        public ReleaseableBuffer finish() {
            ReleaseableBuffer result = new ReleaseableBuffer(buf.flip());
            buf = null;
            return result;
        }
    }

    public class ReleaseableBuffer {

        private final ByteBuffer buf;

        public ReleaseableBuffer(ByteBuffer buf) {
            this.buf = buf;
        }

        public ByteBuffer buffer() {
            return buf.asReadOnlyBuffer().order(ByteOrder.nativeOrder());
        }

        public void release() {
            BufferPool.this.release(buf);
        }
    }
}
