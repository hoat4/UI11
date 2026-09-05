package ui11.renderer.j2d;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// nagyon lassú volt az opponentfinder felülete (animáció kb. 20 FPS-sel
// futott TestBelote-n, D3D nélkül kb. 40-nel), nem értettem hogy miért van,
// ezért csináltam ezt az tesztosztályt. de mikor már épp kipróbáltam volna hogy ez a 256
// téglalapot hány FPS-sel rajzolja ki, addigra egyszer csak lefagyott a gépem és újraindítás után
// már a TestBelote is jó sebességgel futott (~180 FPS).

public class J2DPerf extends Frame {

    private int fpsCounter;
    private long fpsCounterLast;

    public J2DPerf() throws HeadlessException {
    }

    @Override
    public void paint(Graphics g) {
        update(g);
    }

    @Override
    public void update(Graphics g1) {
        /*
        BufferStrategy bs = getBufferStrategy();

        do {
            do {
                Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                try {
                    paintImpl(g);
                } finally {
                    g.dispose();
                }
            } while (bs.contentsRestored());
            bs.show();
        } while (bs.contentsLost());
         */
        paintImpl(g1);


        fpsCounter++;
        long now = System.currentTimeMillis();
        if (now - fpsCounterLast >= 1000) {
            fpsCounterLast = now;
            setTitle(Integer.toString(fpsCounter));
            fpsCounter = 0;
        }

        repaint();
    }

    private void paintImpl(Graphics g) {
        Insets insets = getInsets();
        int w = getWidth() - insets.left - insets.right, h = getHeight() - insets.top - insets.bottom;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                g.setColor(new Color(256 - (i + 1) * 16, j * 16, i * 16));
                g.fillRect(insets.left + i * w / 16, insets.top + j * h / 16, w / 16, h / 16);
            }
        }
    }

    public static void main(String[] args) {
        J2DPerf f = new J2DPerf();
        f.setExtendedState(Frame.MAXIMIZED_BOTH);
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                f.dispose();
            }
        });
        f.setVisible(true);
    }
}
