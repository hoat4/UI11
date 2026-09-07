package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.platform.opengl.renderer.displaylist.DisplayList;
import ui11.renderer.Node;

public abstract class GLNode extends Node {

    public abstract void addToDisplayList(Mat4 transform, DisplayList displayList);
}
