import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.windows.User32;
import org.lwjgl.system.windows.WNDCLASSEX;
import org.lwjgl.system.windows.WindowsLibrary;

import static org.lwjgl.system.MemoryUtil.memPutAddress;
import static org.lwjgl.system.windows.User32.*;

public class ChildWindowTest {
    public static void main(String[] args) throws InterruptedException {
        MemoryStack stack = MemoryStack.stackPush();

        WNDCLASSEX wc = WNDCLASSEX.calloc(stack)
                .cbSize(WNDCLASSEX.SIZEOF)
                .style(User32.CS_HREDRAW | User32.CS_VREDRAW)
                .hInstance(WindowsLibrary.HINSTANCE)
                .lpszClassName(stack.UTF16("vacak"));

        memPutAddress(
                wc.address() + WNDCLASSEX.LPFNWNDPROC,
                User32.Functions.DefWindowProc
        );

        long classAtom = RegisterClassEx(wc);
        if (classAtom == 0) {
            throw new RuntimeException("Failed to register WGL window class");
        }

        long hwnd2 = User32.CreateWindowEx(0, "vacak", null, WS_EX_OVERLAPPEDWINDOW,
                0, 0, 100, 100, 0, 0, 0, 0);
        User32.ShowWindow(hwnd2, SW_SHOW);

        while (true) Thread.sleep(10000);
    }
}
