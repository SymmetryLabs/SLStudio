package com.symmetrylabs.slstudio.ui.gdx;

public class UI {
    public static int TREE_FLAG_LEAF;
    public static int TREE_FLAG_DEFAULT_OPEN;
    public static int TREE_FLAG_SELECTED;

    public static native boolean init(long windowPointer);
    public static native void newFrame();
    public static native void render();
    public static native boolean shutdown();

    /* Layout */
    public static native void begin(String label);
    public static native void end();

    /* Widgets */
    public static native void text(String t);
    public static void text(String t, Object... objs) {
        text(String.format(t, objs));
    }

    public static native boolean button(String t);
    public static native boolean checkbox(String label, boolean v);
    public static native boolean selectable(String label, boolean v);

    public static native String inputText(String label, String text);

    public static native float sliderFloat(String label, float v, float v0, float v1);
    public static native int sliderInt(String label, int v, int v0, int v1);

    public static native int combo(String label, int selected, String[] options);

    public static boolean treeNode(String label) {
        return treeNode(label, 0, label);
    }
    public static boolean treeNode(String label, int flags) {
        return treeNode(label, flags, label);
    }
    public static native boolean treeNode(String id, int flags, String label);
    public static native void treePop();

    /* Interaction */
    public static boolean isItemClicked() {
        return isItemClicked(0);
    }
    public static native boolean isItemClicked(int mouseButton);

    /* IO */
    static native boolean wantCaptureKeyboard();
    static native boolean wantCaptureMouse();
    static native void keyDown(int keycode);
    static native void keyUp(int keycode);
    static native void addInputCharacter(char c);
    static native void scrolled(float amount);

    /* Testing */
    static native void showDemoWindow();

    static {
        System.loadLibrary("slimgui");
    }
}
