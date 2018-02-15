package com.symmetrylabs.slstudio.workspaces;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.ArrayList;

import static com.symmetrylabs.slstudio.util.Utils.sketchPath;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.ObjectParameter;

public class Workspaces extends LXComponent {

    private final LX lx;

    private final String path = sketchPath("workspaces");

    private final List<Workspace> workspaces = new ArrayList<Workspace>();

    public final ObjectParameter<Workspace> active = new ObjectParameter<Workspace>("active", new Workspace[] {new Workspace.Emtpy()});

    public Workspaces(LX lx) {
        super(lx, "workspaces");
        this.lx = lx;
        addParameter(active);

        // only happens on intial load for now (won't change on removing or creating new project files)
        loadProjectFiles();
        removeEmtpyWorkspaces();
        setParameterWorkspaces();

        active.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter param) {
                openWorkspace((Workspace)(((ObjectParameter)param).getObject()));
            }
        });
    }

    public void openWorkspace(Workspace workspace) {
        lx.openProject(workspace.getFile());
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
                System.out.println(i+1 + ") " + workspace.getName());
            }
            System.out.print("\n");
        } catch (Exception e) {
            System.out.println("Could not print workspaces");
        }
    }

    private void removeEmtpyWorkspaces() {
        for (Workspace workspace : workspaces) {
            if (workspace.getName() == null) {
                workspaces.remove(workspace);
            }
        }
    }

    private void setParameterWorkspaces() {
        Workspace[] workspacesArr = new Workspace[workspaces.size()];

        for (int i = 0; i < workspacesArr.length; i++) {
            workspacesArr[i] = workspaces.get(i);
        }

        active.setObjects(workspacesArr);
    }

    public List<Workspace> getAll() {
        return workspaces;
    }

    public Workspace get(String name) {
        for (Workspace workspace : workspaces) {
            if (name.equals(workspace.getName())) {
                return workspace;
            }
        }
        return null;
    }
}