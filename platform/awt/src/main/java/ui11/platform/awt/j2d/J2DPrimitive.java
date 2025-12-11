package ui11.platform.awt.j2d;

import ui11.geom.Vec2;
import ui11.platform.awt.j2d.J2DPointerRegionPeer.J2DPointerRegionPeerImpl;
import ui11.provide.UpValue;

import java.awt.*;

public interface J2DPrimitive extends UpValue {

    void draw(Graphics2D g, Rectangle bounds);

    PickResult findInputRegion(Vec2 p);

    record PickResult(J2DPointerRegionPeerImpl region, Vec2 localPoint) {}
}
