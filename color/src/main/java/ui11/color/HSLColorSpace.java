package ui11.color;

import java.util.Objects;

class HSLColorSpace implements ColorSpace<HSLColor> {

    private final ColorSpace<RGBColor> rgbColorSpace;

    public HSLColorSpace(ColorSpace<RGBColor> rgbColorSpace) {
        this.rgbColorSpace = rgbColorSpace;
    }

    @Override
    public ColorModel<HSLColor> colorModel() {
        return HSLColor.COLOR_MODEL;
    }

    public ColorSpace<RGBColor> rgbColorSpace() {
        return rgbColorSpace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HSLColorSpace that = (HSLColorSpace) o;
        return Objects.equals(rgbColorSpace, that.rgbColorSpace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rgbColorSpace);
    }
}
