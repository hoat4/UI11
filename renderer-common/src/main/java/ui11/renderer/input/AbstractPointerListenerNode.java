package ui11.renderer.input;

import ui11.geom.Vec4;
import ui11.input.pointer.PointerRegion;
import ui11.observable.MutableObservable;
import ui11.renderer.Node;

public interface AbstractPointerListenerNode {

    PointerRegion listener();
}
