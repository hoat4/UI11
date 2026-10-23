package ui11.output.html;

import ui11.output.html.util.UTF8Writer;
import ui11.Widget;
import ui11.decoration.Background;
import ui11.color.Color;
import ui11.text.Text;
import com.flyordie.ui4.BackgroundLayer;
import com.flyordie.ui4.html.HTMLElementFactory;
import com.flyordie.ui4.html.HTMLLayoutElement;
import org.openjdk.jmh.annotations.*;

import static ui11.layout.multichild.LinearLayout.row;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.openjdk.jmh.annotations.Mode.AverageTime;

@Warmup(iterations = 5)
@Measurement(iterations = 5)
@Fork(1)
@OutputTimeUnit(MICROSECONDS)
@BenchmarkMode(AverageTime)
//@State(Scope.Thread)
public class HtmlRendererPerf {

    @Benchmark
    public void twoColumnRow() {
        Widget e = row(
                Background.withBackground(Color.YELLOW, new Text("A")),
                Background.withBackground(Color.GREEN, new Text("B"))
        );
        UTF8Writer.makeString(out -> HtmlUIRenderer.renderAsFragment(e, out, null));
    }

    @Benchmark
    public void twoColumnRow_ui4() {
        UTF8Writer.makeString(out -> {
            HTMLElementFactory f = HTMLElementFactory.INSTANCE;
            HTMLLayoutElement e = (HTMLLayoutElement) f.horizontally(
                    f.label("A").background(BackgroundLayer.Color.rgb(0xFFFF00)),
                    f.label("B").background(BackgroundLayer.Color.rgb(0x00FF00))
            );
            e.write(out);
        });
    }

    public static void main(String[] args) {
        for (int i = 0; ; i++) {
            new HtmlRendererPerf().twoColumnRow();
            if (i % 100 == 0)
                System.out.println(i / 100);
        }
    }
}
