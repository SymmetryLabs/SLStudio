package com.symmetrylabs.slstudio.objimporter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import com.symmetrylabs.util.Utils;

public class ObjConfigReader {

    private final String CONFIG_FILENAME = "objConfig.txt";

    private String path;

    public ObjConfigReader(String path) {
        this.path = path;
    }

    public ObjConfig readConfig(String fileName) {
        ObjConfig config = new ObjConfig();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    Utils.createInput(path + "/" + CONFIG_FILENAME)));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(fileName + ".enabled")) config.enabled = line.contains("true");
                else if (line.contains(fileName + ".x")) config.x = extractFloat(line);
                else if (line.contains(fileName + ".y")) config.y = extractFloat(line);
                else if (line.contains(fileName + ".z")) config.z = extractFloat(line);
                else if (line.contains(fileName + ".rotateX")) config.xRotation = extractFloat(line);
                else if (line.contains(fileName + ".rotateY")) config.yRotation = extractFloat(line);
                else if (line.contains(fileName + ".rotateZ")) config.zRotation = extractFloat(line);
                else if (line.contains(fileName + ".scale")) config.scale = extractFloat(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return config;
    }

    public float extractFloat(String line) {

        String str = line.replaceAll("[^-+.0123456789]", "");
        if (str.startsWith("")) {
            str = str.substring(1);
        }

        return Float.valueOf(str);
    }
}
