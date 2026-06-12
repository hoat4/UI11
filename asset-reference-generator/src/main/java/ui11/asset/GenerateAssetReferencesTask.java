package ui11.asset;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.*;
import org.jetbrains.annotations.NotNull;

import org.jspecify.annotations.NonNull;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.joining;

public abstract class GenerateAssetReferencesTask extends DefaultTask {

    private String className;
    private FileCollection inputDirs;

    private static final String NL = System.lineSeparator();

    @TaskAction
    void generateAssetReferencesClass() throws IOException {
        List<String> classNameTokens = List.of(className.split("\\."));
        List<String> packageNameTokens = classNameTokens.subList(0, classNameTokens.size() - 1);

        Path sourceFilePath = getOutput().get().file(
                        className.replace('.', '/') + ".java").
                getAsFile().toPath();
        Files.createDirectories(sourceFilePath.getParent());
        try (Writer writer = Files.newBufferedWriter(sourceFilePath)) {
            writer.write("// To regenerate the contents of this file, execute the \"" + getName() + "\" Gradle task " +
                    "in project \"" + getProject().getPath() + "\"" + NL + NL);

            if (className.contains("."))
                writer.write("package " + className.substring(0, className.lastIndexOf('.')) + ";" + NL + NL);

            writer.write("import java.util.Map;" + NL); // SVG foreignObjectek miatt
            writer.write("import ui11.Widget;" + NL);
            writer.write("import ui11.media.SVGImageView;" + NL);
            writer.write("import ui11.media.LottieView;" + NL);
            writer.write(NL);
            String classSimpleName = className.substring(className.lastIndexOf('.') + 1);
            writer.write("public class " + classSimpleName + " {" + NL + NL);
            writer.write("    private " + classSimpleName + "() {" + NL);
            writer.write("        throw new Error();" + NL);
            writer.write("    }" + NL);

            for (File inputDirF : inputDirs) {
                Path inputDir = inputDirF.toPath();
                Files.walkFileTree(inputDir, new SimpleFileVisitor<>() {

                    @Override
                    public @NonNull FileVisitResult visitFile(@NotNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
                        String fileName = file.getFileName().toString();
                        AssetType assetType = null;
                        if (fileName.endsWith(".svg")) {
                            assetType = AssetType.SVG;
                        } else if (fileName.endsWith(".json") && LottieDetector.isLikelyLottie(file) ||
                                fileName.endsWith(".lot")) {
                            assetType = AssetType.LOTTIE;
                        }
                        if (assetType != null) {
                            List<String> resourcePathTokens =
                                    StreamSupport.stream(inputDir.relativize(file).spliterator(), false).
                                            map(p -> p.getFileName().toString()).toList();
                            int commonTokens =
                                    IntStream.range(0, Math.min(packageNameTokens.size(), resourcePathTokens.size())).
                                            takeWhile(i -> packageNameTokens.get(i).equals(resourcePathTokens.get(i))).
                                            max().orElse(-1) + 1;

                            handleAsset(file, fileName, resourcePathTokens,
                                    commonTokens, writer, classSimpleName, assetType);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            writer.write("}" + NL);
        }
    }

    private static void handleAsset(@NonNull Path file, String fileName,
                                    List<String> resourcePathTokens, int commonTokens,
                                    Writer writer, String classSimpleName,
                                    AssetType assetType) throws IOException {
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));
        if (fileName.contains("."))
            throw new RuntimeException("multiple dots in filename of " + file);

        String name = makeResourceMethodName(
                resourcePathTokens.subList(commonTokens, resourcePathTokens.size()), fileName);

        String resourcePath = "/" + String.join("/", resourcePathTokens);
        // lehetne a patht relatívvá alakítani ha nem a packageen kívül van,
        // de IntelliJ-ben csak az abszolút resource pathokra működik a Ctrl+kattintás

        List<String> foreignObjectIDs = switch (assetType) {
            case SVG -> findSVGForeignObjectIDs(file);
            case LOTTIE -> Collections.emptyList();
        };

        writer.write(NL);
        // azért az API része, hogy a withereket lehessen hívogatni
        // (pl. LottieView::withPlaybackController)
        String widgetClass = switch (assetType) {
            case SVG -> "SVGImageView";
            case LOTTIE -> "LottieView";
        };
        writer.write("    public static "+widgetClass+" " + name + "(");
        int PARAMS_PER_LINE = 2;
        boolean manyForeignObjects = foreignObjectIDs.size() > PARAMS_PER_LINE;
        if (manyForeignObjects)
            writer.write(NL);
        for (int i = 0; i < foreignObjectIDs.size(); i++) {
            if (i != 0) {
                writer.write(", ");
                if (manyForeignObjects && i % PARAMS_PER_LINE == 0)
                    writer.write(NL);
            }
            if (manyForeignObjects && i % PARAMS_PER_LINE == 0)
                writer.write("            ");
            writer.write("Widget ");
            String paramName = foreignObjectIDToJavaParameterName(foreignObjectIDs.get(i));
            writer.write(paramName);
        }
        writer.write(") {" + NL);
        writer.write("        return ");
        writer.write(widgetClass);
        writer.write(".fromURL(" + classSimpleName + ".class.getResource(" + NL);
        writer.write("                \"" + resourcePath + "\"))");
        if (!foreignObjectIDs.isEmpty()) {
            // TODO ez 10-nél többre nem jó
            writer.write("." + NL);
            writer.write("            withEmbeddedWidgets(Map.of(" + NL);
            for (int i = 0; i < foreignObjectIDs.size(); i++) {
                String id = foreignObjectIDs.get(i);
                String paramName = foreignObjectIDToJavaParameterName(id);

                writer.write("                  ");
                writer.write("\"" + id + "\", " + paramName); // TODO string escape
                if (i != foreignObjectIDs.size() - 1)
                    writer.write(", ");
                writer.write(NL);
            }
            writer.write("            ))");
        }
        writer.write(";" + NL);
        writer.write("    }" + NL);
    }

    private static final Pattern FOREIGN_OBJECT_TAG_PATTERN = Pattern.compile("<foreignObject\\s+id=\"([^\"]+)\"");

    private static List<String> findSVGForeignObjectIDs(Path svgFile) throws IOException {
        List<String> foreignObjectIDs = new ArrayList<>();
        Set<String> foreignObjectIDSet = new HashSet<>();

        // egyelőre feltételezzük, hogy az id attribútum az első
        Matcher matcher = FOREIGN_OBJECT_TAG_PATTERN.matcher(Files.readString(svgFile));
        while (matcher.find()) {
            String name = matcher.group(1);
            if (foreignObjectIDSet.add(name))
                foreignObjectIDs.add(name);
        }
        return foreignObjectIDs;
    }

    private static @NonNull String makeResourceMethodName(List<String> pathRelativeToPackage, String fileName) {
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < pathRelativeToPackage.size() - 1; i++) {
            appendNameToken(pathRelativeToPackage.get(i), name);
            name.append('_');
        }
        appendNameToken(fileName, name);
        return name.toString();
    }

    private static void appendNameToken(String fileName, StringBuilder name) {
        boolean uppercase = false;
        for (int i = 0; i < fileName.length(); ) {
            int cp = fileName.codePointAt(i);
            if (cp == '-' || cp == '_' || cp == ' ')
                uppercase = true;
            else if (uppercase) {
                name.appendCodePoint(Character.toUpperCase(cp));
                uppercase = false;
            } else
                name.appendCodePoint(cp);
            i += Character.charCount(cp);
        }
    }

    private static String foreignObjectIDToJavaParameterName(String id) {
        StringBuilder sb = new StringBuilder();
        appendNameToken(id, sb);
        return sb.toString();
    }

    private enum AssetType {
        SVG, LOTTIE
    }

    @Input
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @InputFiles
    public FileCollection getInput() {
        return inputDirs;
    }

    public void setInput(FileCollection input) {
        this.inputDirs = input;
    }

    @OutputDirectory
    public abstract DirectoryProperty getOutput();
}
