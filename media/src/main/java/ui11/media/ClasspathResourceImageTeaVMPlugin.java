package ui11.media;

import org.teavm.diagnostics.Diagnostics;
import org.teavm.model.*;
import org.teavm.model.ValueType.Array;
import org.teavm.model.ValueType.Primitive;
import org.teavm.model.instructions.ClassConstantInstruction;
import org.teavm.model.instructions.InvokeInstruction;
import org.teavm.model.instructions.StringConstantInstruction;
import org.teavm.model.optimization.ConstantConditionElimination;
import org.teavm.model.optimization.GlobalValueNumbering;
import org.teavm.model.optimization.UnreachableBasicBlockEliminator;
import org.teavm.vm.spi.TeaVMHost;
import org.teavm.vm.spi.TeaVMPlugin;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ClasspathResourceImageTeaVMPlugin implements TeaVMPlugin {

    @Override
    public void install(TeaVMHost host) {
        host.add(new ClasspathResourceImageClassHolderTransformer(host.getClassLoader()));
    }

    private static class ClasspathResourceImageClassHolderTransformer implements ClassHolderTransformer {

        private static final String ERROR_MSG_PREFIX = "[" + ClasspathResourceImageTeaVMPlugin.class.getSimpleName() + "] ";

        private static final MethodDescriptor Class_getResource_DESC = MethodDescriptor.parse(
                "getResource(Ljava/lang/String;)Ljava/net/URL;");


        private final ClassLoader classLoader;

        public ClasspathResourceImageClassHolderTransformer(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @Override
        public void transformClass(ClassHolder cls, ClassHolderTransformerContext context) {
            for (MethodHolder method : cls.getMethods()) {
                if (method.getProgram() != null) {
                    transformProgram(method.getReference(), method.getProgram(), context.getDiagnostics());
                }
            }
        }

        // részben org.teavm.classlib.impl.reflection.ReflectionTransformerről mintázva
        private void transformProgram(MethodReference containingMethod, Program program, Diagnostics diagnostics) {
            boolean hasChanges = false;

            // feltesszük hogy nem piszkálunk bele más utasításokkal a fájlnév konstans változóba
            Map<Variable, Object> vars = new HashMap<>();


            for (BasicBlock block : program.getBasicBlocks()) {
                for (Instruction instruction : block) {
                    switch (instruction) {
                        case ClassConstantInstruction classConstantInstruction -> {
                            vars.put(classConstantInstruction.getReceiver(), classConstantInstruction.getConstant());
                        }
                        case StringConstantInstruction stringConstantInstruction -> {
                            vars.put(stringConstantInstruction.getReceiver(), stringConstantInstruction.getConstant());
                        }
                        case InvokeInstruction invoke -> {
                            MethodReference m = invoke.getMethod();
                            if (m.getClassName().equals("java.lang.Class") && m.getDescriptor().equals(Class_getResource_DESC)) {
                                ValueType clazz = (ValueType) vars.get(invoke.getInstance());
                                String filename = (String) vars.get(invoke.getArguments().getFirst());
                                if (clazz == null) {
                                    diagnostics.error(new CallLocation(containingMethod, invoke.getLocation()),
                                            ERROR_MSG_PREFIX + "Unknown class instance for Class.getResource");
                                }
                                if (filename == null) {
                                    diagnostics.error(new CallLocation(containingMethod, invoke.getLocation()),
                                            ERROR_MSG_PREFIX + "Unknown filename for Class.getResource");
                                }
                                if (clazz == null || filename == null)
                                    return;
                                String resourcePath = resolveResourceName(clazz, filename);
                                byte[] bytes = loadResource(resourcePath);
                                if (bytes == null) {
                                    diagnostics.error(new CallLocation(containingMethod, invoke.getLocation()),
                                            ERROR_MSG_PREFIX + "Resource \"" + resourcePath + "\" not exists");
                                    return;
                                }
                                vars.put(invoke.getReceiver(), new LoadedResource(bytes, resourcePath, invoke));
                            } else if ((m.getClassName().equals(SVGImageView.class.getName()) ||
                                    m.getClassName().equals(LottieView.class.getName())) &&
                                    m.getName().equals("fromURL") &&
                                    vars.get(invoke.getArguments().getFirst()) instanceof LoadedResource loadedResource) {
                                // lottie esetén a parzolt JSON-t ki kéne emelni egy JSObject konstansba

                                ValueType[] methodType = m.getDescriptor().getSignature();
                                if (!methodType[0].isObject(URL.class))
                                    throw new RuntimeException();
                                methodType = methodType.clone();
                                methodType[0] = ValueType.object("java.lang.String");

                                Variable v = program.createVariable();
                                StringConstantInstruction stringConstant = new StringConstantInstruction();
                                if (m.getClassName().equals(SVGImageView.class.getName())) {
                                    // SVG-ket ne alakítsuk data URI-vá, mert DOMTemplatedSVGPeer azt nem tudja kezelni,
                                    // ezért fromURI helyett fromString

                                    invoke.setMethod(new MethodReference(m.getClassName(), "fromString", methodType));
                                    byte[] bytes = loadedResource.bytes();
                                    String decoded;
                                    try {
                                        decoded = UTF_8.newDecoder().
                                                onMalformedInput(CodingErrorAction.REPORT).
                                                onMalformedInput(CodingErrorAction.REPORT).
                                                decode(ByteBuffer.wrap(bytes)).toString();
                                    } catch (CharacterCodingException e) {
                                        throw new RuntimeException(
                                                loadedResource.path() + " is not a valid UTF-8 text: " + e, e);
                                    }
                                    stringConstant.setConstant(decoded);
                                } else if (m.getClassName().equals(LottieView.class.getName())) {
                                    invoke.setMethod(new MethodReference(m.getClassName(), "fromString", methodType));
                                    stringConstant.setConstant(new String(loadedResource.bytes, UTF_8));
                                } else
                                    throw new RuntimeException("unknown fromURL call: " + m);
                                stringConstant.setReceiver(v);
                                // TODO ha getResource return valueját másra is próbálja használni a kód,
                                //      akkor most lehet hogy hibás JS-t generál.
                                //      működő kódot nem tud, mert TClass.getResource nincs implementálva,
                                //      de inkább dobjunk hibát fordításkor.
                                loadedResource.getResourceCall.replace(stringConstant);

                                Variable[] arguments = invoke.getArguments().toArray(Variable[]::new);
                                arguments[0] = v;
                                invoke.setArguments(arguments);

                                hasChanges = true;
                            }
                        }
                        default -> {
                        }
                    }
                }
            }

            if (hasChanges) {
                boolean changed;
                do {
                    changed = new GlobalValueNumbering(true).optimize(program)
                            | new ConstantConditionElimination().optimize(containingMethod.getDescriptor(), program);
                    new UnreachableBasicBlockEliminator().optimize(program);
                } while (changed);
            }
        }

        private static String resolveResourceName(ValueType base, String name) {
            // java.lang.Class.resolveName

            if (!name.startsWith("/")) {
                String baseName = packageName(base);
                if (!baseName.isEmpty()) {
                    int len = baseName.length() + 1 + name.length();
                    StringBuilder sb = new StringBuilder(len);
                    name = sb.append(baseName.replace('.', '/'))
                            .append('/')
                            .append(name)
                            .toString();
                }
            } else {
                name = name.substring(1);
            }
            return name;
        }

        private static String packageName(ValueType t) {
            // java.lang.Class.getPackageName

            while (t instanceof Array a)
                t = a.getItemType();
            if (t instanceof Primitive) {
                return "java.lang";
            } else {
                String cn = ((ValueType.Object) t).getClassName();
                int dot = cn.lastIndexOf('.');
                return (dot != -1) ? cn.substring(0, dot).intern() : "";
            }
        }

        /**
         * @return null, ha nem létezik ilyen nevű resource
         */
        private byte[] loadResource(String path) {
            try (InputStream in = classLoader.getResourceAsStream(path)) {
                if (in == null)
                    return null;
                return in.readAllBytes();
            } catch (IOException e) {
                throw new RuntimeException("can't read resource '" + path + "': " + e, e);
            }
        }

        private record LoadedResource(byte[] bytes, String path, InvokeInstruction getResourceCall) {

            String toDataURI() {
                return "data:" + mimeType() + ";base64," + Base64.getEncoder().encodeToString(bytes);
            }

            private String mimeType() {
                if (path.endsWith(".svg")) {
                    return "image/svg+xml";
                }
                if (path.endsWith(".json")) {
                    // tegyük fel, hogy lottie, mert macerás ellenőrizni
                    return "video/lottie+json";
                }
                try (ImageInputStream in = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
                    Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(in);
                    if (!imageReaders.hasNext())
                        throw new RuntimeException("no ImageReader supports " + path);
                    ImageReader imageReader = imageReaders.next();
                    try {
                        imageReader.setInput(in);
                        return Objects.requireNonNull(imageReader.getOriginatingProvider().getMIMETypes()[0]);
                    } finally {
                        imageReader.dispose();
                    }
                } catch (IOException e) {
                    throw new RuntimeException("couldn't determine image size of " + path + ": " + e, e);
                }
            }
        }
    }
}
