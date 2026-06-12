package ui11.platform.awt.j2d;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.function.Consumer;

public class RenderingContext {

    public static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(
            new AffineTransform(), true, false);

    public Graphics2D g;
    public AffineTransform transform = new AffineTransform();
    private double opacity = 1;
    /**
     * képernyő-koordinátákban
     */
    public Area clip;

    private final BufferedImage primaryImage, secondaryImage;

    public RenderingContext(int w, int h) {
        // ARGB-re nem tud LCD textet rajzolni
        this.primaryImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        this.secondaryImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        this.g = primaryImage.createGraphics();

        initRenderingHints(g);
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, w, h);

        clip = new Area(new Rectangle(w, h));
    }

    private void initRenderingHints(Graphics2D g) {
        Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().
                getDesktopProperty("awt.font.desktophints");

        if (desktopHints != null) {
            g.setRenderingHints(desktopHints);
        } else {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public void withTransform(AffineTransform t, Runnable task) {
        AffineTransform prev = new AffineTransform(transform);
        transform.concatenate(t);
        if (Math.abs(transform.getDeterminant()) > Double.MIN_VALUE)
            task.run();
        transform.setTransform(prev);
    }

    public void withOpacity(double opacity, Runnable task) {
        double prev = this.opacity;
        Composite prevComposite = g.getComposite();
        this.opacity *= opacity;
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) opacity));
        // ez nem fog grafikailag helyes eredményt produkálni, de egyelőre jó lesz.
        // helyette másik képre kéne renderelni, de ahhoz egy stacket kéne fenntartani, viszont
        // text akkor nem LCD hanem grayscale antialiasing lesz, tehát nem tudom hogy mi legyen.
        task.run();
        g.setComposite(prevComposite);
        this.opacity = prev;
    }

    public void withClip(Shape clip, Runnable task) {
        Area oldClip = this.clip;

        Area area = new Area(clip);
        area.transform(transform);
        area.intersect(this.clip);
        this.clip = area;

        task.run();

        this.clip = oldClip;
    }

    public void drawClipped(Consumer<Graphics2D> task) {
        Graphics2D g2 = secondaryImage.createGraphics();
        initRenderingHints(g2);
        g2.drawImage(primaryImage, 0, 0, null);
        task.accept(g2);
        g2.dispose();

        Paint prevPaint = g.getPaint();
        g.setPaint(new TexturePaint(secondaryImage, clip.getBounds2D()));
        g.fill(clip);
        g.setPaint(prevPaint);
    }

    public BufferedImage finish() {
        return primaryImage;
    }
}
