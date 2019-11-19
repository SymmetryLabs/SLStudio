package com.symmetrylabs.util.persistance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import java.io.*;

public class ClassWriterLoader <T> {
    private final static String RESOURCES_DIR = "src/main/resources";
    private final String persistFilePath;
    private final Class<T> tClass;

    public ClassWriterLoader(String persistFilePath, Class<T> tClass) {
        this.persistFilePath = persistFilePath;
        this.tClass = tClass;
    }

    public void writeObj(T instanceToSave) throws IOException {
        File resFile = new File(RESOURCES_DIR, persistFilePath);
        JsonWriter writer = new JsonWriter(new FileWriter(resFile));
        writer.setIndent("  ");

        new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(instanceToSave, tClass, writer);
        writer.close();
        System.out.println("inventory file writen " + resFile);
    }

    public T loadObj(){
        ClassLoader cl = tClass.getClassLoader();
        InputStream resourceStream = cl.getResourceAsStream(persistFilePath);
        if (resourceStream != null) {
            return new Gson().fromJson(new InputStreamReader(resourceStream), tClass);
        }
        return null; // didn't work
    }
}
