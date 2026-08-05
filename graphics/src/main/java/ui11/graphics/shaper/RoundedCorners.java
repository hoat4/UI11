package ui11.graphics.shaper;

import org.jspecify.annotations.NonNull;
import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Length;

import java.util.Objects;

public final class RoundedCorners extends SubstitutedWidget {

    private final Length topLeftRadius;
    private final Length topRightRadius;
    private final Length bottomRightRadius;
    private final Length bottomLeftRadius;
    private final Widget content;

    @Remember private Slot2 contentSlot;

    public RoundedCorners(@NonNull Length topLeftRadius,
                          @NonNull Length topRightRadius,
                          @NonNull Length bottomRightRadius,
                          @NonNull Length bottomLeftRadius,
                          @NonNull Widget content) {
        this.topLeftRadius = Objects.requireNonNull(topLeftRadius);
        this.topRightRadius = Objects.requireNonNull(topRightRadius);
        this.bottomRightRadius = Objects.requireNonNull(bottomRightRadius);
        this.bottomLeftRadius = Objects.requireNonNull(bottomLeftRadius);
        this.content = Objects.requireNonNull(content);
    }

    public static RoundedCorners withRoundedCorners(@NonNull Length radii, @NonNull Widget content) {
        return new RoundedCorners(radii, radii, radii, radii, content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected RoundedCorners forSubstitution() {
        return new RoundedCorners(
                topLeftRadius,
                topRightRadius,
                bottomRightRadius,
                bottomLeftRadius,
                contentSlot.with(content)
        );
    }

    public @NonNull Length topLeftRadius() {
        return topLeftRadius;
    }

    public @NonNull Length topRightRadius() {
        return topRightRadius;
    }

    public @NonNull Length bottomRightRadius() {
        return bottomRightRadius;
    }

    public @NonNull Length bottomLeftRadius() {
        return bottomLeftRadius;
    }

    public @NonNull Widget content() {
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
