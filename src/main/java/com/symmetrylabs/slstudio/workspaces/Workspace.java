package com.symmetrylabs.slstudio.workspaces;

import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;
import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class Workspace extends LXComponent {
    public static final int WORKSPACE_OSC_PORT = 3999;
    private static final int NO_PROJECT = -1;

    /** Switching projects is expensive, and a poorly-configured OSC sender
            can DoS SLStudio by sending project-switch events on every frame. This
            prevents projects from being switched more often than once every 500ms.
            Note that it does not queue up requests to be served after the debounce
            time elapses; OSC clients should send the desired project on every
            frame, so that eventually their request will be honored. */
    private static final long MIN_TIME_BETWEEN_SWITCHES_NS = (long) 0.5e9;

    private final LX lx;
    private final SLStudioLX.UI ui;
    private final String path;
    private final List<WorkspaceProject> projects = new ArrayList<WorkspaceProject>();
    private int currentWorkspaceIndex = NO_PROJECT;
    private int successfulWorkspaceSwitches = 0;
    private long lastSwitchTime = 0;

    private int requestsBeforeSwitch = 0;
    private int matchingRequestsReceived = 0;
    private int request = NO_PROJECT;

    public Workspace(LX lx, SLStudioLX.UI ui, String path) {
        super(lx, "workspaces");
        this.lx = lx;
        this.ui = ui;
        this.path = path;

        loadProjectFiles();
        Collections.sort(projects, Comparator.comparing(WorkspaceProject::getLabel));

        lx.addProjectListener((f, s) -> {
            if (s == LX.ProjectListener.Change.OPEN) {
                setCurrentProject(f);
            }
        });
        setCurrentProject(lx.getProject());

        try {
            lx.engine.osc.receiver(WORKSPACE_OSC_PORT).addListener(new SwitchProjectOscListener());
        } catch (SocketException sx) {
            throw new RuntimeException(sx);
        }
    }

    /**
     * Sets the number of OSC requests we must receive before we do a project switch.
     *
     * Untrustworthy OSC sources (like Vezer with an untrustworthy time code
     * source cough cough) will sometimes send a single frame with the wrong
     * project set in it. Shows can request that we receive the same project
     * in a number of adjacent frames before we switch to it by setting this
     * to something nonzero.
     */
    public void setRequestsBeforeSwitch(int r) {
        requestsBeforeSwitch = r;
    }

    private void setCurrentProject(File f) {
        if (f != null) {
            try {
                String path = f.getCanonicalPath();
                for (int i = 0; i < projects.size(); i++) {
                    WorkspaceProject p = projects.get(i);
                    if (p.getFile().getCanonicalPath().equals(path)) {
                        currentWorkspaceIndex = i;
                        return;
                    }
                }
            } catch (IOException e) {
                System.err.println("couldn't find project in workspace: ");
                e.printStackTrace();
                currentWorkspaceIndex = NO_PROJECT;
            }
        }
        currentWorkspaceIndex = NO_PROJECT;
    }

    public void goIndex(int i) {
        if (i < 0 || i > projects.size() - 1) {
            return;
        }
        System.out.println(String.format(
            "goIndex i=%d workspace=%s", i, projects.get(i).getLabel()));
        openProject(projects.get(i));
    }

    public int size() {
        return projects.size();
    }

    public void openProject(WorkspaceProject workspace) {
        long now = System.nanoTime();
        if (now - lastSwitchTime < MIN_TIME_BETWEEN_SWITCHES_NS) {
            return;
        }

        int newWorkspaceIndex = projects.indexOf(workspace);
        if (currentWorkspaceIndex == newWorkspaceIndex || newWorkspaceIndex < 0) {
            return;
        }
        lastSwitchTime = now;

        System.out.println(String.format("openProject new=%d current=%d", newWorkspaceIndex, currentWorkspaceIndex));
        currentWorkspaceIndex = newWorkspaceIndex;

        /* We need this to not run during a UI redraw event, so we register handlers to run
         * after the frame, and our LX engine task will wait on that handler to run
         * before it switches projects.
         *
         * When we switch projects during a draw event, we often remove things in the middle
         * of them being drawn. That being possible at all is probably a bad thing,
         * but to mitigate the issue for now we wait until we're done drawing, and then freeze
         * the UI while we load the project. Once the engine loop is running again we let the
         * UI loop continue.
         *
         * To make sure we don't deadlock SLStudio completely, we also put a timer on the whole
         * thing that just releases all of the semaphores, letting everything continue after
         * a timeout. That timer task is a no-op if it happens after either of the other two
         * tasks complete.
         */

        /* A permit is put into this semaphore when we aren't currently drawing the UI. */
        Semaphore drawDoneLock = new Semaphore(0);

        /* A permit is put into this semaphore once the engine starts up again after we
         * request a project switch. */
        Semaphore loadDoneLock = new Semaphore(0);

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                /* Give both semaphores a permit, which lets LX switch the project and lets
                 * the UI continue drawing. This is here entirely to prevent a deadlock and
                 * should never be necessary. */
                drawDoneLock.release();
                loadDoneLock.release();
            }
        }, 1000);

        /* We want to swap projects once we're done with all of the stuff that happens
         * during the engine loop, so we don't swap out channels while stuff is interacting
         * with them. This task waits for the signal that we aren't drawing before it
         * requests a project change. */
        lx.engine.addTask(() -> {
            try {
                drawDoneLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lx.openProject(workspace.getFile());
        });

        /* We set up this task to run after the next frame of the UI is rendered. It
         * puts a permit into drawDoneLock, which allows our engine task to run, and then
         * it waits for a permit on the loadDoneLock before it lets the UI continue.
         * Blocking in this task blocks the whole UI, so we can ensure the project load
         * won't smash the UI by waiting for the engine to run our task (i.e., for the
         * project load to be done). */
        ui.runAfterDraw(() -> {
            drawDoneLock.release();
            lx.engine.addTask(() -> loadDoneLock.release());
            try {
                loadDoneLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            successfulWorkspaceSwitches++;
            System.out.println(String.format(
                "workspace switch %d finished successfully", successfulWorkspaceSwitches));
        });
    }

    private void loadProjectFiles() {
        final File[] filesArr = new File(path).listFiles((dir, name) -> name.endsWith(".lxp"));
        if (filesArr != null) {
            for (int i = 0; i < filesArr.length; i++) {
                WorkspaceProject workspace = new WorkspaceProject(filesArr[i]);
                this.projects.add(workspace);
            }
        }
    }

    public List<WorkspaceProject> getAll() {
        return Collections.unmodifiableList(projects);
    }

    public WorkspaceProject get(String name) {
        for (WorkspaceProject workspace : projects) {
            if (name.equals(workspace.getLabel())) {
                return workspace;
            }
        }
        return null;
    }

    private class SwitchProjectOscListener implements LXOscListener {
        public void oscMessage(OscMessage message) {
            if (message.matches("/lx/workspaces/active")) {
                int index = message.getInt();
                if (index < 0 || index >= projects.size()) {
                    System.err.println(String.format("workspace project index %d invalid", index));
                    return;
                }
                if (requestsBeforeSwitch > 0 && index != currentWorkspaceIndex) {
                    if (index != request) {
                        System.out.println(String.format(
                            "received request for project %d, waiting for debounce", index));
                        request = index;
                        matchingRequestsReceived = 1;
                        return;
                    } else {
                        matchingRequestsReceived++;
                        if (matchingRequestsReceived < requestsBeforeSwitch) {
                            return;
                        }
                    }
                    System.out.println(String.format("starting switch to project %d", index));
                }
                WorkspaceProject workspace = projects.get(index);
                openProject(workspace);
            }
        }
    }
}
