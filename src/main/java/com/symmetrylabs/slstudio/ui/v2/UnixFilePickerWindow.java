package com.symmetrylabs.slstudio.ui.v2;

import heronarts.lx.LX;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.nio.file.Files;
import java.io.IOException;


public class UnixFilePickerWindow implements Window {
    private static int windowId = 0;

    private final LX lx;
    private final String title;
    private final FileDialog.Type type;
    private final FileDialog.FilePickedCallback fpc;

    private Path currentPath;
    private String currentPathString;
    private final Path defaultDirectory;
    private boolean opened = false;

    public UnixFilePickerWindow(LX lx, String title, FileDialog.Type type, FileDialog.FilePickedCallback fpc) {
        this.lx = lx;
        this.title = title;
        this.type = type;
        this.fpc = fpc;

        defaultDirectory = Paths.get(System.getProperty("user.dir"));
        currentPath = defaultDirectory;
        currentPathString = currentPath.toString();
    }

    @Override
    public void draw() {
        if (!opened) {
            UI.openPopup(title);
        }
        if (!UI.beginPopup(title)) {
            WindowManager.closeWindow(this);
            return;
        }
        currentPathString = UI.inputText("##file", currentPathString);
        currentPath = Paths.get(currentPathString);

        UI.sameLine();
        if (UI.button(type == FileDialog.Type.OPEN ? "Open" : "Save")) {
            lx.engine.addTask(() -> fpc.onFilePicked(currentPath.toFile()));
            WindowManager.closeWindow(this);
            UI.endPopup();
            return;
        }
        UI.sameLine();
        if (UI.button("Cancel")) {
            WindowManager.closeWindow(this);
            UI.endPopup();
            return;
        }

        UI.beginChild("dir-tree", true, 0, 400, 600);
        showDir(Paths.get("/"));
        UI.endChild();
        UI.endPopup();
    }

    private void showDir(Path path) {
        String name = path.getFileName() == null ? "/" : path.getFileName().toString();
        int flags = 0;
        if (defaultDirectory.equals(path) || defaultDirectory.startsWith(path)) {
            flags |= UI.TREE_FLAG_DEFAULT_OPEN;
        }
        if (currentPath.equals(path)) {
            flags |= UI.TREE_FLAG_SELECTED;
        }
        if (UI.treeNode(path.toString(), flags, name)) {
            try {
                Files.list(path)
                    .sorted((p1, p2) -> p1.toString().compareToIgnoreCase(p2.toString()))
                    .forEach(p -> {
                            if (Files.isDirectory(p)) {
                                showDir(p);
                            } else {
                                showFile(p);
                            }
                        });
            } catch (IOException e) {}
            UI.treePop();
        }
    }

    private void showFile(Path path) {
        int flags = UI.TREE_FLAG_LEAF;
        if (currentPath.equals(path)) {
            flags |= UI.TREE_FLAG_SELECTED;
        }
        UI.treeNode(path.toString(), flags, path.getFileName().toString());
        if (UI.isItemClicked()) {
            currentPath = path;
            currentPathString = currentPath.toString();
        }
        UI.treePop();
    }
}
