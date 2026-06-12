package ui11.platform.dom;

import ui11.window.FileChooserProvider;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.html.HTMLInputElement;

import java.net.URI;
import java.util.function.Consumer;

public class DOMFileChooserProvider implements FileChooserProvider {

    private final JSWindowWrapper window;

    public DOMFileChooserProvider(JSWindowWrapper window) {
        this.window = window;
    }

    @Override
    public void showOpenFileDialog(String acceptedMimeTypes, Consumer<SelectedFile> selectedFileConsumer) {
        HTMLInputElement input = (HTMLInputElement) window.getWindow().getDocument().createElement("input");
        input.setType("file");
        setAccept(input, acceptedMimeTypes);
        input.addEventListener("change", evt -> {
            JSObject selectedFile = selectedFile(input);
            if (selectedFile != null)
                selectedFileConsumer.accept(new SelectedFileImpl(selectedFile));
        });
        input.click();
    }

    @JSBody(script = "return input.files.length == 0 ? null : input.files[0];", params = {"input"})
    private static native JSObject selectedFile(HTMLInputElement input);

    @JSBody(script = "input.accept = accept;", params = {"input", "accept"})
    private static native void setAccept(HTMLInputElement input, String accept);

    private static class SelectedFileImpl implements SelectedFile {
        private final JSObject jsFile;

        public SelectedFileImpl(JSObject jsFile) {
            this.jsFile = jsFile;
        }

        @Override
        public String name() {
            return nameOfFile(jsFile);
        }

        @Override
        public URI asURL() {
            // TODO revokeObjectURL
            return URI.create(makeBlobURL(jsFile));
        }

        @JSBody(script = "return file.name;", params = {"file"})
        private static native String nameOfFile(JSObject file);

        @JSBody(script = "return URL.createObjectURL(file);", params = {"file"})
        private static native String makeBlobURL(JSObject file);
    }
}
