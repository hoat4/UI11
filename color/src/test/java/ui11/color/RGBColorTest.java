package ui11.color;

import org.junit.Test;

public class RGBColorTest {

    @Test
    public void testCreateValid() {
        new RGBColor(0.5, 0.5, 0.5, 0.5, ColorSpace.sRGB);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNegative() {
        new RGBColor(-0.5, 0.5, 0.5, 0.5, ColorSpace.sRGB);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateTooLargeComponent() {
        new RGBColor(1.5, 0.5, 0.5, 0.5, ColorSpace.sRGB);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNaN() {
        new RGBColor(0, Double.NaN, 0.5, 0.5, ColorSpace.sRGB);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNegativeInfinity() {
        new RGBColor(0, 0.5, Double.NEGATIVE_INFINITY, 0.5, ColorSpace.sRGB);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatePositiveInfinity() {
        new RGBColor(0, 0.5, Double.POSITIVE_INFINITY, 0.5, ColorSpace.sRGB);
    }

    @Test(expected = NullPointerException.class)
    public void testColorSpaceNonNull() {
        new RGBColor(0, 0.5, 1, 1, null);
    }
}
