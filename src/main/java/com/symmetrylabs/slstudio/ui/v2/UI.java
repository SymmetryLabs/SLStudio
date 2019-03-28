package com.symmetrylabs.slstudio.ui.v2;

import java.nio.ByteBuffer;


/**
 * The Java half of "slimgui", the Symmetry Labs wrapper around Dear Imgui, an immediate-mode GUI library
 *
 * <p>
 * Dear Imgui is the industry-standard implementation of the immediate-mode GUI
 * (IMGUI) UI paradigm. IMGUI is probably not the UI style that you're used to,
 * but once you're used to it it feels pretty good: fewer memory leaks, fewer
 * logic errors, no listeners at all. For more information about IMGUI and Dear
 * Imgui, <a href="https://github.com/ocornut/imgui/blob/master/docs/README.md">the readme
 * of Dear Imgui</a> has a good introduction to immediate-mode GUIs and Dear
 * Imgui.
 *
 * <h2>IDs and labels for windows and widgets</h2>
 *
 * <p>
 * The API of ImGui is stateless, but the library itself is not. The library
 * tracks UI state for us from frame-to-frame, but to do that it needs stable,
 * unique IDs for widgets and windows that don't change between frames. Whenever
 * there's a label or ID parameter to a function, that parameter is also acting
 * as an ID for the widget or window being drawn.
 *
 * <p>
 * You might want for multiple widgets to have the same label, but just passing the
 * same label to both would cause ImGui to see them as the same widget. You can
 * disambiguate the two by appending a unique suffix to both after the string {@code "##"}.
 * That string tells ImGui that everything after it is part of the identifier but
 * not part of the label. For example, if you had two patterns you wanted to draw
 * size parameters for, you could do:
 *
 * <pre>
 *     UI.text("Pattern 1");
 *     pattern1.size = UI.floatBox("size##pattern1", pattern1.size);
 *     UI.text("Pattern 2");
 *     pattern2.size = UI.floatBox("size##pattern2", pattern2.size);
 * </pre>
 *
 * Which would draw two float boxes with the label "size". If you ever see two widgets
 * whose behavior seem to be strangely linked, it is almost certainly because their
 * IDs are colliding and you need to disambiguate them. The "##" facility also
 * lets you make widgets with empty labels, by passing in something like "##actual-id".
 * IDs are scoped to windows and sometimes to containing widgets; notably, child windows
 * and trees add new scopes. That means that you can have two buttons with the
 * ID "Add" as long as they're in different windows.
 *
 * <p>
 * There is also {@code pushId} and {@code popId}, and "###" when lets you change the
 * label of a widget without changing the ID. For more information about these, please
 * read the <a href="https://github.com/ocornut/imgui/blob/9a0e71a6ecef4402d0504e3a2c9a05ca705ed5db/imgui.cpp#L658">
 * primer on the ID stack in the ImGui codebase</a>.
 */
public class UI {
    /** This class is static because our UI is stateless! */
    private UI() {}

    /** Marks a tree node as a leaf node (a node that can be clicked but not expanded) */
    public static int TREE_FLAG_LEAF;
    /**
     * If this tree flag is set, the subtree rooted at the given tree node will default to being expanded.
     * Without this flag, the default is for all tree nodes to start collapsed.
     */
    public static int TREE_FLAG_DEFAULT_OPEN;
    /**
     * If set, the given tree node is displayed with a background showing it as selected.
     * This is most useful on collapsable headers, which are technically also tree nodes.
     */
    public static int TREE_FLAG_SELECTED;

    /**
     * Allow horizontal scrolling in a window.
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_HORIZ_SCROLL;
    /**
     * Prevent the resize handle from displaying, and prevents resizing from window edge.
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_NO_RESIZE;
    /**
     * Locks a window in place (although it can still be moved using {@link setNextWindowPosition}).
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_NO_MOVE;
    /**
     * Removes the title bar from the window.
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_NO_TITLE_BAR;
    /**
     * Prevents the window from both being a dock target and from being docked to another target.
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_NO_DOCKING;
    /**
     * Skips drawing the background of the window, which has the effect of giving it a transparent background.
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_NO_BACKGROUND;
    /**
     * If set, the window is resized to perfectly fit its contents on every frame. Should be used with {@link WINDOW_NO_RESIZE}.
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_ALWAYS_AUTO_RESIZE;
    /**
     * If set, removes all decoration from the window (borders, title bar, scroll bars, resize handle).
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_NO_DECORATION;
    /**
     * Prevents mouse wheel scrolling on a window. Does not prevent scrollbar from showing up, nor does it prevent scrolling using the scrollbar.
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_NO_SCROLL_WITH_MOUSE;
    /**
     * If set, the horizontal scrollbar is always shown, even when not needed.
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_FORCE_HORIZ_SCROLL;
    /**
     * Hides all scrollbars.
     * @see begin(String, int)
     * @see beginClosable(String, int)
     * @see beginChild(String, boolean, int)
     * @see beginPopup(String, boolean, int)
     */
    public static int WINDOW_NO_SCROLLBAR;

    /**
     * Used to set the background of standard widgets (sliders, input boxes, etc)
     * @see pushColor(int, int)
     */
    public static int COLOR_WIDGET;
    /**
     * Used to set the background of standard widgets (sliders, input boxes, etc) when being mouse-overed
     * @see pushColor(int, int)
     */
    public static int COLOR_WIDGET_HOVERED;
    /**
     * Sets the background color of collapsible header widgets when not being mouse-overed or mouse-downed
     * @see pushColor(int, int)
     */
    public static int COLOR_HEADER;
    /**
     * Sets the background color of collapsible header widgets when mouse-downed
     * @see pushColor(int, int)
     */
    public static int COLOR_HEADER_ACTIVE;
    /**
     * Sets the background color of collapsible header widgets when mouse-overed
     * @see pushColor(int, int)
     */
    public static int COLOR_HEADER_HOVERED;
    /**
     * Sets the background color of buttons when not being mouse-overed or mouse-downed
     * @see pushColor(int, int)
     */
    public static int COLOR_BUTTON;
    /**
     * Sets the background color of buttons when mouse-downed
     * @see pushColor(int, int)
     */
    public static int COLOR_BUTTON_ACTIVE;
    /**
     * Sets the background color of buttons when mouse-overed
     * @see pushColor(int, int)
     */
    public static int COLOR_BUTTON_HOVERED;
    /**
     * Sets the color of window borders
     * @see pushColor(int, int)
     */
    public static int COLOR_WINDOW_BORDER;

    /**
     * Flag specifying that the given action should always be taken.
     *
     * Some functions take a "condition" parameter that tells the function when
     * the given action should be applied. If this when flag is passed, the
     * given action is always taken.
     *
     * @see setNextTreeNodeOpen(boolean, int)
     */
    public static int COND_ALWAYS;

    /**
     * Flag specifying that the given action should be taken the first time it's requested in a session, then never again.
     *
     * Some functions take a "condition" parameter that tells the function when
     * the given action should be applied. If this when flag is passed, the
     * given action is taken once for the duration of the runtime; future
     * requests to take the action will have no effect.
     *
     * @see setNextTreeNodeOpen(boolean, int)
     */
    public static int COND_ONCE;

    /**
     * Flag specifying that the given action should be taken when a widget or window first appears.
     *
     * Some functions take a "condition" parameter that tells the function when
     * the given action should be applied. If this when flag is passed, the
     * given action is taken the first time the widget appears after being
     * hidden, or the first time the window is opened after being closed.
     * Further requests to take the action will have no effect until the widget
     * is hidden (or the window closed) and re-shown (or re-opened).
     *
     * @see setNextTreeNodeOpen(boolean, int)
     */
    public static int COND_APPEARING;

    /**
     * A class representing the state of a collapsable section that is removeable.
     */
    public static class CollapseResult {
        /** true if the collapsable section should be open (i.e., client should draw the contents of the section) */
        public boolean isOpen;
        /** true if the collapsable section should be removed (the user pressed the close button) */
        public boolean shouldRemove;
    }

    /**
     * The width, in pixels, of the current SLStudio window
     *
     * Set on every frame by SLStudioGDX for reading by UI clients that want to anchor themselves somewhere.
     */
    public static float width;
    /**
     * The height, in pixels, of the current SLStudio window
     *
     * Set on every frame by SLStudioGDX for reading by UI clients that want to anchor themselves somewhere.
     */
    public static float height;
    /**
     * The density scaling of the display; lower numbers mean higher density
     */
    public static float density = 1; // this one is also used from native code in init()

    static void setDensity(float d) {
        /* we would really prefer to not scale, so if we're close to 1 we don't scale at all */
        density = Math.abs(d - 1.f) > 0.1 ? d : 1.f;
    }

    /**
     * Initialize ImGui
     * @param windowPointer the native pointer to our GLFW window encoded as a long
     * @param useMacBehaviors when true, we use Mac style input behaviors (ctrl-click right clicks, shortcuts use Cmd instead of Ctrl, etc)
     */
    public static native boolean init(long windowPointer, boolean useMacBehaviors);

    /**
     * Begin processing the UI for a new frame.
     *
     * This absorbs mouse and keyboard input data, synthesizes it, and prepares buffers for drawing.
     */
    public static native void newFrame();

    /**
     * Draw buffered draw data to the screen.
     */
    public static native void render();

    /**
     * Deallocate all resources and drop all UI state.
     *
     * After a call to shutdown(), UI should be considered unusable.
     */
    public static native boolean shutdown();

    /**
     * Add a font using a byte buffer of TTF or OTF data.
     *
     * This compiles the font into a font texture and prepares it for display
     * through ImGui. Fonts can be viewed and changed in the style editor. If
     * this function is never called, the ImGui default font is used. If addFont
     * is called, the default UI font is the font given in the first call to
     * addFont.
     *
     * If you're creating a UI, you're probably looking for {@link FontLoader},
     * not this function.
     *
     * @param name the font name
     * @param ttfData the font data in TTF or OTF format
     * @param fontSize the font size, in pixels, that we want to compile the font at
     * @return an opaque handle that can be used to refer to the font
     */
    public static native long addFont(String name, ByteBuffer ttfData, float fontSize);

    /**
     * Temporarily change the font that widgets are drawn with.
     *
     * The given font will be used until a corresponding call to {@link popFont()}.
     * Every call to {@code pushFont} must be paired with a corresponding
     * {@code popFont} on every frame, or an assertion will be raised. The font
     * handles for default UI fonts are stored as static fields on
     * {@link FontLoader}.
     *
     * @param fontHandle the font handle as returned by {@link addFont(String, ByteBuffer, float)}
     * @see FontLoader
     */
    public static native void pushFont(long fontHandle);

    /**
     * Pop the current font off of the font stack, and go back to using whatever font was before it.
     */
    public static native void popFont();

    /**
     * Temporarily change a color in the UI style.
     *
     * This changes the UI style until a corresponding call to {@link popColor()}
     *
     * @param key the style color to change; use one of the COLOR_ constants on the UI class
     * @param color the color in LX format (0xAARRGGBB). If no alpha is given the color is assumed to be full-opaque.
     */
    public static native void pushColor(int key, int color);

    /**
     * Undo one call to {@link pushColor(int, int)}.
     *
     * This restores the colors in the UI style to whatever they before the last pushColor call.
     */
    public static void popColor() {
        popColor(1);
    }

    /**
     * Undo multiple calls to {@link pushColor(int, int)} at once.
     *
     * This restores the colors in the UI style to whatever they before the last {@code count} pushColor calls.
     *
     * @param count the number of pushColor calls to undo
     */
    public static native void popColor(int count);

    /**
     * Temporarily changes the width of any widgets drawn after this call.
     *
     * All calls to {@code pushWidth} must be eventually followed by a corresponding call to {@link popWidth}.
     *
     * @param width if 0, widgets are automatically sized. If {@code width} is greater than
     * zero, widgets are drawn at this size in pixels. If {@code width} is less than
     * zero, sizes the widget so there are {@code width} pixels between the
     * right edge of the widget and the right edge of the window.
     */
    public static native void pushWidth(float width);

    /**
     * Restores the item sizing rules that were in effect before the last call to {@link pushWidth(float)}
     */
    public static native void popWidth();

    /**
     * Sets the position of the next window.
     *
     * @param x the position of the pivot along X, in pixels
     * @param y the position of the pivot along Y, in pixels
     * @param pivotX the relative location within the window that we're
     * positioning, along X. If 0, then the X-position is relative to the left
     * edge of the window; if 1, the X-position locates the right edge, if 0.5
     * it locates the horizontal center of the window, etc.
     * @param pivotY the relative location within the window that we're
     * positioning, along Y. If 0, then the Y-position is relative to the top
     * edge of the window; if 1, the Y-position locates the bottom edge, if 0.5
     * it locates the vertical center of the window, etc.
     */
    public static native void setNextWindowPosition(float x, float y, float pivotX, float pivotY);

    /**
     * Sets the default position and size of the next window.
     *
     * This only has an effect on the frame in which the window appears for the
     * first time; future calls to this function are no-ops.
     *
     * @param x the X-position of the left edge of the window
     * @param y the Y-position of the top edge of the window
     * @param w the width of the window, in pixels
     * @param h the height of the window, in pixels
     */
    public static native void setNextWindowDefaults(float x, float y, float w, float h);

    /**
     * Sets the default size of the next window, and requests that it appear at the current mouse cursor location.
     *
     * This only has an effect on the frame in which the window appears for the
     * first time; future calls to this function are no-ops.
     *
     * @param w the width of the window, in pixels
     * @param h the height of the window, in pixels
     */
    public static native void setNextWindowDefaultToCursor(float w, float h);

    /**
     * Sets the size of the contents of the next window.
     *
     * This only has an effect on the frame in which the window appears for the
     * first time; future calls to this function are no-ops. If the content size
     * of a window exceeds the window's size, the window will scroll to allow for
     * the contents of the window to be visible.
     *
     * @param w the width of the window, in pixels
     * @param h the height of the window, in pixels
     */
    public static native void setNextWindowContentSize(float w, float h);

    /**
     * Sets the size of the next window.
     *
     * @param w the width of the window, in pixels
     * @param h the height of the window, in pixels
     */
    public static native void setNextWindowSize(float w, float h);

    /**
     * Open a new window with the default flag set
     *
     * Must be paired with a call to {@link end()} when done drawing the
     * contents of the window.
     *
     * @param label the title and ID of the window (see note on IDs in class javadoc).
     */
    public static void begin(String label) {
        begin(label, 0);
    }

    /**
     * Open a new window
     *
     * Must be paired with a call to {@link end()} when done drawing the
     * contents of the window.
     *
     * @param label the title and ID of the window (see note on IDs in class javadoc).
     * @param flags a bitset combination of the {@code WINDOW_} flags on this class
     */
    public static native void begin(String label, int flags);

    /**
     * Open a new window that can be closed.
     *
     * Must be paired with a call to {@link end()} when done drawing the
     * contents of the window.
     *
     * @param label the title and ID of the window (see note on IDs in class javadoc).
     * @return true if the window is still open, false if the user requested to close the window
     */
    public static boolean beginClosable(String label) {
        return beginClosable(label, 0);
    }

    /**
     * Open a new window that can be closed.
     *
     * Must be paired with a call to {@link end()} when done drawing the
     * contents of the window.
     *
     * @param label the title and ID of the window (see note on IDs in class javadoc).
     * @param flags a bitset combination of the {@code WINDOW_} flags on this class
     * @return true if the window is still open, false if the user requested to close the window
     */
    public static native boolean beginClosable(String label, int flags);

    /**
     * Finish drawing a window.
     */
    public static native void end();

    /**
     * Requests that the next widget be drawn on the same line as the previous widget.
     *
     * The default behavior is for widgets to each be drawn on their own line.
     */
    public static native void sameLine();

    /**
     * Starts a table with the given number of columns.
     *
     * Columns are user-resizable and default to all being equal width.
     * Must be paired with a corresponding call to {@link endTable()}.
     * Note that this wraps what Dear ImGui calls "columns", but the widget
     * is truly a table widget and the columns name is confusing, given the
     * behavior of {@code NextColumn()} (what we call {@link nextCell()}).
     *
     * @param num the number of columns
     * @param id a unique ID for the column view
     */
    public static native void beginTable(int num, String id);
    /**
     * Moves to the next cell in the table.
     *
     * Cells run left-to-right then top-to-bottom. If we're currently in the
     * last column of a row, this creates a new row.
     */
    public static native void nextCell();
    /**
     * Finish drawing the current table widget.
     */
    public static native void endTable();

    /**
     * Draw a horizontal separator.
     *
     * If a table widget is active, this draws a line below the current row
     * and moves to the next row.
     */
    public static native void separator();

    /**
     * Inserts an invisible spacer.
     *
     * @param w the width of the spacer, in pixels
     * @param h the height of the spacer, in pixels
     */
    public static native void spacing(float w, float h);

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
    public static boolean button(String t) {
        return button(t, 0, 0);
    }
    public static boolean button(String t, float w) {
        return button(t, w, 0);
    }
    public static native boolean button(String t, float w, float h);
    public static native boolean checkbox(String label, boolean v);
    public static boolean selectable(String label, boolean v) {
        return selectable(label, v, 0);
    }
    public static native boolean selectable(String label, boolean v, float height);
    public static native String inputText(String label, String text);
    public static native int colorPicker(String label, int rgb);
    public static native float[] colorPickerHSV(String label, float h, float s, float v);
    public static native float sliderFloat(String label, float v, float v0, float v1);
    public static native float vertSliderFloat(String label, float v, float v0, float v1, String valFmt, float width, float height);
    public static native int sliderInt(String label, int v, int v0, int v1);
    public static native int combo(String label, int selected, String[] options);
    public static float floatBox(String label, float v) {
        return floatBox(label, v, 1, 0, 0, null);
    }
    public static native float floatBox(String label, float v, float speed, float min, float max, String valFmt);

    /* knob widgets are always over [0, 1] */
    public static native float knobFloat(String label, float value, float normalized);
    public static native float knobModulatedFloat(
        String label, float value, float normalizedBase, float normalizedValue,
        int modulatorCount, float[] modulatorMins, float[] modulatorMaxs, int[] modulatorColors);

    public static boolean collapsibleSection(String label) {
        return collapsibleSection(label, false, 0).isOpen;
    }
    public static CollapseResult collapsibleSection(String label, boolean allowClose) {
        return collapsibleSection(label, allowClose, 0);
    }
    public static native CollapseResult collapsibleSection(String label, boolean allowClose, int flags);

    public static native void histogram(String label, float[] values, float min, float max, int size);
    public static native void plot(String label, float[] values, float min, float max, int size);
    public static native boolean colorButton(String id, float h, float s, float b);

    public static native void envelopeEditor(String label, double[] basis, double[] value, double[] shape);

    /* Images. These are package-private; code should interact with TextureManager, which
       actually handles the complexity of texture loading/unloading for you. */
    static native void image(int texId, float w, float h, float u0, float v0, float u1, float v1);
    static native boolean imageButton(int texId, float w, float h, float u0, float v0, float u1, float v1);

    /* Menus */
    public static native boolean beginMainMenuBar();
    public static native void endMainMenuBar();
    public static native boolean beginMenu(String label);
    public static native void endMenu();
    public static native boolean menuItem(String label, String shortcut, boolean selected, boolean enabled);
    public static boolean menuItem(String label) {
        return menuItem(label, null, false, true);
    }
    public static boolean menuText(String label) {
        return menuItem(label, null, false, false);
    }
    public static native boolean menuItemToggle(String label, String shortcut, boolean selected, boolean enabled);

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

    public static void setNextTreeNodeOpen(boolean isOpen) {
        setNextTreeNodeOpen(isOpen, COND_ALWAYS);
    }
    public static native void setNextTreeNodeOpen(boolean isOpen, int when);

    /* Drag and drop. Package-private; use DragDrop class for memory-safe versions of these functions */
    static boolean beginDragDropSource() {
        return beginDragDropSource(0);
    }
    static native boolean beginDragDropSource(int flags);
    static native void endDragDropSource();
    static native boolean setDragDropPayload(String type, Object data);
    static native boolean beginDragDropTarget();
    static native void endDragDropTarget();
    static native Object acceptDragDropPayload(String type, int flags);
    public static <T> T acceptDragDropPayload(String type, Class<T> cls) {
        Object res = acceptDragDropPayload(type, 0);
        if (res == null || !cls.isAssignableFrom(res.getClass())) {
            return null;
        }
        return (T) res;
    }

    /* Interaction */
    public static boolean isItemClicked() {
        return isItemClicked(0, false);
    }
    public static boolean isItemClicked(int mouseButton) {
        return isItemClicked(mouseButton, false);
    }
    public static boolean isItemClicked(boolean allowMouseHold) {
        return isItemClicked(0, allowMouseHold);
    }
    public static native boolean isItemClicked(int mouseButton, boolean allowMouseHold);

    public static boolean isItemDoubleClicked() {
        return isItemDoubleClicked(0);
    }
    public static native boolean isItemDoubleClicked(int mouseButton);
    public static native boolean isItemActive();
    public static native boolean isAltDown();
    public static native boolean isCtrlDown();
    public static native boolean isShiftDown();

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
