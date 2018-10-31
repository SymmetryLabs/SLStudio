package com.symmetrylabs.slstudio.ui;

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
    public static native String inputText(String label, String text);

    public static boolean treeNode(String label) {
        return treeNode(label, 0, label);
    }
    public static boolean treeNode(String label, int flags) {
        return treeNode(label, flags, label);
    }
    public static native boolean treeNode(String id, int flags, String label);
    public static native void treePop();

    /* Interaction */
    public static native boolean isItemClicked();

    /* IO */
    public static native boolean wantCaptureKeyboard();
    public static native boolean wantCaptureMouse();
    public static native void keyDown(int keycode);
    public static native void keyUp(int keycode);
    public static native void addInputCharacter(char c);

    static {
        System.loadLibrary("slimgui");
    }
}
