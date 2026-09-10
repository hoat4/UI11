package ui11.renderer.input;

import ui11.geom.Vec4;

public abstract class InputNode {

    public abstract boolean pick(PickContext pickContext, Vec4 p);
}
