/*
package ui11.layout.impl;

import ui11.Node;
import ui11.Peer;
import ui11.geom.Axis;
import ui11.geom.Rect;
import ui11.geom.Shape;
import ui11.geom.Size;
import ui11.layout.singlechild.PassiveHeight;
import ui11.layout.helper.SingleChildLayout;
import ui11.layout.protocol.Sizing;

public class DefaultPassiveHeightImpl extends Peer<PassiveHeight> {
    @Override
    protected Object build() {
        return new SingleChildLayout() {

            @Override
            public Node content() {
                return model().content();
            }

            @Override
            public Sizing sizingImpl(Sizing elementSizing) {
                if (model().aspectRatio() == -1)
                    return Sizing.ofPassiveHeight(model().aspectRatio());
                return new Sizing() {
                    @Override
                    public Size preferredSize() {
                        return new Size(0, 0);
                    }

                    @Override
                    public double preferredSize(Axis axis, double crossAxisFixedLength) {
                        return switch (axis) {
                            case HORIZONTAL -> {
                                Size elemPrefSize = elementSizing.preferredSize();
                                if (elemPrefSize.height() == 0)
                                    yield 0; // nem tudom, mit lehetne értelmesen
                                double aspectRatio = elemPrefSize.width() / elemPrefSize.height();
                                yield crossAxisFixedLength * aspectRatio;
                            }
                            case VERTICAL -> 0;
                        };
                    }
                };
            }

            @Override
            public Shape layout(Sizing elementSizing, Size size) {
                return Rect.of(size);
            }
        };
    }
}
 */