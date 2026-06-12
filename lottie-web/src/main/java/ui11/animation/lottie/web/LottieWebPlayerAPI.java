package ui11.animation.lottie.web;

import org.teavm.interop.PlatformMarker;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.EventTarget;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.metaprogramming.CompileTime;
import org.teavm.metaprogramming.Meta;
import org.teavm.metaprogramming.Metaprogramming;
import org.teavm.metaprogramming.Value;

import java.io.IOException;
import java.io.InputStream;


public abstract class LottieWebPlayerAPI implements JSObject {

    public static final String RENDERER_SVG = "svg";
    public static final String RENDERER_CANVAS = "canvas";
    public static final String RENDERER_HTML = "html";

    @JSBody(
            params = {
                    "container",
                    "renderer",
                    "loop",
                    "autoplay",
                    "path"
            },
            script = "return lottie.loadAnimation({container: container, renderer: renderer, loop: loop, autoplay: autoplay, path: path})"
    )
    public static native LottieAnimation loadAnimation(
            HTMLElement container,
            String renderer,
            boolean loop,
            boolean autoplay,
            String path
    );


    @JSBody(
            params = {
                    "container",
                    "renderer",
                    "loop",
                    "autoplay",
                    "jsonData"
            },
            script = "return lottie.loadAnimation({container: container, renderer: renderer, loop: loop, autoplay: autoplay, animationData: jsonData})"
    )
    public static native LottieAnimation loadAnimation(
            HTMLElement container,
            String renderer,
            boolean loop,
            boolean autoplay,
            JSObject jsonData
    );


    public static abstract class LottieAnimation implements EventTarget {
        public final static String EVENT_COMPLETE = "complete";
        public final static String EVENT_LOOP_COMPLETE = "loopComplete";
        public final static String EVENT_DESTROY = "destroy";

        @JSBody(script = "this.play()")
        public native void play();

        @JSBody(script = "this.pause()")
        public native void pause();

        @JSBody(script = "this.stop()")
        public native void stop();

        @JSBody(script = "this.destroy()")
        public native void destroy();

        /**
         *
         * @param value
         * @param isFrame true a value frame sorszám, false ha idő (talán milliszekundum, de dokumentációban nem írják)
         */
        public native void goToAndPlay(double value, boolean isFrame);
    }

    public static void ensureLoaded() {
        // lenti statikus initializer csinálja a betöltést.
        // de TeaVM hülye, ha natív függvényt hívunk
        // akkor nem foglalkozik a statikus inicializálókkal,
        // ezért kell ez a függvény.
    }

    static {
        if (isTeaVM())
            eval(LottieWebPlayerLoader.code((Integer) null));
    }

    @JSBody(params = "s", script = "eval(s)")
    private static native void eval(String s);

    @PlatformMarker
    private static boolean isTeaVM() {
        return false;
    }

    @CompileTime
    private static class LottieWebPlayerLoader {

        @Meta
        private static native String code(Integer dummy);

        @SuppressWarnings("DataFlowIssue")
        private static void code(Value<Integer> dummy) throws IOException {
            try (InputStream in = LottieWebPlayerAPI.class.getResourceAsStream("lottie.min.js")) {
                String s = new String(in.readAllBytes());
                Metaprogramming.exit(() -> s);
            }
        }
    }
}
