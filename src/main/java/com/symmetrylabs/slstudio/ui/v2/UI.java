package com.symmetrylabs.slstudio.ui.v2;

import java.nio.ByteBuffer;


public class UI {
    public static int TREE_FLAG_LEAF;
    public static int TREE_FLAG_DEFAULT_OPEN;
    public static int TREE_FLAG_SELECTED;

    public static int WINDOW_HORIZ_SCROLL;
    public static int WINDOW_NO_RESIZE;
    public static int WINDOW_NO_MOVE;
    public static int WINDOW_NO_TITLE_BAR;
    public static int WINDOW_NO_DOCKING;
    public static int WINDOW_NO_BACKGROUND;
    public static int WINDOW_ALWAYS_AUTO_RESIZE;

    public static int DEFAULT_WIDTH = 250;

    public static int COLOR_WIDGET;
    public static int COLOR_HEADER;
    public static int COLOR_HEADER_ACTIVE;
    public static int COLOR_HEADER_HOVERED;
    public static int COLOR_WINDOW_BORDER;

    public static class CollapseResult {
        /** true if the collapsable section should be open (i.e., client should draw the contents of the section) */
        public boolean isOpen;
        /** true if the collapsable section should be removed (the user pressed the close button) */
        public boolean shouldRemove;
    }

    /* set on every frame by SLStudioGDX for reading by UI clients that want to anchor themselves somewhere */
    public static float width;
    public static float height;
    public static float density = 1; // this one is also used from native code in init()

    static void setDensity(float d) {
        /* we would really prefer to not scale, so if we're close to 1 we don't scale at all */
        density = Math.abs(d - 1.f) > 0.1 ? d : 1.f;
    }

    public static native boolean init(long windowPointer);
    public static native void newFrame();
    public static native void render();
    public static native boolean shutdown();

    /* Styling */
    public static native void addFont(String name, ByteBuffer ttfData, float fontSize);
    public static native void pushColor(int key, int color);
    public static void popColor() {
        popColor(1);
    }
    public static native void popColor(int count);

    /* Layout */
    public static native void setNextWindowPosition(float x, float y, float pivotX, float pivotY);
    public static native void setNextWindowDefaults(float x, float y, float w, float h);
    public static native void setNextWindowDefaultToCursor(float w, float h);
    public static native void setNextWindowContentSize(float w, float h);
    public static void begin(String label) {
        begin(label, 0);
    }
    public static native void begin(String label, int flags);
    public static boolean beginClosable(String label) {
        return beginClosable(label, 0);
    }
    public static native boolean beginClosable(String label, int flags);
    public static native void end();
    public static native void sameLine();

    public static native void columnsStart(int num, String id);
    public static native void nextColumn();
    public static native void columnsEnd();

    public static native void separator();
    public static native void spacing();

    public static boolean beginChild(String id, boolean border, int flags) {
        return beginChild(id, border, flags, 0, 0);
    }
    public static native boolean beginChild(String id, boolean border, int flags, int w, int h);
    public static native void endChild();
    public static native void beginGroup();
    public static native void endGroup();

    /* Popup model windows */
    public static native void openPopup(String name);
    public static native void closePopup();
    public static boolean beginPopup(String name, boolean modal) {
        return beginPopup(name, modal, 0);
    }
    public static native boolean beginPopup(String name, boolean modal, int flags);
    public static native void endPopup();

    /* Widgets */
    public static native void text(String t);
    public static void text(String t, Object... objs) {
        text(String.format(t, objs));
    }
    public static native void labelText(String label, String value);
    public static native boolean button(String t);
    public static native boolean checkbox(String label, boolean v);
    public static native boolean selectable(String label, boolean v);
    public static native String inputText(String label, String text);
    public static native int colorPicker(String label, int color);
    public static float sliderFloat(String label, float v, float v0, float v1) {
        return sliderFloat(label, v, v0, v1, false);
    }
    public static native float sliderFloat(String label, float v, float v0, float v1, boolean vert);
    public static native int sliderInt(String label, int v, int v0, int v1);
    public static native int combo(String label, int selected, String[] options);
    public static native float floatBox(String label, float v);
    public static native float knobFloat(String label, float v, float v0, float v1);
    public static native float knobModulatedFloat(
        String label, float base, float v0, float v1, float modulatedValue,
        int modulatorCount, float[] modulatorMins, float[] modulatorMaxs, int[] modulatorColors);
    public static native CollapseResult collapsibleSection(String label, boolean allowClose);
    public static native void histogram(String label, float[] values, float min, float max, int size);
    public static native void plot(String label, float[] values, float min, float max, int size);

    /* Images. These are package-private; code should interact with TextureManager, which
       actually handles the complexity of texture loading/unloading for you. */
    static native void image(int texId, float w, float h, float u0, float v0, float u1, float v1);
    static native boolean imageButton(int texId, float w, float h, float u0, float v0, float u1, float v1);

    /* Menus */
    public static native boolean beginMainMenuBar();
    public static native void endMainMenuBar();
    public static native boolean beginMenu(String label);
    public static native void endMenu();
    public static native boolean menuItem(
        String label, String shortcut, boolean selected, boolean enabled);
    public static boolean menuItem(String label) {
        return menuItem(label, null, false, true);
    }

    /* Context menus */
    public static native boolean beginContextMenu(String id);
    public static native boolean contextMenuItem(String label);
    public static native void endContextMenu();

    /* Trees */
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
    public static native boolean isItemActive();

    /* IO */
    static native float getFrameRate();
    static native boolean wantCaptureKeyboard();
    static native boolean wantCaptureMouse();
    static native void keyDown(int keycode);
    static native void keyUp(int keycode);
    static native void addInputCharacter(char c);
    static native void scrolled(float amount);

    /* Testing */
    static native boolean showDemoWindow();
    static native boolean showMetricsWindow();
    static native boolean showStyleEditor();
    static native boolean showAboutWindow();

    static {
        System.loadLibrary("slimgui");
    }
}