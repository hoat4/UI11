package ui11.color;

import ui11.geom.LerpUtil;

public interface ColorModel<C extends Color> {

    C interpolate(C a, C b, double t);
}
