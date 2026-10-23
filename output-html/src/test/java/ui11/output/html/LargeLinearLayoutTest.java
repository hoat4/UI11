package ui11.output.html;

import ui11.output.html.util.UTF8Writer;
import ui11.layout.multichild.LinearLayout;
import ui11.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ui11.layout.multichild.LinearLayout.column;

public class LargeLinearLayoutTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        Thread.sleep(5000);
        System.out.println("BEGIN");
        try (UTF8Writer w = new UTF8Writer(Files.newOutputStream(
                Path.of("build/largelinearlayout.html")))) {
            for (int j = 0; ; j++) {
                long begin = System.nanoTime();
                int rows = 1000000;
                LinearLayout ll = LinearLayout.column(column -> {
                    for (int i = 0; i < rows; i++) {
                        column.add(new Text(Integer.toString(i)));
                    }
                });
                HtmlUIRenderer.renderAsPage(ll, w);
                long end = System.nanoTime();
                System.out.println((end - begin) / rows + " ns/row");
            }
        }
    }
}
