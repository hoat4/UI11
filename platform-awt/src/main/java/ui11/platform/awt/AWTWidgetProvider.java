package ui11.platform.awt;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.input.gesture.EnterContentListener;

public class AWTWidgetProvider implements ResolverProvider {
    @Override
    public void configure(ResolverRegistry r) {
        // TODO r.registerForContextType(J2DVisualContentRequest.class, EnterContentListener.class,
        //            AWTEnterContentListenerPeer::new);
    }
}
