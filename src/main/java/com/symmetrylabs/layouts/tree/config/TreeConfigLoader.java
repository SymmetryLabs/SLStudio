package com.symmetrylabs.layouts.tree.config;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.layouts.tree.*;


public class TreeConfigLoader extends LXComponent {

    private static final String KEY = "treeConfiguration";

    private final TreeModel tree;

    private File file = null;

    private TreeConfig config = null;

    public TreeConfigLoader(LX lx) {
        super(lx);
        this.tree = (TreeModel)lx.model;
        //loadConfig();
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
            // if (file.createNewFile()) {
            //   System.out.println("TreeConfig: " + getConfigFilePath() + " already exists");
            // } else {
            //   writeConfig(file);
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private String getConfigFilePath() {
        return "data/" + SLStudio.applet.getSelectedLayoutName() + "-tree-config.json";
    }

    private void reconfigureTree(TreeConfig config) {
        tree.reconfigure(config);
    }

    @Override
    public void save(LX lx, JsonObject obj) {
        writeConfig();
    }

    private void writeConfig() {
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

    public static boolean isTreeLayout(Layout layout) {
        return layout instanceof TreeLayout;
    }
}