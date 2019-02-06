package com.symmetrylabs.slstudio.ui.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages and draws windows in the UI.
 *
 * There are two kinds of windows that WindowManager can manage: persistent and
 * transient windows. Persistent windows appear in the Window toolbar at the
 * top, and have a consistent API through which they can be displayed. Only one
 * instance of a persistent window can be displayed at any one time. Transient
 * windows are windows that are opened through some other action in the UI, and
 * once they are closed they can only be re-opened by taking that action again.
 * WindowManager does not concern itself with creating transient windows; other
 * classes create them and add them to the set of displayed windows by calling
 * WindowManager's {@link add(Window w)} method.
 *
 * Whether or not a window is closeable is not WindowManager's concern; closeable
 * window logic is implemented by {@link CloseableWindow}, which does the drawing
 * of the close button and removes the window from WindowManager's set of windows
 * when the close button is pressed.
 *
 * It is important that nothing maintain a persistent reference to the WindowManager
 * reference, so that the {@link reset()} method here can discard the only persistent
 * reference to the WindowManager and cause it to be garbage collected. To that end,
 * the public API of WindowManager is all static methods that call into the current
 * global. This design prevents client code from being able to access a reference
 * to WindowManager at all.
 */
public class WindowManager {
    private static WindowManager INSTANCE = new WindowManager();

    /** A factory function to create Window instances. */
    public interface WindowCreator {
        Window create();
    }

    /**
     * Add a persistent window to the window manager.
     *
     * Once added, this window will appear in the Window menu in the main menu bar.
     *
     * @param name The label for the window in the Window menu
     * @param creator A factory function to make the window when requested
     * @param displayByDefault When true, an instance of the window is created and displayed when this function is called.
    */
    public static void addPersistent(String name, WindowCreator creator, boolean displayByDefault) {
        INSTANCE.addPersistentImpl(name, creator, displayByDefault);
    }

    /**
     * Add a transient window to the window manager
     * @param w The window to add.
     */
    public static void addTransient(Window w) {
        INSTANCE.addTransientImpl(w);
    }

    /**
     * Close a window currently being displayed.
     *
     * For transient windows, this removes the window completely. For persistent windows,
     * the window is hidden but its state is maintained. Calling closeWindow on a window
     * that is not currently in the draw list is a no-op.
     *
     * @param w the window to close.
     */
    public static void closeWindow(Window w) {
        INSTANCE.closeWindowImpl(w);
    }

    /**
     * Request that a persistent window be displayed, based on the name of the window. If
     * the window is already being displayed, this is a no-op.
     *
     * @param name the name of the persistent window to show (must match the name assigned in addPersistent)
     * @throws RuntimeException if a persistent window with the given name hasn't been added
     */
    public static void showPersistent(String name) {
        INSTANCE.showPersistentImpl(name);
    }

    /* Package-private implementation follows */

    /** @return the global WindowManager singleton instance. */
    static WindowManager get() {
        return INSTANCE;
    }

    /** Creates a new global WindowManager singleton, dropping its reference to the old one. */
    static void reset() {
        INSTANCE = new WindowManager();
    }

    static class PersistentWindow {
        String name;
        WindowCreator creator;
        Window current;
    }

    private final List<PersistentWindow> persistentWindows;
    private final List<Window> transientWindows;

    /* these are used to allow for adding/removing of windows from draw() methods;
         we queue up mutations to transient/persistentWindows here, so that we don't
         mutate those collections while we're iterating over them. Access to both of
         these collections must be synchronized on the collection being accessed. */
    private final List<Window> transientToAdd;
    private final Set<Window> toRemove;

    private boolean uiEnabled;

    protected WindowManager() {
        transientWindows = new ArrayList<>();
        transientToAdd = new ArrayList<>();
        toRemove = new HashSet<>();
        persistentWindows = new ArrayList<>();
        uiEnabled = true;
    }

    void addPersistentImpl(String name, WindowCreator creator, boolean displayByDefault) {
        PersistentWindow ws = new PersistentWindow();
        ws.name = name;
        ws.creator = creator;
        ws.current = displayByDefault ? creator.create() : null;
        persistentWindows.add(ws);
    }

    void addTransientImpl(Window w) {
        transientToAdd.add(w);
    }

    void draw() {
        /* Some windows have the power to enable and disable the UI; the moment
           we are disabled, we want to stop drawing, so we check at the start
           and then at the end of every window draw whether we are disabled and
           should stop drawing. This is important because the window that
           requested the UI be disabled may have started some kind of
           asynchronous job that would cause other windows to not see a
           consistent view of the engine if the job were to complete in the
           middle of drawing the other window. */
        if (!uiEnabled) {
            return;
        }
        for (Window w : transientWindows) {
            w.draw();
            if (!uiEnabled) {
                return;
            }
        }
        for (PersistentWindow w : persistentWindows) {
            if (w.current != null) {
                w.current.draw();
                if (!uiEnabled) {
                    return;
                }
            }
        }

        synchronized (toRemove) {
            transientWindows.removeAll(toRemove);
            for (PersistentWindow pw : persistentWindows) {
                if (pw.current != null && toRemove.contains(pw.current)) {
                    pw.current = null;
                }
            }
            toRemove.clear();
        }
        synchronized (transientToAdd) {
            transientWindows.addAll(transientToAdd);
            transientToAdd.clear();
        }
    }

    void closeWindowImpl(Window w) {
        synchronized (toRemove) {
            toRemove.add(w);
        }
    }

    void enableUI() {
        uiEnabled = true;
    }

    void disableUI() {
        uiEnabled = false;
    }

    Collection<PersistentWindow> getSpecs() {
        return persistentWindows;
    }

    void showPersistent(PersistentWindow ws) {
        if (ws.current != null) {
            return;
        }
        ws.current = ws.creator.create();
    }

    void showPersistentImpl(String name) {
        for (PersistentWindow pw : persistentWindows) {
            if (pw.name.equals(name)) {
                showPersistent(pw);
                return;
            }
        }
        throw new RuntimeException("no persistent window defined with name \"" + name + "\"");
    }

    void hidePersistent(PersistentWindow ws) {
        if (ws.current == null) {
            return;
        }
        synchronized (toRemove) {
            toRemove.add(ws.current);
        }
    }
}
