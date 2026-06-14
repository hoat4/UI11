package ui.platform.glass.windows;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Optional;

import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.MemorySegment.NULL;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.invoke.MethodHandles.lookup;

@SuppressWarnings("preview")
public class DirectCompositionAPI {

    private static final MethodHandle DCompositionWaitForCompositorClock;
    private static final MethodHandle DCompositionGetFrameId;

    // nem DirectComposition, de mindegy.
    // bár nem teljesen mindegy, mert ha nem fut DWM, akkor ez nem működik.
    // de most már csak WinPE-ben meg hasonlókban van az hogy nincs DWM elvileg.
    private static final MethodHandle DwmGetCompositionTimingInfo;

    private static final MethodHandle DwmFlush;

    private static final NativeStructMapper<TimingInfo> timingInfoMapper = new NativeStructMapper<>(TimingInfo.class, lookup());

    private DirectCompositionAPI() {
        throw new Error();
    }

    static {
        System.loadLibrary("dcomp"); // ez kell még?
        System.loadLibrary("dwmapi");

        DCompositionWaitForCompositorClock = NativeFunctions.lookup("DCompositionWaitForCompositorClock",
                JAVA_INT, JAVA_INT, ValueLayout.ADDRESS, JAVA_INT);
        DCompositionGetFrameId = NativeFunctions.lookup("DCompositionGetFrameId",
                JAVA_INT, JAVA_INT, ValueLayout.ADDRESS);
        DwmGetCompositionTimingInfo = NativeFunctions.lookup("DwmGetCompositionTimingInfo",
                JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS);
        DwmFlush = NativeFunctions.lookup("DwmFlush", JAVA_INT);
    }

    /**
     * @param timeout -1, ha végtelen
     */
    public static int waitForCompositorClock(int handleCount, MemorySegment handles, int timeout) {
        try {
            return (int) DCompositionWaitForCompositorClock.invokeExact(0, NULL, -1);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static int flush() {
        try {
            return (int) DwmFlush.invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static TimingInfo getCompositionTimingInfo() {
        // volt egy hwnd paraméter is, de dok szerint:
        // "Starting with Windows 8.1, this parameter must be set to NULL.
        // If this parameter is not set to NULL, DwmGetCompositionTimingInfo returns E_INVALIDARG."

        MemorySegment pTimingInfo = timingInfoMapper.allocate(Arena.ofAuto());

        int hresult;
        try {
            hresult = (int) DwmGetCompositionTimingInfo.invokeExact(NULL, pTimingInfo);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        if (hresult != 0)
            throw new RuntimeException("DwmGetCompositionTimingInfo failed: " + hresult);

        return timingInfoMapper.parse(pTimingInfo);
    }

    public static long getCompositionFrameID(CompositionFrameIDType frameIdType) {
        int frameIdTypeInt = switch (frameIdType) {
            case CREATED -> 0;
            case CONFIRMED -> 1;
            case COMPLETED -> 2;
        };

        MemorySegment frameID = Arena.ofAuto().allocate(JAVA_LONG, 0);

        int hresult;
        try {
            hresult = (int) DCompositionGetFrameId.invokeExact(frameIdTypeInt, frameID);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        if (hresult != 0)
            throw new RuntimeException("DCompositionGetFrameId failed: " + hresult);

        return frameID.get(JAVA_LONG, 0);
    }

    public enum CompositionFrameIDType {
        CREATED,
        CONFIRMED,
        COMPLETED
    }

    @NativeStructMapper.Struct(packed = true)
    public record UnsignedRatio(int uiNumerator, int uiDenominator) {
    }

    @NativeStructMapper.Struct(packed = true, prefixWithStructSize = true)
    public record TimingInfo(
            UnsignedRatio rateRefresh,
            long qpcRefreshPeriod,
            UnsignedRatio rateCompose,
            long qpcVBlank,
            long cRefresh,
            int cDXRefresh,
            long qpcCompose,
            long cFrame,
            int cDXPresent,
            long cRefreshFrame,
            long cFrameSubmitted,
            int cDXPresentSubmitted,
            long cFrameConfirmed,
            int cDXPresentConfirmed,
            long cRefreshConfirmed,
            int cDXRefreshConfirmed,
            long cFramesLate,
            int cFramesOutstanding,
            long cFrameDisplayed,
            long qpcFrameDisplayed,
            long cRefreshFrameDisplayed,
            long cFrameComplete,
            long qpcFrameComplete,
            long cFramePending,
            long qpcFramePending,
            long cFramesDisplayed,
            long cFramesComplete,
            long cFramesPending,
            long cFramesAvailable,
            long cFramesDropped,
            long cFramesMissed,
            long cRefreshNextDisplayed,
            long cRefreshNextPresented,
            long cRefreshesDisplayed,
            long cRefreshesPresented,
            long cRefreshStarted,
            long cPixelsReceived,
            long cPixelsDrawn,
            long cBuffersEmpty
    ) {
    }
}
