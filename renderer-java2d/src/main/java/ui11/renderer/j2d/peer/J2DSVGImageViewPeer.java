package ui11.renderer.j2d.peer;

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
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.rendertree.SVGDocumentNode;
import ui11.task.BackgroundTask;
import ui11.task.TaskStatus;
import ui11.text.Text;
import ui11.text.TextStyle;
import ui11.window.Shell.URLResolver;

import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;

public class J2DSVGImageViewPeer extends Widget {

    private static final Logger logger = LoggerFactory.getLogger(J2DSVGImageViewPeer.class);

    private final SVGImageView svgImageView;

    @Inject private J2DVisualContentRequest request;
    @Inject private Surface surface;
    @Inject private TextStyle textStyle;
    @Inject(required = false) private URLResolver urlResolver;
    @Inject private BoxLayoutResult.SizeRequest[] sizeRequests;

    @Remember private SVGDocumentNode node;
    @Remember private TextStyle prevTextStyle;

    public J2DSVGImageViewPeer(SVGImageView svgImageView) {
        this.svgImageView = svgImageView;
    }

    @Override
    protected void initState() {
        node = new SVGDocumentNode();
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
        if (!textStyle.equals(prevTextStyle)) {
            node.font.set(J2DTextPeer.awtFont(textStyle));
            prevTextStyle = textStyle;
        }

        node.svgDocument.set(loadedDocument);
        Size size = surface.size();
        node.size.set(size);
        FloatSize docSize = loadedDocument.size();
        Widget result = new Expose<>(request, node);
        for (BoxLayoutResult.SizeRequest sizeRequest : sizeRequests) {
            // TODO constraintset figyelembe kéne venni
            BoxLayoutResult.OfChosenSize chosenSize =
                    new BoxLayoutResult.OfChosenSize(new Size(docSize.width, docSize.height));
            result = new Expose<>(sizeRequest, chosenSize, result);
        }
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
