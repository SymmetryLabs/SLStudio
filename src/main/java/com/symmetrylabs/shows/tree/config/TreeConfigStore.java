package com.symmetrylabs.shows.tree.config;

import java.io.*;

import com.symmetrylabs.slstudio.SLStudioLX;

import com.google.gson.Gson;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.util.FileUtils;

public class TreeConfigStore implements SLStudioLX.SaveHook {
    private final TreeModel tree;
    private File file = null;
    private TreeConfig config = null;

    public TreeConfigStore(LX lx) {
        this(lx, true);
    }

    public TreeConfigStore(LX lx, boolean loadSavedConfig) {
        this.tree = (TreeModel) lx.model;
        if (loadSavedConfig) {
            loadConfig();
        }
    }

    private void loadConfig() {
        File file = getConfigFile();
        if (!file.exists()) {
            file = createConfigFile();
        }

        this.file = file;
        TreeConfig configIn = FileUtils.readJson(file, TreeConfig.class);
        this.config = configIn == null ? this.config : configIn;
        if (config != null) reconfigureTree(config);
    }

    private File createConfigFile() {
        File file = getConfigFile();
        try {
            writeConfig(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private File getConfigFile() {
        return FileUtils.getShowFile("tree-config.json");
    }

    private void reconfigureTree(TreeConfig config) {
        tree.reconfigure(config);
    }

    @Override
    public void onSave() {
        writeConfig();
    }

    public void writeConfig() {
        writeConfig(file);
    }

    private void writeConfig(File file) {
        FileUtils.writeJson(getConfigFile(), tree.getConfig());
    }
}
