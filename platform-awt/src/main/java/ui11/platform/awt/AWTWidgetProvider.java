package ui11.platform.awt;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.input.gesture.EnterContentListener;
import ui11.renderer.j2d.J2DSurface;

public class AWTWidgetProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        r.registerForContextType(J2DSurface.class, EnterContentListener.class, // TODO ez nem is J2DSurface
                AWTEnterContentListenerPeer::new);
    }
}
