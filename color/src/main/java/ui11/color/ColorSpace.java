package ui11.color;

public interface ColorSpace<C extends Color> {

    ColorSpace<RGBColor> sRGB = new ColorSpace<>() {
        @Override
        public ColorModel<RGBColor> colorModel() {
            return RGBColor.COLOR_MODEL;
        }
    };

    ColorSpace<HSLColor> sRGB_HSL = new HSLColorSpace(sRGB);

    ColorModel<C> colorModel();
}
