package com.symmetrylabs.shows.empirewall.config;

import java.io.*;

import com.symmetrylabs.slstudio.SLStudioLX;

import com.google.gson.Gson;

import heronarts.lx.LX;

import com.symmetrylabs.shows.empirewall.VineModel;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.shows.tree.TreeModel;
import com.symmetrylabs.util.FileUtils;

public class VineWallConfigStore implements SLStudioLX.SaveHook {
    private final VineModel model;
    private File file = null;
    private VineWallConfig config = null;

    public VineWallConfigStore(LX lx) {
        this(lx, true);
    }

    public VineWallConfigStore(LX lx, boolean loadSavedConfig) {
        this.model = (VineModel) lx.model;
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
        this.config = FileUtils.readJson(file, VineWallConfig.class);
        if (config != null) reconfigureModel(config);
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
        return FileUtils.getShowFile("vine-wall-config.json");
    }

    private void reconfigureModel(VineWallConfig config) {
        model.reconfigure(config);
    }

    @Override
    public void onSave() {
        writeConfig();
    }

    public void writeConfig() {
        writeConfig(file);
    }

    private void writeConfig(File file) {
        FileUtils.writeJson(getConfigFile(), model.getConfig());
    }
}
