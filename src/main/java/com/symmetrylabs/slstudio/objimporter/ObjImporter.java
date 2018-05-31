package com.symmetrylabs.slstudio.objimporter;

import java.io.*;
import java.util.*;

import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.slstudio.model.SLModel;
import static com.symmetrylabs.util.MathConstants.*;


public class ObjImporter {

    private final String path;

    private final ObjConfigReader configReader;

    private final ObjConfig config;

    private final List<SLModel> models = new ArrayList<SLModel>();

    public ObjImporter(String path, LXTransform transform) {
        this.path = path;
        this.configReader = new ObjConfigReader(this.path);
        this.config = configReader.readConfig("global");

        if (config.enabled) {
            buildModels(transform);
        }
    }

    public SLModel[] getModels() {
        return (SLModel[]) models.toArray(new SLModel[models.size()]);
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
                System.out.println("Problem with obj file: " + file.getName());
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
