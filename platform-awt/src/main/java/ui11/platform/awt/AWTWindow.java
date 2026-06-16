package ui11.platform.awt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Component;
import ui11.Slot;
import ui11.Widget;
import ui11.animation.Scheduler;
import ui11.geom.*;
import ui11.geom.Location.CoordinateSpaceRoot;
import ui11.graphics.Surface;
import ui11.color.Color;
import ui11.input.gesture.EnterContentListener.EnterContent.KeyboardEnterContentSource;
import ui11.input.keyboard.KeyCombination;
import ui11.input.keyboard.KeyCombination.Modifier;
import ui11.input.keyboard.KeySymbol.TextSymbol;
import ui11.input.pointer.Pointer.StandardMouseButton;
import ui11.input.pointer.PointerRegion.PointerListener;
import ui11.observable.InvalidationPoint;
import ui11.observable.MutableObservable;
import ui11.platform.awt.AWTEnterContentListenerPeer.AWTEnterContentListenerPeerState;
import ui11.platform.awt.j2d.*;
import ui11.platform.awt.j2d.J2DSurface.J2DSurfaceWithOwnShape;
import ui11.platform.awt.j2d.inputtree.InputNode.PickContext;
import ui11.platform.awt.j2d.inputtree.InputNode.PickContext.PickStackItem;
import ui11.platform.awt.j2d.rendertree.RenderNode.RenderTreePrinter;
import ui11.provide.Provide;
import ui11.provide.Provider;
import ui11.resolution.WidgetResolver;
import ui11.text.TextAlign;
import ui11.text.TextStyle;
import ui11.text.TextStyle.FontStyle;
import ui11.text.TextStyle.FontWeight;
import ui11.window.Shell;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class AWTWindow {

    private static final Logger logger = LoggerFactory.getLogger(AWTWindow.class);

    private final AWTDesktop desktop;
    private final Widget content;
    private final AWTWindowImpl frame;
    private final InvalidationPoint repaintInvalidationPoint = new InvalidationPoint();

    public final CoordinateSpaceRoot windowCoordinateSystemRoot = new CoordinateSpaceRoot();

    private final MutableObservable<Size> size = MutableObservable.ofNullable();
    private final J2DSurface rootSurface = new RootJ2DSurface();
    private final MutableObservable<J2DNodeHolder> rootNodeHolder = MutableObservable.ofNullable();

    private final BufferStrategy bs;

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
        size.set(new Size(frame.innerWidth(), frame.innerHeight()));
    }

    class Root extends Widget {

        @Remember private AWTScheduler scheduler;

        @Override
        protected void initState() {
            scheduler = new AWTScheduler();
        }

        @Override
        protected Widget build() {
            final TextStyle rootTextStyle = new TextStyle(
                    Color.BLACK,
                    12D, null, TextAlign.LEFT,
                    FontWeight.NORMAL, TextStyle.Wrapping.BETWEEN_WORDS, false, null, Length.zero(), FontStyle.NORMAL);
            Widget content = new Provider<>(WidgetResolver.class, J2DWidgetResolver.INSTANCE, AWTWindow.this.content);
            content = new Provider<>(Surface.class, rootSurface, content);
            content = new Provider<>(TextStyle.class, rootTextStyle, content);
            content = new Provider<>(AWTWindow.class, AWTWindow.this, content);
            content = new Provider<>(Shell.class, AWTDesktopProvider.DEFAULT_SHELL, content);
            // TODO mi legyen ha a root widget peerjét nem sikerül létrehozni?
            //      most ilyenkor végtelen loopba kezd, mert itt a Rootban még nincs olyan WidgetResolver ami
            //      a hibaüzenetet (Text widget) tudná resolvolni

            return new J2DPeerCreationRequest().executedOn(content, contentPeer->{
                rootNodeHolder.set(contentPeer);
                // TODO repaint kéne, ha rootPeer megváltozik

                if (!frame.isVisible()) // TODO onResume kéne, csak az túl korán van
                    frame.setVisible(true);

                return new Repainter();
            });
        }

        @Provide
        private AWTWindow awtWindow() {
            return AWTWindow.this;
        }

        @Provide
        private Scheduler scheduler() {
            return scheduler;
        }
    }

    class Repainter extends Component<Void> {
        @Override
        protected Void update() {
            repaintInvalidationPoint.subscribe();
            if (!frame.isVisible()) {
                logger.warn("Skip repaint because frame is invisible");
                return null;
            }
            try {
                redraw();
            } catch (Throwable e) {
                // itt VP ami supportálja Text-et, ezért nem szabad továbbdobni az exceptiont, mert
                // végtelen rekurzió lesz belőle
                logger.error("Repaint failed", e);
            }
            return null;
        }
    }

    private void redraw() {
        if (false) {
            System.out.println();
            System.out.println("Render tree: ");
            System.out.print(RenderTreePrinter.toString(rootNodeHolder.get().renderNode()));
            System.out.println("Render tree end");
            System.out.println();
        }

        RenderingContext ctx = new RenderingContext(frame.innerWidth(), frame.innerHeight());
        rootNodeHolder.get().renderNode().render(ctx);
        BufferedImage image = ctx.finish();
        // constructor inits Graphics2D renderingHints

        Graphics2D g = (Graphics2D) bs.getDrawGraphics();
        g.setTransform(new AffineTransform());
        double scaleX = frame.getGraphicsConfiguration().getDefaultTransform().getScaleX();
        double scaleY = frame.getGraphicsConfiguration().getDefaultTransform().getScaleY();

        int x = (int) Math.round(frame.getInsets().left * scaleX);
        int y = (int) Math.round(frame.getInsets().top * scaleY);
        g.drawImage(image, x, y, null);

        g.dispose();
        bs.show();
    }

    private void onMouseMove(Vec2 point) {
        AWTMouse.INSTANCE.location.set(new Location(rootSurface.coordinateSpace(), point));
    }

    private void onMousePress(Vec2 point) {
        AWTMouse.INSTANCE.location.set(new Location(rootSurface.coordinateSpace(), point));

        PickContext pickContext = new PickContext();
        rootNodeHolder.get().inputNode().pick(pickContext, point);

        List<PickStackItem> result = pickContext.result();
        if (result == null)
            logger.info("Clicked into mouse transparent region at " + point);
        else if (result.isEmpty())
            logger.info("No mouse input region found for " + point);
        else {
            for (PickStackItem item : result.reversed()) {
                currentMousePress = item.n().listener.onPointerDown(
                        AWTMouse.INSTANCE, StandardMouseButton.PRIMARY /* TODO */);
                if (currentMousePress != null)
                    break;
            }
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
            repaintInvalidationPoint.invalidate();
        }

        @Override
        public void paint(Graphics g) {
            repaintInvalidationPoint.invalidate();
        }

        int innerWidth() {
            Insets insets = getInsets();
            return (int) Math.round((getWidth() - insets.left - insets.right) * displayScale().x());
        }

        int innerHeight() {
            Insets insets = getInsets();
            return (int) Math.round((getHeight() - insets.top - insets.bottom) * displayScale().y());
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

    public class RootJ2DSurface extends J2DSurfaceWithOwnShape {

        public AWTWindow window() {
            return AWTWindow.this;
        }

        @Override
        public Size size() {
            return AWTWindow.this.size.get();
        }

        @Override
        public Shape shape() {
            Size size = size();
            return new Rectangle2D.Double(0, 0, size.width(), size.height());
        }
    }
}
