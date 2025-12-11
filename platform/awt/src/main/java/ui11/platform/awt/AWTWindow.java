package ui11.platform.awt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Component;
import ui11.Slot;
import ui11.Widget;
import ui11.geom.*;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Location.CoordinateSpaceRoot;
import ui11.graphics.Surface;
import ui11.graphics.fill.Color;
import ui11.input.gesture.EnterContentListener.EnterContent.KeyboardEnterContentSource;
import ui11.input.keyboard.KeyCombination;
import ui11.input.keyboard.KeyCombination.Modifier;
import ui11.input.keyboard.KeySymbol.TextSymbol;
import ui11.input.pointer.Pointer.StandardMouseButton;
import ui11.input.pointer.PointerRegion.PointerListener;
import ui11.observable.MutableObservable;
import ui11.platform.awt.AWTEnterContentListenerPeer.AWTEnterContentListenerPeerState;
import ui11.platform.awt.j2d.J2DPrimitive;
import ui11.platform.awt.j2d.J2DPrimitive.PickResult;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.J2DWidgetDecomposer;
import ui11.provide.Provide;
import ui11.provide.Provider;
import ui11.resolution.WidgetResolver;
import ui11.text.TextAlign;
import ui11.text.TextStyle;
import ui11.text.TextStyle.FontStyle;
import ui11.text.TextStyle.FontWeight;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class AWTWindow {

    private static final Logger logger = LoggerFactory.getLogger(AWTWindow.class);

    private final AWTDesktop desktop;
    private final Widget content;
    private final AWTWindowImpl frame;

    private final MutableObservable<Size> size = MutableObservable.ofNullable();
    private final AWTSurface rootSurface = new AWTSurface();
    double i = 0;

    private final BufferStrategy bs;
    private J2DPrimitive rootPeer;
    private boolean requestRepaintAvailable = true;

    private PointerListener currentMousePress;

    AWTEnterContentListenerPeerState enterContentListenerPeer;

    AWTWindow(AWTDesktop desktop, Widget content) {
        this.desktop = desktop;
        this.content = content;

        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.awt.erasebackgroundonresize", "false");
        frame = new AWTWindowImpl();
        frame.setSize(300, 300);
        frame.setLocationRelativeTo(null);
        frame.addNotify();
        updateSize();

        frame.createBufferStrategy(2);
        bs = frame.getBufferStrategy();
    }

    private void updateSize() {
        size.set(new Size(
                frame.getWidth() - frame.getInsets().left - frame.getInsets().right,
                frame.getHeight() - frame.getInsets().top - frame.getInsets().bottom
        ));
    }

    class Root extends Component {

        @Inject private Slot contentSlot;

        @Override
        protected void update() {
            final TextStyle rootTextStyle = new TextStyle(
                    Color.BLACK,
                    12D, null, TextAlign.LEFT,
                    FontWeight.NORMAL, false, false, null, Length.zero(), FontStyle.NORMAL);
            Widget content = new Provider<>(WidgetResolver.class, J2DWidgetDecomposer.INSTANCE, AWTWindow.this.content);
            content = new Provider<>(Surface.class, rootSurface, content);
            content = new Provider<>(TextStyle.class, rootTextStyle, content);
            content = new Provider<>(AWTWindow.class, AWTWindow.this, content);
            // TODO mi legyen ha a root widget peerjét nem sikerül létrehozni?
            rootPeer = contentSlot.instantiate(content).lookup(J2DPrimitive.class);
            // TODO repaint kéne, ha rootPeer megváltozik

            if (!frame.isVisible()) // TODO onResume kéne, csak az túl korán van
                frame.setVisible(true);
        }

        @Provide
        private AWTWindow awtWindow() {
            return AWTWindow.this;
        }
    }

    private void redraw() {
        requestRepaintAvailable = false;
        try {
            Graphics2D g = (Graphics2D) bs.getDrawGraphics();

            Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().
                    getDesktopProperty("awt.font.desktophints");

            if (desktopHints != null) {
                g.setRenderingHints(desktopHints);
            } else {
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g.setTransform(new AffineTransform());
            double scaleX = frame.getGraphicsConfiguration().getDefaultTransform().getScaleX();
            double scaleY = frame.getGraphicsConfiguration().getDefaultTransform().getScaleY();
            g.translate(frame.getInsets().left * scaleX, frame.getInsets().top * scaleY);
            rootPeer.draw(g,
                    new Rectangle(0, 0, frame.innerWidth(), frame.innerHeight()));
            g.dispose();
            bs.show();
        } finally {
            requestRepaintAvailable = true;
        }
    }

    private void onMouseMove(Vec2 point) {
        AWTMouse.INSTANCE.location.set(new Location(rootSurface.coordinateSpace(), point));
    }

    private void onMousePress(Vec2 point) {
        AWTMouse.INSTANCE.location.set(new Location(rootSurface.coordinateSpace(), point));

        PickResult r = rootPeer.findInputRegion(point);
        if (r == null)
            logger.info("No mouse input region found for " + point);
        else {
            currentMousePress = r.region().handleMousePress(r.localPoint());
            // TODO ez visszaadhat nullt
        }
    }

    private void onMouseRelease(Vec2 point) {
        AWTMouse.INSTANCE.location.set(new Location(rootSurface.coordinateSpace(), point));

        if (currentMousePress == null)
            logger.info("No active mouse release callback for " + point);
        else {
            currentMousePress.onRelease(StandardMouseButton.PRIMARY); // TODO
            currentMousePress.onFinish(); // commit volt itt
            currentMousePress = null;
        }
    }

    private class AWTSurface implements J2DSurface {

        // TODO ablakok között?
        private final CoordinateSpace coordinateSpace = new CoordinateSpace(new CoordinateSpaceRoot(), Mat4.IDENTITY);

        @Override
        public Size size() {
            return size.get();
        }

        @Override
        public CoordinateSpace coordinateSpace() {
            return coordinateSpace;
        }

        @Override
        public void requestRepaint() {
            if (requestRepaintAvailable)
                frame.repaint();
        }

        @Override
        public double devicePixelRatio() {
            return 1;
        }
    }

    private class AWTWindowImpl extends Frame {

        AWTWindowImpl() {
             /*
            frame.setUndecorated(true);
            frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
             */
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                    // TODO context.stop
                }
            });
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                /*Graphics g = frame.getBufferStrategy().getDrawGraphics();
                g.setColor(java.awt.Color.BLUE);
                g.fillRect(0, 0, 200, 100);
                frame.getBufferStrategy().show();
                 */
                    i += 0.1;
                    updateSize();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    onMousePress(mouseEventCoords(e));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    onMouseRelease(mouseEventCoords(e));
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    onMouseMove(mouseEventCoords(e));
                }
            });
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char keyChar = e.getKeyChar();
                    if (keyChar == KeyEvent.CHAR_UNDEFINED)
                        return;
                    if (enterContentListenerPeer == null)
                        Toolkit.getDefaultToolkit().beep();
                    else {
                        Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
                        if (e.isControlDown())
                            modifiers.add(Modifier.CONTROL);
                        if (e.isAltDown())
                            modifiers.add(Modifier.ALT);
                        if (e.isShiftDown())
                            modifiers.add(Modifier.SHIFT);

                        //System.out.println(e.getExtendedKeyCode()+", "+e.getKeyCode()+", "+(int)e.getKeyChar());
                        // TODO suppementary codepointok esetén kétszer fog meghívódni.
                        //      helyette össze kéne olvasztani eggyé.

                        String text = new String(new char[]{keyChar});
                        KeyCombination keyCombination = new KeyCombination(modifiers, new TextSymbol(text));
                        boolean isRepeat = false; // TODO
                        enterContentListenerPeer.handleKeyTyped(text,
                                new KeyboardEnterContentSource(keyCombination, isRepeat));
                    }
                }
            });
        }

        @Override
        public void update(Graphics g) {
            redraw();
            //System.out.println("UPDATE");
        }

        @Override
        public void paint(Graphics g) {
            redraw();
            //System.out.println("PAINT");
        }

        int innerWidth() {
            Insets insets = getInsets();
            return getWidth() - insets.left - insets.right;
        }

        int innerHeight() {
            Insets insets = getInsets();
            return getHeight() - insets.top - insets.bottom;
        }

        /*
        @Override
        public void repaint(long tm, int x, int y, int width, int height) {
            //System.out.println("REPAINT "+tm+", "+x+", "+y+", "+width+", "+height);
        }
         */

        private Vec2 mouseEventCoords(MouseEvent e) {
            Insets insets = getInsets();
            return new Vec2(e.getX() - insets.left, e.getY() - insets.top).mul(displayScale());
        }

        private Vec2 displayScale() {
            AffineTransform t = getGraphicsConfiguration().getDefaultTransform();
            return new Vec2(t.getScaleX(), t.getScaleY());
        }
    }
}
