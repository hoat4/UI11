package ui11.platform.awt.j2d.peer;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.FloatSize;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Size;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.media.SVGImageView;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.inputtree.OpaqueInputNode;
import ui11.platform.awt.j2d.rendertree.SVGDocumentRenderNode;
import ui11.task.BackgroundTask;
import ui11.task.TaskStatus;
import ui11.text.Text;
import ui11.text.TextStyle;
import ui11.window.Shell.URLResolver;

import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Callable;

public class J2DSVGImageViewPeer extends Widget {

    private static final Logger logger = LoggerFactory.getLogger(J2DSVGImageViewPeer.class);

    private final SVGImageView svgImageView;

    @Inject private J2DSurface surface;
    @Inject private TextStyle textStyle;
    @Inject(required = false) private URLResolver urlResolver;
    @Inject private BoxLayoutResult.SizeRequest[] sizeRequests;

    @Remember private SVGDocumentRenderNode node;
    @Remember private OpaqueInputNode inputNode;
    @Remember private TextStyle prevTextStyle;

    public J2DSVGImageViewPeer(SVGImageView svgImageView) {
        this.svgImageView = svgImageView;
    }

    @Override
    protected void initState() {
        node = new SVGDocumentRenderNode();
        inputNode = new OpaqueInputNode();
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
        inputNode.shape.set(new Rectangle2D.Double(0, 0, size.width(), size.height()));
        FloatSize docSize = loadedDocument.size();
        Widget result = new J2DNodeHolder(node, inputNode);
        for (BoxLayoutResult.SizeRequest sizeRequest : sizeRequests) {
            // TODO constraintset figyelembe kéne venni
            BoxLayoutResult.OfChosenSize chosenSize =
                    new BoxLayoutResult.OfChosenSize(sizeRequest.constraints(), new Size(docSize.width, docSize.height));
            result = PeerRequest.combineResults(result, chosenSize);
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
