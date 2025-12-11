package ui11.layout.protocol;

import ui11.geom.Size;
import ui11.provide.UpValue;

public interface BoxLayoutProtocol extends UpValue {

    Size preferredSize(BoxConstraints constraints);
}
