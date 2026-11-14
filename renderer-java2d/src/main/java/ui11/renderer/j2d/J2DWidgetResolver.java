package ui11.renderer.j2d;

import ui11.*;
import ui11.media.Audio;
import ui11.media.SVGImageView;
import ui11.platform.awt.audio.JavaSoundAudioPeer;
import ui11.renderer.layer.Item;

public class J2DWidgetResolver implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        // TODO itt valahogy meg kéne tudni adni, hogy csak J2D renderer esetén használható
        r.registerForContextType(Item.ItemRequest.class, SVGImageView.class, J2DSVGImageViewPeer::new);

        // TODO ez inkább platform-awt-be
        r.registerForContextType(Item.ItemRequest.class, Audio.class, JavaSoundAudioPeer::new);
    }

    @Override
    public Priority priority() {
        return Priority.NATIVE;
    }
}
