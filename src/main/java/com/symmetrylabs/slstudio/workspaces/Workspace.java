package com.symmetrylabs.slstudio.workspaces;

import java.io.File;

public class Workspace {
    private File file = null;
    private String name = null;

    public Workspace(File file) {
        if (file != null) {
            this.file = file;
            this.name = file.getName().substring(0, file.getName().lastIndexOf('.'));
        }
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public static class Emtpy extends Workspace {
        public Emtpy() {
            super(null);
        }
    }
}