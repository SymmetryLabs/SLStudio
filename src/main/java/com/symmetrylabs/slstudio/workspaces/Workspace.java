package com.symmetrylabs.slstudio.workspaces;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;

import java.io.File;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Workspace extends LXComponent {
    public static final int WORKSPACE_OSC_PORT = 3999;
    private final LX lx;
    private final String path;
    private final List<WorkspaceProject> projects = new ArrayList<WorkspaceProject>();
    private int currentWorkspaceIndex = 0;

    public Workspace(LX lx, String path) {
        super(lx, "workspaces");
        this.lx = lx;
        this.path = path;
        loadProjectFiles();
        Collections.sort(projects, Comparator.comparing(WorkspaceProject::getLabel));

        try {
            lx.engine.osc.receiver(WORKSPACE_OSC_PORT).addListener(new SwitchProjectOscListener());
        } catch (SocketException sx) {
            throw new RuntimeException(sx);
        }
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
        int newWorkspaceIndex = projects.indexOf(workspace);
        if (currentWorkspaceIndex == newWorkspaceIndex) {
            return;
        }
        System.out.println(String.format("openProject new=%d current=%d", newWorkspaceIndex, currentWorkspaceIndex));
        lx.openProject(workspace.getFile());
        currentWorkspaceIndex = newWorkspaceIndex;
    }

    private void loadProjectFiles() {
        final File[] filesArr = new File(path).listFiles((dir, name) -> name.endsWith(".lxp"));
        for (int i = 0; i < filesArr.length; i++) {
            WorkspaceProject workspace = new WorkspaceProject(filesArr[i]);
            this.projects.add(workspace);
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
                WorkspaceProject workspace = projects.get(message.getInt() - 1);
                lx.engine.addTask(() -> openProject(workspace));
            }
        }
    }
}
