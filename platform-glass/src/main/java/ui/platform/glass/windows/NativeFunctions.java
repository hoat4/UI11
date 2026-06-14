package ui.platform.glass.windows;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Optional;

class NativeFunctions {

    static MethodHandle lookup(String name, ValueLayout returnType, ValueLayout... parameterTypes) {
        Optional<MemorySegment> functionAddress = SymbolLookup.loaderLookup().find(name);
        if (functionAddress.isEmpty())
            throw new RuntimeException(name + " is not available");
        return Linker.nativeLinker().downcallHandle(functionAddress.get(),
                FunctionDescriptor.of(returnType, parameterTypes));
    }
}
