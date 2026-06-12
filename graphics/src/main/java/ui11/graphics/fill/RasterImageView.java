package ui11.graphics.fill;

import ui11.resolution.SubstitutedWidget;

public final class RasterImageView extends SubstitutedWidget {

    private final RasterImage image;

    public RasterImageView(RasterImage image) {
        this.image = image;
    }

    public RasterImage image() {
        return image;
    }
}
