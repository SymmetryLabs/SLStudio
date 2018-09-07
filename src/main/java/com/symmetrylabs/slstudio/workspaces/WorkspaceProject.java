package com.symmetrylabs.slstudio.workspaces;

import java.io.File;

public class WorkspaceProject {
    private File file = null;
    private String label = null;

    public WorkspaceProject(File file) {
        if (file.exists()) {
            this.file = file;
            this.label = file.getName().substring(0, file.getName().lastIndexOf('.'));
        }
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file.getName();
    }

    public String getLabel() {
        return label;
    }

    public boolean matches(String label) {
        if (!hasLoaded()) {
            return false;
        }
        return label.equals(label);
    }

    public boolean matches(File file) {
        if (!hasLoaded()) {
            return false;
        }
        return getFileName().equals(file.getName());
    }

    public boolean hasLoaded() {
        return (file != null) && file.exists();
    }
}
