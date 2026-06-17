package ui11.graphics.shaper;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Length;

public final class RoundedCorners extends SubstitutedWidget {

    private final Length topLeftRadius;
    private final Length topRightRadius;
    private final Length bottomRightRadius;
    private final Length bottomLeftRadius;
    private final Widget content;

    public RoundedCorners(Length topLeftRadius, Length topRightRadius,
                          Length bottomRightRadius, Length bottomLeftRadius,
                          Widget content) {
        this.topLeftRadius = topLeftRadius;
        this.topRightRadius = topRightRadius;
        this.bottomRightRadius = bottomRightRadius;
        this.bottomLeftRadius = bottomLeftRadius;
        this.content = content;
    }

    public static RoundedCorners withRoundedCorners(Length radii, Widget content) {
        return new RoundedCorners(radii, radii, radii, radii, content);
    }

    public Length topLeftRadius() {
        return topLeftRadius;
    }

    public Length topRightRadius() {
        return topRightRadius;
    }

    public Length bottomRightRadius() {
        return bottomRightRadius;
    }

    public Length bottomLeftRadius() {
        return bottomLeftRadius;
    }

    public Widget content() {
        return content;
    }

    @Override
    public String toString() {
        return "RoundedCorners[" +
                "topLeftRadius=" + topLeftRadius + ", " +
                "topRightRadius=" + topRightRadius + ", " +
                "bottomRightRadius=" + bottomRightRadius + ", " +
                "bottomLeftRadius=" + bottomLeftRadius + ", " +
                "content=" + content + ']';
    }
}
