package ui11.animation.lottie.web;

import ui11.animation.lottie.LottieAnimationData;
import org.teavm.jso.JSObject;
import org.teavm.jso.json.JSON;

import java.io.InputStream;
import java.util.Objects;

public final class LottieWebAnimationData implements LottieAnimationData {

    final JSObject obj;

    private LottieWebAnimationData(JSObject obj) {
        Objects.requireNonNull(obj);
        this.obj = obj;
    }

    public static LottieWebAnimationData of(JSObject obj) {
        return new LottieWebAnimationData(obj);
    }

    public static LottieWebAnimationData ofJSON(String s) {
        return of(JSON.parse(s));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LottieWebAnimationData that = (LottieWebAnimationData) o;
        return obj.equals(that.obj);
    }

    @Override
    public int hashCode() {
        return obj.hashCode();
    }
}
