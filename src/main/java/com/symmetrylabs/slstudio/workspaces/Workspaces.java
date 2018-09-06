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

public class Workspaces extends LXComponent {
    public static final int WORKSPACE_OSC_PORT = 3999;
    private final LX lx;
    private final String path;
    private final List<Workspace> workspaces = new ArrayList<Workspace>();
    private int currentWorkspaceIndex = 0;

    public Workspaces(LX lx, String path) {
        super(lx, "workspaces");
        this.lx = lx;
        this.path = path;
        loadProjectFiles();
        Collections.sort(workspaces, Comparator.comparing(Workspace::getLabel));

        try {
            lx.engine.osc.receiver(WORKSPACE_OSC_PORT).addListener(new SwitchProjectOscListener());
        } catch (SocketException sx) {
            throw new RuntimeException(sx);
        }
    }

    public void goIndex(int i) {
        if (i < 0 || i > workspaces.size() - 1) {
            return;
        }
        System.out.println(String.format(
            "goIndex i=%d workspace=%s", i, workspaces.get(i).getLabel()));
        openWorkspace(workspaces.get(i));
    }

    public int size() {
        return workspaces.size();
    }

    public void openWorkspace(Workspace workspace) {
        int newWorkspaceIndex = workspaces.indexOf(workspace);
        if (currentWorkspaceIndex == newWorkspaceIndex) {
            return;
        }
        System.out.println(String.format("openWorkspace new=%d current=%d", newWorkspaceIndex, currentWorkspaceIndex));
        lx.openProject(workspace.getFile());
        currentWorkspaceIndex = newWorkspaceIndex;
    }

    private void loadProjectFiles() {
        final File[] filesArr = new File(path).listFiles((dir, name) -> name.endsWith(".lxp"));
        for (int i = 0; i < filesArr.length; i++) {
            Workspace workspace = new Workspace(filesArr[i]);
            this.workspaces.add(workspace);
        }
    }

    public List<Workspace> getAll() {
        return Collections.unmodifiableList(workspaces);
    }

    public Workspace get(String name) {
        for (Workspace workspace : workspaces) {
            if (name.equals(workspace.getLabel())) {
                return workspace;
            }
        }
        return null;
    }

    private class SwitchProjectOscListener implements LXOscListener {
        public void oscMessage(OscMessage message) {
            if (message.matches("/lx/workspaces/active")) {
                Workspace workspace = workspaces.get(message.getInt() - 1);
                openWorkspace(workspace);
            }
        }
    }
}
