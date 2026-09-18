package ui11.renderer;

import org.jspecify.annotations.NonNull;

import java.lang.foreign.MemorySegment;

public interface NativeWindowSurface extends RenderableSurface {

    /**
     * Returns a memory address, i.e. a 0 length memory segment
     */
    @SuppressWarnings("Since15") // IntelliJ bug
    @NonNull MemorySegment nativeWindowHandle();
    // TODO ha már nincs érvényes, akkor mit csináljon? exceptiont dob?
}
