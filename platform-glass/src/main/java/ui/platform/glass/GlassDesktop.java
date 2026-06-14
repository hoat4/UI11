package ui.platform.glass;

import ui11.Widget;
import ui11.window.Desktop;
import com.sun.glass.ui.Application;

import java.lang.management.ManagementFactory;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class GlassDesktop implements Desktop {

    private final Future<Void> initLatch;

    private GlassDesktop(Future<Void> initLatch) {
        this.initLatch = initLatch;
    }

    public static GlassDesktop initialize() {
        CompletableFuture<Void> initCF = new CompletableFuture<>();

        Application.run(() -> {
            // első ablak megnyitása előtt kéne EGL/ES2 libeket betölteni, hogy ne az ablak megnyitás után csinálja,
            // ami észrevehetően lassú

            System.out.println("Startup in " + ManagementFactory.getRuntimeMXBean().getUptime() + " ms");

            initCF.complete(null);
        });

        return new GlassDesktop(initCF);
    }

    @Override
    public void openWindow(Widget content) {
        try {
            initLatch.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e); // TODO
        }
        Application.invokeAndWait(()->{
            new WindowImpl(content);
        });
    }
}
