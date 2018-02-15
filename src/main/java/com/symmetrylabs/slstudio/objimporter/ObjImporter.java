package com.symmetrylabs.slstudio.objimporter;

import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXTransform;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static processing.core.PApplet.println;
import static processing.core.PConstants.PI;

public class ObjImporter {

    private final String path;

    private final ObjConfigReader configReader;

    private final ObjConfig config;

    private final List<LXModel> models = new ArrayList<LXModel>();

    public ObjImporter(String path, LXTransform transform) {
        this.path = path;
        this.configReader = new ObjConfigReader(this.path);
        this.config = configReader.readConfig("global");

        if (config.enabled) {
            buildModels(transform);
        }
    }

    public List<LXModel> getModels() {
        return models;
    }

    private void buildModels(LXTransform transform) {
        transform.push();
        transform.translate(config.x, config.y, config.z);
        transform.rotateX(config.xRotation * PI / 180f);
        transform.rotateY(config.yRotation * PI / 180f);
        transform.rotateZ(config.zRotation * PI / 180f);

        for (File file : loadObjFiles()) {
            try {
                ObjModelBuilder objModelBuilder = new ObjModelBuilder(file, configReader);

                if (objModelBuilder.config.enabled) {
                    this.models.add(objModelBuilder.buildModel(transform));
                }
            } catch (Exception e) {
                println("Problem with obj file: " + file.getName());
            }
        }

        transform.pop();
    }

    private List<File> loadObjFiles() {
        File[] directory = new File(path).listFiles();
        List<File> objFiles = new ArrayList<File>();

        for (File file : directory) {
            if (file.getName().matches("([^\\s]+(\\.(?i)(obj))$)")) {

                // don't allow an obj fixture called 'global'
                if (!file.getName().equals("global.obj")) {
                    objFiles.add(file);
                }
            }
        }

        return objFiles;
    }
}
