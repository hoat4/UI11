package ui11.renderer.j2d;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.FloatSize;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Expose;
import ui11.Widget;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.media.SVGImageView;
import ui11.renderer.layer.Item;
import ui11.task.BackgroundTask;
import ui11.task.TaskStatus;
import ui11.text.Text;
import ui11.text.TextStyle;
import ui11.window.Shell.URLResolver;

import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;

public class J2DSVGImageViewPeer extends Widget {

    private static final Logger logger = LoggerFactory.getLogger(J2DSVGImageViewPeer.class);

    private final SVGImageView svgImageView;

    @Inject(required = false) private Item.ItemRequest request;
    @Inject private Surface surface;
    @Inject private TextStyle textStyle;
    @Inject(required = false) private URLResolver urlResolver;
    @Inject private BoxLayoutResult.SizeRequest[] sizeRequests;

    @Remember private TextStyle prevTextStyle;
    @Remember private Font awtFont;

    public J2DSVGImageViewPeer(SVGImageView svgImageView) {
        this.svgImageView = svgImageView;
    }

    @Override
    protected Widget build() {
        URI uri = svgImageView.source().toURI();
        if (!uri.isAbsolute())
            if (urlResolver == null)
                throw new RuntimeException("can't resolve URL because no " + URLResolver.class.getName() + " present: " + uri);
            else
                uri = urlResolver.toAbsoluteURL(uri);

        return withID("loadTask", uri, new BackgroundTask<>(
                new SVGDocumentLoadTask(uri),
                loadStatus -> {
                    return switch (loadStatus) {
                        case TaskStatus.InProgress<SVGDocument> _ -> {
                            // TODO
                            yield new Text("Loading SVG...");
                        }
                        case TaskStatus.Failure<SVGDocument> _ -> {
                            // TODO
                            yield new Text("SVG load error");
                        }
                        case TaskStatus.Success<SVGDocument>(SVGDocument loadedDocument) -> {
                            final Widget result = displayLoadedDocument(loadedDocument);
                            yield result;
                        }
                    };
                }
        ));
    }

    private @NonNull Widget displayLoadedDocument(SVGDocument loadedDocument) {
        if (surface == null && sizeRequests.length == 0)
            throw new RuntimeException("No surface and no size requests");

        if (!textStyle.equals(prevTextStyle)) {
            awtFont = J2DTextLayoutCalculator.awtFont(textStyle);
            prevTextStyle = textStyle;
        }

        FloatSize docSize = loadedDocument.size();
        Widget result;
        if (surface == null) {
            result = null;
        } else {
            Size size = surface.size();
            result = new Expose<>(request, new SVGItem(loadedDocument, awtFont, size, surface.coordinateSpace()));
        }
        for (BoxLayoutResult.SizeRequest sizeRequest : sizeRequests) {
            // TODO constraintset figyelembe kéne venni
            BoxLayoutResult.OfChosenSize chosenSize =
                    new BoxLayoutResult.OfChosenSize(new Size(docSize.width, docSize.height));
            if (result == null)
                result = new Expose<>(sizeRequest, chosenSize);
            else
                result = new Expose<>(sizeRequest, chosenSize, result);
        }
        assert result != null;
        return result;
    }

    private static class SVGDocumentLoadTask implements Callable<SVGDocument> {

        // SVGLoader nem thread safe
        private static final ThreadLocal<SVGLoader> SVG_LOADER_THREAD_LOCAL =
                ThreadLocal.withInitial(SVGLoader::new);

        private final URI uri;

        public SVGDocumentLoadTask(URI uri) {
            this.uri = uri;
        }

        @Override
        public SVGDocument call() throws Exception {
            URL url = uri.toURL();
            SVGLoader svgLoader = SVG_LOADER_THREAD_LOCAL.get();
            return svgLoader.load(url);
        }

        @Override
        public String toString() {
            return "SVG load task for " + uri;
        }
    }
}
