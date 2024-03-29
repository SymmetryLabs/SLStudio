package com.symmetrylabs.slstudio.ui.v2;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;


/**
 * The Java half of "slimgui", the Symmetry Labs wrapper around Dear Imgui, an immediate-mode GUI library
 *
 * <h1>About immediate-mode UIs</h1>
 *
 * <p>
 * <a href="https://github.com/ocornut/imgui">Dear Imgui</a> is the
 * industry-standard implementation of the immediate-mode GUI (IMGUI) UI
 * paradigm. IMGUI is probably not the UI style that you're used to, but once
 * you're used to it it feels pretty good: fewer memory leaks, fewer logic
 * errors, no listeners at all. For more information about IMGUI and Dear
 * Imgui, <a href="https://github.com/ocornut/imgui/blob/master/docs/README.md">the readme
 * of Dear Imgui</a> has a good introduction to immediate-mode GUIs and Dear
 * Imgui.
 *
 * <h1>IDs and labels for windows and widgets</h1>
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
@SuppressWarnings({"WeakerAccess", "JavadocReference"})
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
     * If set, the given text input is read-only. Read-only text inputs are
     * useful because they allow for copy-paste.
     */
    public static int INPUT_TEXT_FLAG_READ_ONLY;

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
     * Sets the color of lines in plot widgets
     * @see pushColor(int, int)
     */
    public static int COLOR_PLOT_LINES;

    /**
     * Makes {@link isWindowFocused(int)} return true if a child window of this window is focused.
     */
    public static int FOCUSED_FLAG_CHILD_WINDOWS;

    /**
     * Flag specifying that the given action should always be taken.
     *
     * <p>
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
     * <p>
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
     * <p>
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

//    public void runAfterDraw(Runnable r) {
//        synchronized (runPostDraw) {
//            runPostDraw.add(r);
//        }
//    }

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
     * A class to represent screen-space positions and sizes (width and height) in fractional pixels.
     */
    public static class ScreenVec2 {
        public float x, y;
    }

    /**
     * The width, in pixels, of the current SLStudio window
     *
     * <p>
     * Set on every frame by VolumeApplication for reading by UI clients that want to anchor themselves somewhere.
     */
    public static float width;
    /**
     * The height, in pixels, of the current SLStudio window
     *
     * <p>
     * Set on every frame by VolumeApplication for reading by UI clients that want to anchor themselves somewhere.
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
     * <p>
     * This absorbs mouse and keyboard input data, synthesizes it, and prepares buffers for drawing.
     */
    public static native void newFrame();

    /**
     * Draw buffered draw data to the screen.
     */
    public static native void render();

    /**
     * Deallocate all resources and drop all UI state.
     * After a call to shutdown(), UI should be considered unusable.
     */
    public static native boolean shutdown();

    /**
     * Add a font using a byte buffer of TTF or OTF data.
     *
     * <p>
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
     * <p>
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
     * <p>
     * This changes the UI style until a corresponding call to {@link popColor()}
     *
     * @param key the style color to change; use one of the COLOR_ constants on the UI class
     * @param color the color in LX format (0xAARRGGBB). If no alpha is given the color is assumed to be full-opaque.
     */
    public static native void pushColor(int key, int color);

    /**
     * Undo one call to {@link pushColor(int, int)}.
     *
     * <p>
     * This restores the colors in the UI style to whatever they before the last pushColor call.
     */
    public static void popColor() {
        popColor(1);
    }

    /**
     * Undo multiple calls to {@link pushColor(int, int)} at once.
     *
     * <p>
     * This restores the colors in the UI style to whatever they before the last {@code count} pushColor calls.
     *
     * @param count the number of pushColor calls to undo
     */
    public static native void popColor(int count);

    /**
     * Temporarily changes the width of any widgets drawn after this call.
     *
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
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
     * Returns true if the current window appeared in this frame.
     */
    public static native boolean isWindowAppearing();

    /**
     * Returns true if focus is on the current window.
     */
    public static native boolean isWindowFocused(int flags);

    /**
     * Get the location of the draw cursor.
     *
     * @return the location, in window-relative pixel coordinates, at which the next widget will be drawn.
     */
    public static native ScreenVec2 getCursorPosition();

    /**
     * Open a new window with the default flag set
     *
     * <p>
     * Must be paired with a call to {@link end()} when done drawing the
     * contents of the window.
     *
     * @param label the title and ID of the window (see note on IDs in class javadoc).
     */
    public static void begin(String label) {
        begin(label, 0);
    }

    /**
     * Open a new window.
     *
     * <p>
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
     * <p>
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
     * <p>
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
     * <p>
     * The default behavior is for widgets to each be drawn on their own line.
     */
    public static native void sameLine();

    /**
     * Starts a table with the given number of columns.
     *
     * <p>
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
     * <p>
     * Cells run left-to-right then top-to-bottom. If we're currently in the
     * last column of a row, this creates a new row.
     */
    public static native void nextCell();
    /**
     * Finish drawing the current table widget.
     */
    public static native void endTable();
    /**
     * Set the width of the given column, in pixels.
     */
    public static native void setColumnWidth(int column, float width);

    /**
     * Draw a horizontal separator.
     *
     * <p>
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

    /**
     * Start drawing into a child window that uses all of the remaining size left in the current window.
     *
     * <p>
     * Child windows are a good way to get a smaller scrollable area within a
     * window, or to draw a border around a set of widgets.
     * Every call to {@code beginChild} must be matched with a corresponding
     * call to {@link endChild()}.
     *
     * @param id the ID of the child window. Child windows have no labels, this is just an ID.
     * @param border if true, a border is drawn around the child window
     * @param flags the set of {@code WINDOW_} flags to apply to the child window
     * @return true if the child window is visible. If false is returned, you
     * can still draw into it, but you could choose to skip drawing the child
     * window's content to save some time.
     */
    public static boolean beginChild(String id, boolean border, int flags) {
        return beginChild(id, border, flags, 0, 0);
    }

    /**
     * Start drawing into a child window with a specified size.
     *
     * <p>
     * Child windows are a good way to get a smaller scrollable area within a
     * window, or to draw a border around a set of widgets.
     * Every call to {@code beginChild} must be matched with a corresponding call to {@link endChild()}.
     *
     * @param id the ID of the child window. Child windows have no labels, this is just an ID.
     * @param border if true, a border is drawn around the child window
     * @param flags the set of {@code WINDOW_} flags to apply to the child window
     * @param w the width of the child window. If 0 is passed, the child takes
     * up all remaining width in its parent. If greater than zero, the child
     * exactly the width given, in pixels. If less than zero, uses the remaining
     * size of the window minus {@code w} pixels.
     * @param h the height of the child window. If 0 is passed, the child takes
     * up all remaining space in the content-size of its parent. If greater than
     * zero, the child exactly the height given, in pixels. If less than zero,
     * uses the remaining height of the window's content-size minus {@code h} pixels.
     * @return true if the child window is visible. If false is returned, you
     * can still draw into it, but you could choose to skip drawing the child
     * window's content to save some time.
     */
    public static native boolean beginChild(String id, boolean border, int flags, int w, int h);

    /**
     * Finish drawing into a child window.
     */
    public static native void endChild();

    /**
     * Begins a new group of widgets.
     *
     * <p>
     * Groups are widgets that are laid out into a rectangle as a unit, which can then be
     * laid out at a higher level with other widgets. Mostly they are useful for making stacks
     * of widgets laid out next to one another, sort of like float-left in CSS; you can do things
     * like
     *
     * <pre>
     *     UI.beginGroup();
     *     UI.text("first");
     *     UI.text("--second--");
     *     UI.endGroup();
     *     UI.sameLine();
     *     UI.beginGroup();
     *     UI.text("third");
     *     UI.text("fourth");
     *     UI.endGroup();
     * </pre>
     *
     * to get something that looks like
     *
     * <pre>
     *     first       third
     *     --second--  fourth
     * </pre>
     *
     * <p>
     * Calls to {@code beginGroup} must be paired with a corresponding call to {@link endGroup()}.
     */
    public static native void beginGroup();
    /** Closes out a group. */
    public static native void endGroup();

    /**
     * Starts drawing a popup window.
     *
     * <p>
     * Popups are unlike normal windows in that you call {@code beginPopup} on each frame,
     * even when you don't intend to display it. ImGui handles the visibility tracking of
     * the window instead. When you want to show the popup, call {@link openPopup(String)}
     * with the corresponding popup ID, and if you want to close it use {@link closePopup()},
     * which will close whatever popup is currently open.
     *
     * <p>
     * This function returns true when the popup is visible, and false otherwise. You should
     * only draw the contents of the popup if this returns true. You also must call {@link endPopup()}
     * when you're done drawing the contents of the popup, but you should only call {@link endPopup()}
     * if this returns true. So the correct usage of this is:
     *
     * <pre>
     *     if (UI.beginPopup("mypopup", false)) {
     *         UI.button("do it");
     *         UI.endPopup();
     *     }
     *     if (UI.button("show my popup")) {
     *         UI.openPopup("mypopup");
     *     }
     * </pre>
     *
     * @param id the ID of this popup
     * @param modal if true, the popup cannot be dismissed by clicking outside
     * of it, and will stay visible until a call to {@link closePopup()}.
     * @return whether the popup is currently visible
     */
    public static boolean beginPopup(String id, boolean modal) {
        return beginPopup(id, modal, 0);
    }

    /**
     * Starts drawing a popup window.
     *
     * <p>
     * Popups are unlike normal windows in that you call {@code beginPopup} on each frame,
     * even when you don't intend to display it. ImGui handles the visibility tracking of
     * the window instead. When you want to show the popup, call {@link openPopup(String)}
     * with the corresponding popup ID, and if you want to close it use {@link closePopup()},
     * which will close whatever popup is currently open.
     *
     * <p>
     * This function returns true when the popup is visible, and false otherwise. You should
     * only draw the contents of the popup if this returns true. You also must call {@link endPopup()}
     * when you're done drawing the contents of the popup, but you should only call {@link endPopup()}
     * if this returns true. So the correct usage of this is:
     *
     * <pre>
     *     if (UI.beginPopup("mypopup", false)) {
     *         UI.button("do it");
     *         UI.endPopup();
     *     }
     *     if (UI.button("show my popup")) {
     *         UI.openPopup("mypopup");
     *     }
     * </pre>
     *
     * @param id the ID of this popup
     * @param modal if true, the popup cannot be dismissed by clicking outside
     * of it, and will stay visible until a call to {@link closePopup()}.
     * @param flags a bitset of the {@code WINDOW_} flags
     * @return whether the popup is currently visible
     */
    public static native boolean beginPopup(String id, boolean modal, int flags);

    /**
     * Finishes drawing a popup.
     * @see beginPopup(String, boolean, int)
     */
    public static native void endPopup();

    /**
     * Requests that the popup with the given ID is displayed. Does nothing if popup is already visible.
     * @see beginPopup(String, boolean, int)
     */
    public static native void openPopup(String id);

    /**
     * Requests that the popup with the given ID be hidden. Does nothing is popup is already hidden.
     * @see beginPopup(String, boolean, int)
     */
    public static native void closePopup();

    /**
     * Starts drawing a tooltip for the widget that was just drawn.
     */
    public static native void beginTooltip();

    /**
     * Finishes drawing a tooltip.
     */
    public static native void endTooltip();

    /* Widgets */
    /**
     * Draws a single line of text.
     *
     * @param t the text to draw
     */
    public static native void text(String t);

    /**
     * Draws and formats a single line of text
     *
     * @param t a format pattern
     * @param objs the objects to substitute in to the provided format pattern
     */
    public static void text(String t, Object... objs) {
        text(String.format(t, objs));
    }

    /**
     * Draws plain text word-wrapped to the size of its containing window.
     *
     * @param t the text to draw
     */
    public static native void textWrapped(String t);

    /**
     * Draws static text with a label on the right.
     *
     * This mimics the layout of an input widget, but without making the text editable
     *
     * @param label the label to draw
     * @param value the text that will be drawn
     */
    public static native void labelText(String label, String value);

    /**
     * Draw a button
     *
     * @param t the ID of the button (and the text on the button)
     * @return true if the button is pressed
     */
    public static boolean button(String t) {
        return button(t, 0, 0);
    }

    /**
     * Draw a button
     *
     * @param t the ID of the button (and the text on the button)
     * @param w the width of the button, in pixels. If 0, the button is sized to fit the label. If -1, the button takes up the remainder of the space on the current line.
     * @return true if the button is pressed
     */
    public static boolean button(String t, float w) {
        return button(t, w, 0);
    }

    /**
     * Draw a button
     *
     * @param t the ID of the button (and the text on the button)
     * @param w the width of the button, in pixels. If 0, the button is sized to fit the label. If -1, the button takes up the remainder of the space on the current line.
     * @param h the height of the button, in pixels. If 0, the button is sized to fit the label
     * @return true if the button is pressed
     */
    public static native boolean button(String t, float w, float h);

    /**
     * Draw a checkbox
     *
     * @param label the ID of (and label on) the checkbox
     * @param v the current value of the checkbox
     * @return the new value of the checkbox
     */
    public static native boolean checkbox(String label, boolean v);

    /**
     * Draws a text widget that can be toggled on and off with a single-click
     *
     * @param label the ID of (and label on) the widget
     * @param v true if the widget is currently selected
     * @return true if the widget is selected after processing current mouse input
     */
    public static boolean selectable(String label, boolean v) {
        return selectable(label, v, 0);
    }

    /**
     * Draws a text widget that can be toggled on and off with a single-click
     *
     * @param label the ID of (and label on) the widget
     * @param v true if the widget is currently selected
     * @param height the height of the widget. If zero, the widget is sized to fit its label
     * @return true if the widget is selected after processing current mouse input
     */
    public static native boolean selectable(String label, boolean v, float height);

    /**
     * Draws a text input field widget
     *
     * @param label the label to draw next to the field
     * @param text the current contents of the input field
     * @return the new contents of the input field
     */
    public static native String inputText(String label, String text);

    /**
     * Draws a multi-line text input area widget
     *
     * @param label the label to draw next to the field
     * @param text the current contents of the input field
     * @param displayLines the height of the widget, in number of lines that should be visible
     * @param flags a bitset of the INPUT_TEXT flags
     * @return the new contents of the input field
     */
    public static native String inputTextMultiline(String label, String text, int displayLines, int flags);

    /**
     * Shows three text fields for editing a triple of floats.
     *
     * @param label the label on the widget
     * @param v the value to display (mutated when the UI input is edited)
     * @param flags a bitset of INPUT_TEXT flags
     * @return true if the array is changed
     */
    public static native boolean inputFloat3(String label, float[] v, int flags);

    /**
     * Draws a color widget with a Photoshop-style editor popup.
     *
     * @param label the ID of (and label on) the widget
     * @param rgb the color to draw on the widget, in 8-bit ARGB format
     * @return the new color, in 8-bit ARGB format
     */
    public static native int colorPicker(String label, int rgb);

    /**
     * Draws a color widget with a Photoshop-style editor popup.
     *
     * This should be preferred whenever you have HSV values, because it will preserve the
     * hue and saturation when saturation or value are at singular values; if you bring value
     * to zero then back up to one, this function will maintain your hue value through it, which
     * won't be the case if you convert your colors to RGB and use {@link colorPicker(String, int)}
     *
     * @param label the ID of (and label on) the widget
     * @param h the hue of the color, between 0 and 360
     * @param s the saturation of the color, between 0 and 100
     * @param v the value of the color, between 0 and 100
     * @return a 3-element float array containing hue, saturation, and value
     */
    public static native float[] colorPickerHSV(String label, float h, float s, float v);

    /**
     * Draws a horizontal slider widget
     *
     * @param label the ID of (and label on) the widget
     * @param v the current value of the slider
     * @param v0 the leftmost value of the slider
     * @param v1 the rightmost value of the slider
     * @return the new value of the slider
     */
    public static native float sliderFloat(String label, float v, float v0, float v1);

    /**
     * Draws a vertical slider widget
     *
     * @param label the ID of (and label on) the widget
     * @param v the current value of the slider
     * @param v0 the bottommost value of the slider
     * @param v1 the topmost value of the slider
     * @param valFmt a String.format-style format string used to display the value of the slider; should contain one %f flag with optional modifiers
     * @param width the width of the drawn widget, in pixels. If -1, uses the remainder of the space on the current line
     * @param height the height of the drawn widget, in pixels
     * @return the new value of the slider
     */
    public static native float vertSliderFloat(String label, float v, float v0, float v1, String valFmt, float width, float height);

    /**
     * Draws a horizontal slider widget with discrete integer values
     *
     * @param label the ID of (and label on) the widget
     * @param v the current value of the slider
     * @param v0 the leftmost value of the slider
     * @param v1 the rightmost value of the slider
     * @return the new value of ths slider
     */
    public static native int sliderInt(String label, int v, int v0, int v1);

    /**
     * Draws a combo-box widget for choosing between a list of options.
     *
     * @param label the ID of (and label on) the widget
     * @param selected the index of the currently-selected item. If this is not in {@code [0, options.length)}, the widget will be drawn with no item selected
     * @param options the list of options shown in the combo box
     * @return the index of the selected item.
     */
    public static native int combo(String label, int selected, String[] options);

    /**
     * Draws an input widget for entering unbounded floating-point values.
     *
     * This widget supports two kinds of modification: textual keyboard input and dragging
     *
     * @param label the ID of (and label on) the widget
     * @param v the current value of the widget
     * @return the new value of the widget
     */
    public static float floatBox(String label, float v) {
        return floatBox(label, v, 1, 0, 0, null);
    }

    /**
     * Draws an input widget for entering bounded floating-point values.
     *
     * This widget supports two kinds of modification: textual keyboard input and dragging
     *
     * @param label the ID of (and label on) the widget
     * @param v the current value of the widget
     * @param speed the rate at which the value changes when dragged
     * @param min the minimum value of the widget's value. If both this and max are zero, the value is unbounded.
     * @param max the maximum value of the widget's value. If both this and min are zero, the value is unbounded.
     * @param valFmt a format string to control how the value is displayed in the widget box. If null, a default format string is used.
     * @return the new value of the widget
     */
    public static native float floatBox(String label, float v, float speed, float min, float max, @Nullable String valFmt);

    /**
     * Draws an input widget for entering unbounded integer values.
     *
     * This widget supports two kinds of modification: textual keyboard input and dragging
     *
     * @param label the ID of (and label on) the widget
     * @param v the current value of the widget
     * @return the new value of the widget
     */
    public static int intBox(String label, int v) {
        return intBox(label, v, 1, 0, 0, null);
    }

    /**
     * Draws an input widget for entering bounded integer values.
     *
     * This widget supports two kinds of modification: textual keyboard input and dragging
     *
     * @param label the ID of (and label on) the widget
     * @param v the current value of the widget
     * @param speed the rate at which the value changes when dragged
     * @param min the minimum value of the widget's value. If both this and max are zero, the value is unbounded.
     * @param max the maximum value of the widget's value. If both this and min are zero, the value is unbounded.
     * @param valFmt a format string to control how the value is displayed in the widget box. If null, a default format string is used.
     * @return the new value of the widget
     */
    public static native int intBox(String label, int v, float speed, int min, int max, String valFmt);

    /**
     * Draws a knob widget.
     *
     * Other than the display value, everything about knobs is done in normalized values, rather
     * than actual values. This is to make it easier to support exponents and other tricky bits
     * of LXParameters; the knob doesn't actually care about that, since exponents and other
     * transformations are applied to the normalized value, which is then mapped onto the actual
     * parameter range.
     *
     * @param label the ID of (and label on) the widget
     * @param displayValue the value to display as text under the widget when manipulating
     * @param normalizedValue the value of the widget as a proportion of it's range, must be in [0, 1]
     * @param dotColor the color of the dot drawn at the center of the knob, in 0xAARRGGBB format. Passing 0 has the effect of not drawing the dot (as alpha = 0)
     * @return the new <b>normalized</b> value of the widget, in [0, 1].
     */
    public static native float knobFloat(String label, float displayValue, float normalizedValue, int dotColor);

    /**
     * Draws a knob widget with modulation rings around it.
     *
     * This is really specific to drawing knobs for CompoundParameter values.
     *
     * Other than the display value, everything about knobs is done in normalized values, rather
     * than actual values. This is to make it easier to support exponents and other tricky bits
     * of LXParameters; the knob doesn't actually care about that, since exponents and other
     * transformations are applied to the normalized value, which is then mapped onto the actual
     * parameter range.
     *
     * @param label the ID of (and label on) the widget
     * @param displayValue the value to display as text under the widget when manipulating
     * @param normalizedBase the normalized value of the underlying parameter without modulation applied, must be in [0, 1]
     * @param normalizedValue the value of the parameter after modulation is applied, must be in [0, 1]
     * @param modulatorCount the number of modulation rings to draw
     * @param modulatorMins the minimum values for each of the modulation rings, in normalized space (i.e.,, in [0, 1])
     * @param modulatorMaxs the maximum values for each of the modulation rings, in normalized space (i.e.,, in [0, 1])
     * @param modulatorColors the colors for each modulation, as 0xRRGGBB
     * @param dotColor the color of the dot drawn in the center of the knob, in 0xAARRGGBB format. Passing 0 has the effect of not drawing the dot (as alpha = 0)
     * @return the new value of the underlying parameter (this is essentially an updated normalizedBase)
     */
    public static native float knobModulatedFloat(
        String label, float displayValue, float normalizedBase, float normalizedValue,
        int modulatorCount, float[] modulatorMins, float[] modulatorMaxs, int[] modulatorColors, int dotColor);

    /**
     * Draw a checkbox-like widget that fits visually into a grid of knobs.
     *
     * @param label the ID of (and label on) the widget
     * @param value the current value of the checkbox
     * @param dotColor the color of the dot drawn in the center of the knob, in 0xAARRGGBB format. Passing 0 has the effect of not drawing the dot (as alpha = 0)
     * @return the new value of the checkbox
     */
    public static native boolean knobToggle(String label, boolean value, int dotColor);

    /**
     * Draw a button-like widget that fits visually into a grid of knobs.
     *
     * Note that this has an important behavior difference from a normal button widget: normal buttons
     * only return true on mouse-down, but do not continue firing while the mouse is being held.
     * Because this is specifically intended for momentary boolean parameters, this returns true
     * as long as the button is being held down.
     *
     * @param label the ID of (and label on) the widget
     * @param displayAsPressed if true, the widget is drawn as if it were pressed (so that these widgets can represent the state of an underlying BooleanParameter)
     * @param dotColor the color of the dot drawn in the center of the knob, in 0xAARRGGBB format. Passing 0 has the effect of not drawing the dot (as alpha = 0)
     * @return true if the button is currently being pressed
     */
    public static native boolean knobButton(String label, boolean displayAsPressed, int dotColor);

    /**
     * Draw the header of a collapsible section.
     *
     * Collapsible sections are a clever imgui trick. This function only draws the header and returns
     * whether the rest of the section should be drawn; if this function returns false, you just don't
     * draw the contents of the section. That's how "collapsing" works; you just don't call the
     * functions to draw the widgets if the header is in the collapsed state.
     *
     * @param label the ID of (and label on) the collapsible section header
     * @return true if the section is currently open
     */
    public static boolean collapsibleSection(String label) {
        return collapsibleSection(label, false, 0).isOpen;
    }

    /**
     * Draw the header of a collapsible section.
     *
     * Collapsible sections are a clever imgui trick. This function only draws the header and returns
     * whether the rest of the section should be drawn; if this function returns false, you just don't
     * draw the contents of the section. That's how "collapsing" works; you just don't call the
     * functions to draw the widgets if the header is in the collapsed state.
     *
     * This function differs from the one-argument version {@link collapsibleSection(String)} in that
     * it also draws a close button if {@code allowClose} is set. If close is clicked, the
     * {@code shouldRemove} field will be set on the returned {@link CollapseResult}.
     *
     * @param label the ID of (and label on) the collapsible section header
     * @param allowClose if true, draws a close button on the header as well as the collapse arrow
     * @return a CollapseResult object that contains both the collapsed and removed states.
     */
    public static CollapseResult collapsibleSection(String label, boolean allowClose) {
        return collapsibleSection(label, allowClose, 0);
    }

    /**
     * Draw the header of a collapsible section.
     *
     * Collapsible sections are a clever imgui trick. This function only draws the header and returns
     * whether the rest of the section should be drawn; if this function returns false, you just don't
     * draw the contents of the section. That's how "collapsing" works; you just don't call the
     * functions to draw the widgets if the header is in the collapsed state.
     *
     * This function differs from the one-argument version {@link collapsibleSection(String)} in that
     * it also draws a close button if {@code allowClose} is set. If close is clicked, the
     * {@code shouldRemove} field will be set on the returned {@link CollapseResult}.
     *
     * @param label the ID of (and label on) the collapsible section header
     * @param allowClose if true, draws a close button on the header as well as the collapse arrow
     * @param flags a bitset of TREE flags to apply to this section (you might be especially interested in {@code TREE_FLAG_DEFAULT_OPEN})
     * @return a CollapseResult object that contains both the collapsed and removed states.
     */
    public static native CollapseResult collapsibleSection(String label, boolean allowClose, int flags);

    /**
     * Draw a histogram of the given floating-point values
     *
     * @param label the ID of (and label on) the widget
     * @param values an array of floating point values to draw
     * @param min the minimum value of the Y axis
     * @param max the maximum value of the Y axis
     * @param size the height of the histogram, in pixels
     */
    public static native void histogram(String label, float[] values, float min, float max, int size);

    /**
     * Draw a line graph of the given floating-point values
     *
     * @param label the ID of (and label on) the widget
     * @param values an array of floating point values to draw
     * @param min the minimum value of the Y axis
     * @param max the maximum value of the Y axis
     * @param size the height of the graph, in pixels
     */
    public static native void plot(String label, float[] values, float min, float max, int size);

    /**
     * Draw a color swatch that is also a button
     *
     * @param id the ID of the button
     * @param h the hue of the color to draw, in [0, 360]
     * @param s the saturation of the color to draw, in [0, 100]
     * @param b the brightness of the color to draw, in [0, 100]
     * @return true if the button is pressed
     */
    public static native boolean colorButton(String id, float h, float s, float b);

    /* Images. These are package-private; code should interact with TextureManager, which
       actually handles the complexity of texture loading/unloading for you. */

    /**
     * Draw a given texture as an image.
     *
     * Client code outside of {@link TextureManager} itself should interact with {@code TextureManager}
     * instead of this function; it has all of the logic required for loading images into GL textures
     * for you, and can manage your UVs.
     *
     * @param texId the texture ID to draw
     * @param w the width in pixels of the draw rect
     * @param h the height in pixels of the draw rect
     * @param u0 the u value of the upper-left UV
     * @param v0 the v value of the upper-left UV
     * @param u1 the u value of the lower-right UV
     * @param v1 the v value of the lower-right UV
     */
    static native void image(int texId, float w, float h, float u0, float v0, float u1, float v1);

    /**
     * Draws a button with an image on it.
     *
     * Client code outside of {@link TextureManager} itself should interact with {@code TextureManager} and
     * not use this function; it has all of the logic required for loading images into GL textures for
     * you, and can manage your UVs.
     *
     * @param texId the texture ID to draw
     * @param w the width in pixels of the draw rect
     * @param h the height in pixels of the draw rect
     * @param u0 the u value of the upper-left UV
     * @param v0 the v value of the upper-left UV
     * @param u1 the u value of the lower-right UV
     * @param v1 the v value of the lower-right UV
     * @return true if the button is pressed
     */
    static native boolean imageButton(int texId, float w, float h, float u0, float v0, float u1, float v1);

    /**
     * Starts drawing the main menu bar at the top of the screen
     *
     * @return true if the menu bar should be drawn. If false, do not draw the menu and do not call {@link endMainMenuBar()}.
     */
    public static native boolean beginMainMenuBar();

    /**
     * Finish drawing the main menu bar.
     *
     * Only call this after a call to {@link beginMainMenuBar()} returns true.
     */
    public static native void endMainMenuBar();

    /**
     * Draw a menu button in a menu bar, and determine whether the menu is open.
     *
     * A menu is something like "File" or "Edit" in a traditional application; it's a button in the menu
     * bar that expands/collapses an actual menu.
     *
     * If this returns false, the menu is not open and you should not draw the menu items, nor should
     * you call {@link endMenu()}. If this returns true, you should draw the contents of the
     * menu, followed by {@link endMenu()}.
     *
     * Menu contents can be any widget, but widgets that start with {@code menu} (like {@link menuItem(String)}
     * will look more natural in menus than other widgets.
     *
     * @param label the label of this menu
     * @return true if the menu is open and its contents should be drawn
     */
    public static native boolean beginMenu(String label);

    /**
     * Finish drawing an open menu.
     */
    public static native void endMenu();

    /**
     * Draws a selectable text item in a menu
     *
     * @param label the ID of (and label on) this menu item
     * @param shortcut if not null, this text is drawn on the right side of the menu (where you would normally draw a keyboard shortcut in a menu). This has no behavioral effect.
     * @param selected if true, a checkmark is drawn on the right of this menu item
     * @param enabled if true, this menu item can be selected
     * @return true if the item is clicked in this frame
     */
    public static native boolean menuItem(String label, String shortcut, boolean selected, boolean enabled);

    /**
     * Draws a selectable text item in a menu
     *
     * @param label the ID of (and label on) this menu item
     * @return true if the item is clicked in this frame
     */
    public static boolean menuItem(String label) {
        return menuItem(label, null, false, true);
    }

    /**
     * Draws a dimmed non-selectable text item in a menu
     *
     * @param label the ID of (and label on) this menu item
     */
    public static void menuText(String label) {
        menuItem(label, null, false, false);
    }

    /**
     * Draw a toggleable menu item.
     *
     * This is a convenience method over {@link menuItem(String, String, boolean, boolean)} that manages
     * the underlying selected state; it toggles selection on click and returns the selection state
     * instead of returning the mouse state of the item.
     *
     * @param label the ID of (and label on) this menu item
     * @param shortcut if not null, this text is drawn on the right side of the menu (where you would normally draw a keyboard shortcut in a menu). This has no behavioral effect.
     * @param selected true if the menu item is currently selected
     * @param enabled if true, this menu item can be selected
     * @return the new selection state
     */
    public static native boolean menuItemToggle(String label, String shortcut, boolean selected, boolean enabled);

    /**
     * Start drawing a context menu (i.e., a right click menu) for the last widget drawn
     *
     * Returns true if the last widget drawn was right clicked and thus the context menu
     * should be drawn. If this returns true, draw the contents of the menu and then call
     * {@link endContextMenu()}. If this function returns false, do not draw the context
     * menu contents and do not call the end function.
     *
     * Any widget can be drawn in a context menu, but if you're just looking for selectable
     * text items, you should use {@link contextMenuItem(String, boolean)}, which will draw
     * nice-looking menu items.
     *
     * The open state of the menu is entirely managed by ImGUI; it will automatically close
     * the menu when an item in the menu is selected or the user clicks out of it.
     *
     * @param id the ID of this context menu (used to disambiguate context menu state between different context menus)
     * @return true if the menu is open and its contents should be drawn
     */
    public static native boolean beginContextMenu(String id);

    /**
     * Draw a context menu item
     *
     * @param label the ID of (and label on) the context menu item
     * @return true if the item was clicked
     */
    public static boolean contextMenuItem(String label) {
        return contextMenuItem(label, true);
    }

    /**
     * Draw a context menu item
     *
     * @param label the ID of (and label on) the context menu item
     * @param enabled true if the item is enabled. If false, the item is drawn grayed out and it won't respond to click events
     * @return true if the item was clicked
     */
    public static native boolean contextMenuItem(String label, boolean enabled);

    /**
     * Finish drawing the contents of a context menu.
     *
     * This should only be called after a call to {@link beginContextMenu(String)} returns true
     */
    public static native void endContextMenu();

    /**
     * Draw a tree node.
     *
     * This is a convenience method that passes the default set of flags and sets
     * the label and the ID to be the same thing.
     *
     * If this returns true, you should draw the children of this node (also using the
     * {@code treeNode} family of functions) and then call {@link treePop()}. Do not
     * call {@code treePop} if this function returns false.
     *
     * Note that this function will always draw a node with the triangle handle to
     * expand or collapse a subtree; to draw leaf nodes, use one of the variants that
     * allows you to pass flags and pass {@link TREE_FLAG_LEAF}.
     *
     * @param label the label and ID of the node
     * @return true if the subtree rooted at this tree is open
     */
    public static boolean treeNode(String label) {
        return treeNode(label, 0, label);
    }

    /**
     * Draw a tree node.
     *
     * This is a convenience method that sets the label and the ID to be the same thing.
     *
     * If this returns true, you should draw the children of this node (also using the
     * {@code treeNode} family of functions) and then call {@link treePop()}. Do not
     * call {@code treePop} if this function returns false.
     *
     * @param label the label and ID of the node
     * @param flags the tree flags that should be used to draw this node
     * @return true if the subtree rooted at this tree is open
     */
    public static boolean treeNode(String label, int flags) {
        return treeNode(label, flags, label);
    }

    /**
     * Draw a tree node.
     *
     * If this returns true, you should draw the children of this node (also using the
     * {@code treeNode} family of functions) and then call {@link treePop()}. Do not
     * call {@code treePop} if this function returns false.
     *
     * @param id the ID of the node (should be stable between frames)
     * @param flags the tree flags that should be used to draw this node
     * @param label the label on the node (can change as much as you'd like)
     * @return true if the subtree rooted at this tree is open
     */
    public static native boolean treeNode(String id, int flags, String label);

    /**
     * Finish drawing the current subtree, or finish drawing a tree widget if we're already at the root level.
     *
     * This function must be called once for every call to {@code treeNode} that returns true.
     */
    public static native void treePop();

    /**
     * Set whether the next tree node or collapsible section should be open or closed
     *
     * @param isOpen true to open it, false to close it
     */
    public static void setNextTreeNodeOpen(boolean isOpen) {
        setNextTreeNodeOpen(isOpen, COND_ALWAYS);
    }

    /**
     * Conditionally set whether the next tree node or collapsible section should be open or closed
     *
     * @param isOpen true to open it, false to close it
     * @param when one of the {@code COND} flags that sets the conditions in which this function call should apply
     */
    public static native void setNextTreeNodeOpen(boolean isOpen, int when);

    /**
     * Mark the previously drawn widget as a drag-drop source
     *
     * If this returns true, the widget is currently being dragged and its payload is needed; you must
     * set the drag-drop payload using {@link setDragDropPayload(String, Object)} and call
     * {@link endDragDropSource()}. If this returns false, do not call either.
     *
     * @return true if the widget is currently being dragged and its payload is needed
     */
    public static boolean beginDragDropSource() {
        return beginDragDropSource(0);
    }

    /**
     * Mark the previously drawn widget as a drag-drop source
     *
     * If this returns true, the widget is currently being dragged and its payload is needed; you must
     * set the drag-drop payload using {@link setDragDropPayload(String, Object)} and call
     * {@link endDragDropSource()}. If this returns false, do not call either.
     *
     * @param flags Flags to pass to the drag-drop system. None of the imgui flags are currently exposed via slimgui.
     *
     * @return true if the widget is currently being dragged and its payload is needed
     */
    public static native boolean beginDragDropSource(int flags);

    /**
     * Sets the drag-drop payload for the currently-active drag
     *
     * The string types are matched between source and target to make sure they're compatible.
     * ParameterUI defines a few of these, as do the cube mapping and output systems, but in general
     * they should just be reasonably short and scoped strings. All built-in drag-drop payload types
     * start with "_", so type strings should not start with "_" unless they are intentionally
     * integrating with built-in drag-drop types in Dear Imgui.
     *
     * This can be called multiple times for a single source with multiple payload types, to allow
     * for a single widget to be interpreted multiple ways.
     *
     * @param type the type of the payload, used to ensure that sources and targets are compatible
     * @param data the data to pass through the payload
     * @return whether the payload was successfully loaded into slimgui
     */
    public static native boolean setDragDropPayload(String type, Object data);

    /**
     * Called to declare that we have finished specifying the payload(s) for an active drag-drop source.
     *
     * This should only be called after a call to {@link beginDragDropSource()} returns true.
     */
    public static native void endDragDropSource();

    /**
     * Mark the previously-drawn widget as a drag-drop target
     *
     * If this returns true, there is a drag-drop payload available for dropping. If the widget wants
     * it, it should call {@link acceptDragDropPayload(String, Class)}. If this returns true, you
     * must follow it with a call to {@link endDragDropTarget()}.
     *
     * @return true if there is a payload in flight that could be accepted
     */
    public static native boolean beginDragDropTarget();

    /**
     * Called to declare that we are finished handling drag-drop payloads for the current widget.
     *
     * This must be called for every call to {@link beginDragDropTarget()} that returns true.
     */
    public static native void endDragDropTarget();

    /**
     * Accepts a drag-drop payload of a given type.
     *
     * If the type specified here matches the type of one of the currently in-flight payloads, this
     * will return the object that was specified in that payload.
     *
     * @param type the string type of the payload this call accepts, which is matched against the string type specified in {@link setDragDropPayload(String, Object)}
     * @param flags flags to pass to the drag-drop system. None of the Dear Imgui drag-drop flags are currently exposed via slimgui.
     * @return the object associated with the payload, or null if there is no payload with the given type
     */
    public static native @Nullable Object acceptDragDropPayload(String type, int flags);

    /**
     * Accepts a drag-drop payload of a given type.
     *
     * If the type specified here matches the type of one of the currently in-flight payloads, this
     * will return the object that was specified in that payload.
     *
     * This is a convenience wrapper around {@link acceptDragDropPayload(String, int)} with "typechecking".
     *
     * @param type the string type of the payload this call accepts, which is matched against the string type specified in {@link setDragDropPayload(String, Object)}
     * @param <T> the expected type of the payload object
     * @param cls the class object associated with the given payload
     * @return the object in the payload, or null if there is no payload with the given string type or if the class does not match the class of the payload object
     */
    public static @Nullable <T> T acceptDragDropPayload(String type, Class<T> cls) {
        Object res = acceptDragDropPayload(type, 0);
        if (res == null || !cls.isAssignableFrom(res.getClass())) {
            return null;
        }
        //noinspection unchecked
        return (T) res;
    }

    /**
     * Determine if the last widget drawn was clicked in this frame
     *
     * @return true if the last widget drawn was clicked in this frame
     */
    public static boolean isItemClicked() {
        return isItemClicked(0, false);
    }

    /**
     * Determine if the last widget drawn was clicked in this frame by a given mouse button
     *
     * @param mouseButton the mouse button to check. 0 is left, 1 is middle, 2 is right.
     * @return true if the last widget drawn was clicked in this frame
     */
    public static boolean isItemClicked(int mouseButton) {
        return isItemClicked(mouseButton, false);
    }

    /**
     * Determine if the last widget drawn was clicked in this frame
     *
     * @param allowMouseHold if true, return true on every frame in which the mouse is down on the widget. If false, this only returns true on the first frame in which the mouse is down on the widget.
     * @return true if the last widget drawn is being clicked
     */
    public static boolean isItemClicked(boolean allowMouseHold) {
        return isItemClicked(0, allowMouseHold);
    }

    /**
     * Determine if the last widget drawn was clicked in this frame by a given mouse button
     *
     * @param mouseButton the mouse button to check. 0 is left, 1 is middle, 2 is right.
     * @param allowMouseHold if true, return true on every frame in which the mouse is down on the widget. If false, this only returns true on the first frame in which the mouse is down on the widget.
     * @return true if the last widget drawn is being clicked
     */
    public static native boolean isItemClicked(int mouseButton, boolean allowMouseHold);

    /**
     * Determine if the last widget drawn was double-left-clicked in this frame
     *
     * @return true if the last widget drawn was double-left-clicked in this frame
     */
    public static boolean isItemDoubleClicked() {
        return isItemDoubleClicked(0);
    }

    /**
     * Determine if the last widget drawn was double-clicked in this frame by a given mouse button
     *
     * @param mouseButton the mouse button to check. 0 is left, 1 is middle, 2 is right.
     * @return true if the last widget drawn was double-clicked in this frame by the given mouse button
     */
    public static native boolean isItemDoubleClicked(int mouseButton);

    /**
     * Return true if the last widget drawn is "active"
     *
     * Active means that it has focus. For keyboard-focusable widgets, this means keyboard focus or
     * a click. For all other widgets, this means the mouse is currently down on part of the widget
     * that is interactive.
     *
     * @return true if the widget is active
     */
    public static native boolean isItemActive();

    /**
     * Return true if the mouse pointer is currently over the last widget drawn
     *
     * @return true if the mouse pointer is currently over the last widget drawn
     */
    public static native boolean isItemHovered();

    /**
     * Return true if the alt key is down
     *
     * @return true if the alt key is down
     */
    public static native boolean isAltDown();

    /**
     * Return true if the control key is down
     *
     * @return true if the control key is down
     */
    public static native boolean isCtrlDown();

    /**
     * Return true if the shift key is down
     *
     * @return true if the shift key is down
     */
    public static native boolean isShiftDown();

    /**
     * Return true if the given key is currently pressed
     *
     * "Pressed" here means that the physical key is currently depressed. This function returns
     * true for any frame in which the key is depressed.
     *
     * @param keycode the GLFW keycode for the given key. These are the {@code KEY} constants from {@link org.lwjgl.glfw.GLFW}.
     * @return true if the key is depressed
     */
    static native boolean isKeyPressed(int keycode);

    /**
     * Return true if the given key was newly pressed in this frame
     *
     * This emulates a "key down" event; it debounces keys and diffs from frame to frame to only
     * fire this when the key goes from up to pressed.
     *
     * @param keycode the GLFW keycode for the given key. These are the {@code KEY} constants from {@link org.lwjgl.glfw.GLFW}.
     * @return true if the key was pressed on this frame
     */
    static native boolean isKeyDown(int keycode);

    /**
     * Returns true if the given key chord is currently down.
     *
     * This ensures we don't react to key events that are going to be consumed
     * by slimgui. This should almost always be preferred to {@link isKeyDown(int)}, unless
     * it's for a key that you want to be active when a text field has focus (like the Enter
     * key submitting a form).
     *
     * @param keycodes the keys that must all be down for the event to fire
     * @return true if the key chord is newly pressed on this frame
     */
    public static boolean isApplicationKeyChordDown(int... keycodes) {
        if (UI.wantCaptureKeyboard()) {
            return false;
        }
        boolean allDown = true;
        for (int keycode : keycodes) {
            allDown = allDown && UI.isKeyDown(keycode);
        }
        return allDown;
    }

    /**
     * Returns true if the given key chord has fired on this frame.
     *
     * This handles two things: key debouncing for key chords, and ensuring we don't
     * react to key events that are going to be consumed by slimgui. This should almost always
     * be preferred to {@link isKeyPressed(int)} or {@link isKeyDown(int)}, unless
     * it's for a key that you want to be active when a text field has focus (like the Enter
     * key submitting a form).
     *
     * @param keycodes the keys that must all be down for the event to fire
     * @return true if the key chord is newly pressed on this frame
     */
    public static boolean isApplicationKeyChordPressed(int... keycodes) {
        if (UI.wantCaptureKeyboard()) {
            return false;
        }
        /* we only return true if one of these keys was pressed on this frame and
         * all of them are down. */
        boolean anyPressed = false;
        boolean allDown = true;
        for (int keycode : keycodes) {
            allDown = allDown && UI.isKeyDown(keycode);
            anyPressed = anyPressed || UI.isKeyPressed(keycode);
        }
        return anyPressed && allDown;
    }

    /**
     * Returns true if any item has keyboard focus
     */
    public static native boolean isAnyItemFocused();

    /**
     * Set keyboard focus on the next widget to be drawn.
     */
    public static void setKeyboardFocusHere() {
        setKeyboardFocusHere(0);
    }

    /**
     * Set keyboard focus on the next widget to be drawn.
     *
     * @param offset indexes into the sub-widgets of widgets where that applies (like vector sliders)
     */
    public static native void setKeyboardFocusHere(int offset);

    /**
     * Get slimgui's measurement of frame timings
     *
     * @return slimgui's frame rate, in frames per second
     */
    static native float getFrameRate();

    /**
     * Returns true if slimgui has keyboard focus and is consuming key events
     *
     * If this is true, the rest of the application should ignore key events.
     *
     * @return true if slimgui wants keyboard focus
     */
    static native boolean wantCaptureKeyboard();

    /**
     * Returns true if slimgui has mouse focus and is consuming mouse events
     *
     * If this is true, the rest of the application should ignore mouse events, especially
     * mouse button events.
     *
     * @return true if slimgui wants mouse focus
     */
    static native boolean wantCaptureMouse();

    /**
     * Tells slimgui that the given key is depressed
     *
     * @param keycode the GLFW key code of the key that was depressed
     */
    static native void keyDown(int keycode);

    /**
     * Tells slimgui that the given key was released
     *
     * @param keycode the GLFW key code of the key that was released
     */
    static native void keyUp(int keycode);

    /**
     * Add a character of text input to slimgui's input buffer
     *
     * Slimgui gets all of its keyboard input this way; {@link keyDown(int)} is used for querying
     * the state of specific keys, but those key events are not translated into typing for
     * text input. That all happens through this interface instead, so the OS can translate key
     * codes into characters for us.
     *
     * @param c the character to add
     */
    static native void addInputCharacter(char c);

    /**
     * Tells slimgui that the scroll wheel moved by a certain amount
     *
     * @param amount the amount the scroll wheel moved
     */
    static native void scrolled(float amount);

    /**
     * Draws the Dear ImGui demo window
     *
     * @return true if the window should remain visible (if false, the close button was clicked)
     */
    static native boolean showDemoWindow();

    /**
     * Draws the Dear ImGui metrics window
     *
     * This contains a ton of information about the draw calls that are happening in each
     * frame, and is invaluable for debugging drawing issues.
     *
     * @return true if the window should remain visible (if false, the close button was clicked)
     */
    static native boolean showMetricsWindow();

    /**
     * Draws the Dear ImGui style editor window
     *
     * @return true if the window should remain visible (if false, the close button was clicked)
     */
    static native boolean showStyleEditor();

    /**
     * Draws the Dear ImGui "About" window
     *
     * @return true if the window should remain visible (if false, the close button was clicked)
     */
    static native boolean showAboutWindow();

    static {
        System.loadLibrary("slimgui");
    }
}
