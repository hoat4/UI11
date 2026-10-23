package ui11.output.html;

import ui11.Widget;
import ui11.output.html.util.UTF8Writer;
import ui11.control.EditablePlainText;
import ui11.control.TextField;
import ui11.layout.singlechild.Align;
import ui11.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static ui11.layout.multichild.LinearLayout.column;
import static ui11.geom.Length.em;

public class HtmlRendererTest {
    public static void main(String[] args) throws IOException {
        try (UTF8Writer w = new UTF8Writer(Files.newOutputStream(
                Path.of("build/htmlrenderertest.html")))) {
            /*
            Widget e = Align.center(column(
                    new TextField(new EditablePlainText()),
                    new TextField(new EditablePlainText())
            ).withGap(em(1)));
             */
            Widget e = Align.center(new Text("Hello world!"));
            HtmlUIRenderer.renderAsPage(e, w);
        }
    }
}
