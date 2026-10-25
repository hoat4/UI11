package prismtest;

import java.awt.*;
import java.lang.management.ManagementFactory;

public class AWTStartupTest {
    public static void main(String[] args) {
        Frame frame = new Frame();
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        System.out.println(ManagementFactory.getRuntimeMXBean().getUptime());
    }
}
