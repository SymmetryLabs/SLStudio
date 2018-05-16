package com.symmetrylabs.slstudio.workspaces;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.ArrayList;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import static com.symmetrylabs.slstudio.util.Utils.sketchPath;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.ObjectParameter;
import heronarts.lx.osc.LXOscEngine;
import heronarts.lx.osc.LXOscListener;
import heronarts.lx.osc.OscMessage;

import java.util.Collections;
import java.util.Comparator;

public class Workspaces extends LXComponent {

    private final LX lx;

    private final String path = sketchPath("workspaces");

    private final List<Workspace> workspaces = new ArrayList<Workspace>();

    private int currentWorkspaceIndex = 0;

    public Workspaces(LX lx) {
        super(lx, "workspaces");
        this.lx = lx;

        // not dynamic at runtime
        loadProjectFiles();

        if (workspaces.size() > 0) {
            Collections.sort(workspaces, new Comparator<Workspace>() {
                    @Override
                    public int compare(final Workspace w1, final Workspace w2) {
                            return w1.getLabel().compareTo(w2.getLabel());
                    }
            });
        }

        //openWorkspace(workspaces.get(5));
    }

    public void goIndex(int i) {
        if (i < 0 || i > workspaces.size() - 1) {
            return;
        }
        System.out.println("goIndex(" + i + ")");
        System.out.println("workspace: " + workspaces.get(i).getLabel());
        openWorkspace(workspaces.get(i));
    }

    public void goPrev() {
        int index = currentWorkspaceIndex-1;
        if (index == -1) {
            index = workspaces.size()-1;
        }
        goIndex(index);
        System.out.println("goPrev()");
    }

    public void goNext() {
        int index = currentWorkspaceIndex+1;
        if (index == workspaces.size()) {
            index = 0;
        }
        goIndex(index);
        System.out.println("goNext()");
    }

    public int size() {
        return workspaces.size();
    }

    public void openWorkspace(Workspace workspace) {
        int newWorkspaceIndex = workspaces.indexOf(workspace);
        System.out.println("newWorkspaceIndex: " + newWorkspaceIndex);
        System.out.println("currentWorkspaceIndex: " + currentWorkspaceIndex);

        if (currentWorkspaceIndex == newWorkspaceIndex) {
            return;
        }

        lx.openProject(workspace.getFile());
        currentWorkspaceIndex = newWorkspaceIndex;

        System.out.println("Open new workspace: " + workspace.getLabel());
    }

    private void loadProjectFiles() {
        final File[] filesArr = new File(path).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".lxp");
            }
        });

        try {
            System.out.println("\nLoaded the following workspaces:");

            for (int i = 0; i < filesArr.length; i++) {
                Workspace workspace = new Workspace(filesArr[i]);
                this.workspaces.add(workspace);
                System.out.println(i+1 + ") " + workspace.getLabel());
            }
            System.out.print("\n");
        } catch (Exception e) {
            System.out.println("Could not print workspaces");
        }
    }

    public void attachOscListener() {
        try {
            lx.engine.osc.receiver(3232).addListener(new SwitchProjectOscListener());
        } catch (SocketException sx) {
            throw new RuntimeException(sx);
        } 
    }

    public List<Workspace> getAll() {
        return workspaces;
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
            if (message.matches("/lx/golive")) {
                if (message.getFloat() > 0.5) {
                    if (!lx.engine.output.enabled.isOn()) {
                        lx.engine.output.enabled.setValue(true);
                    }
                }
            }

            // TEMP PLACE FOR THIS
            if (message.matches("/lx/tempo/tap")) {
                float val = message.getFloat();
                System.out.println(val);
                lx.tempo.tap.setValue(val);
            }

            if (message.matches("/lx/workspaces/active")) {
                Workspace workspace = workspaces.get(message.getInt()-1);
                openWorkspace(workspace);
            }
        }
    }
}
