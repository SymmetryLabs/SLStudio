package com.symmetrylabs.shows.tree.config;

import java.io.*;

import com.google.gson.Gson;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.shows.tree.TreeModel;


public class TreeConfigStore {

    private final TreeModel tree;

    private File file = null;

    private TreeConfig config = null;

    public TreeConfigStore(LX lx) {
        this.tree = (TreeModel)lx.model;
        loadConfig();
    }

    private void loadConfig() {
        File file = new File(getConfigFilePath());

        if (!file.exists()) {
            file = createConfigFile();
        }

        try {
            this.file = file;
            this.config = new Gson().fromJson(new FileReader(file), TreeConfig.class);
            reconfigureTree(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File createConfigFile() {
        File file = new File(getConfigFilePath());

        try {
            writeConfig(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private String getConfigFilePath() {
        return "data/" + SLStudio.applet.getSelectedShowName() + "-tree-config.json";
    }

    private void reconfigureTree(TreeConfig config) {
        tree.reconfigure(config);
    }

    public void writeConfig() {
        writeConfig(file);
    }

    private void writeConfig(File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(new Gson().toJson(tree.getConfig()));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
