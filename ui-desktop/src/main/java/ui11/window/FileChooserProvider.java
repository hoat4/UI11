package ui11.window;

import java.net.URI;
import java.util.function.Consumer;

public interface FileChooserProvider {

    void showOpenFileDialog(String acceptedMimeTypes, Consumer<SelectedFile> selectedFileConsumer);

    interface SelectedFile {

        String name();

        URI asURL();
    }
}
