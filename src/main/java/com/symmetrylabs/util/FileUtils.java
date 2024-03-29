package com.symmetrylabs.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.ui.v2.NotImplementedInV2Exception;
import com.symmetrylabs.slstudio.ApplicationState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;

import de.javagl.obj.ObjReader;
import de.javagl.obj.ReadableObj;
import processing.core.PImage;

public class FileUtils {
    /** Gets the path to a file in the show directory for the current show. */
    public static String getShowPath(String filename) {
        return Utils.sketchPath(
            "shows/" + ApplicationState.showName() + "/" + filename
        );
    }

    /** Gets a File in the show directory for the current show. */
    public static File getShowFile(String filename) {
        return new File(getShowPath(filename));
    }

    /** Gets the path to a file, relative to the sketch's root directory. */
    public static String getRelativePath(File file) {
        String sketchPath = Utils.sketchPath();
        String path = file.getAbsolutePath();
        if (path.substring(0, sketchPath.length()).equals(sketchPath)) {
            path = path.substring(sketchPath.length());
        }
        return path;
    }

    /**
     * Reads a JSON file in the show directory for the current show,
     * without complaining if the file is missing.
     */
    public static <T> T readShowJsonIfExists(String filename, Class<T> type) {
        return readJsonIfExists(getShowFile(filename), type);
    }

    /** Reads a JSON file in the show directory for the current show. */
    public static <T> T readShowJson(String filename, Class<T> type) {
        return readJson(getShowFile(filename), type);
    }

    /** Reads a JSON file, without complaining if the file is missing. */
    public static <T> T readJsonIfExists(File file, Class<T> type) {
        return file.exists() ? readJson(file, type) : null;
    }

    /** Reads a JSON file, with error handling and reporting. */
    public static <T> T readJson(File file, Class<T> type) {
        FileReader reader = null;
        try {
            reader = new FileReader(file);
            T result = new Gson().fromJson(reader, type);
            System.out.println("Read JSON file: " + getRelativePath(file));
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            ApplicationState.setWarning(getRelativePath(file), "Could not read JSON file: " + e.getMessage());
            return null;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            String message = (e.getCause() != null ? e.getCause() : e).getMessage();
            ApplicationState.setWarning(getRelativePath(file), "JSON syntax error: " + message);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) { }
            }
        }
    }

    /** Reads a JSON file in the resources directory, with error handling and reporting. */
    public static <T> T readResourceJson(String filename, Class<T> type) {
        // createInput() will search a few places, catch exceptions, and print their stack traces.
        InputStream stream = Utils.createInput(filename);
        if (stream == null) {
            ApplicationState.setWarning(filename, "Resource file not found");
            return null;
        }
        try {
            T result = new Gson().fromJson(new InputStreamReader(stream), type);
            System.out.println("Read JSON resource: " + filename);
            return result;
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            String message = (e.getCause() != null ? e.getCause() : e).getMessage();
            ApplicationState.setWarning(filename, "JSON syntax error in resource: " + message);
            return null;
        } finally {
            try {
                stream.close();
            } catch (IOException e) { }
        }
    }

    /** Writes a JSON file in the show directory for the current show. */
    public static void writeShowJson(String filename, Object obj) {
        writeJson(getShowFile(filename), obj);
    }

    /** Writes a JSON file, with error handling and reporting. */
    public static void writeJson(File file, Object obj) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            new GsonBuilder().setPrettyPrinting().create().toJson(obj, writer);
            System.out.println("Wrote JSON: " + getRelativePath(file));
        } catch (IOException e) {
            e.printStackTrace();
            ApplicationState.setWarning(getRelativePath(file), "Could not read JSON file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) { }
            }
        }
    }

    /** Reads an OBJ file in the show directory for the current show. */
    public static ReadableObj readShowObj(String filename) {
        return readObj(getShowFile(filename));
    }

    /** Reads an OBJ file, with error handling and reporting. */
    public static ReadableObj readObj(File file) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            ReadableObj result = ObjReader.read(stream);
            System.out.println("Read OBJ file: " + getRelativePath(file));
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            ApplicationState.setWarning(getRelativePath(file), "Could not read OBJ file: " + e.getMessage());
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) { }
            }
        }
    }

    /** Reads a text file in the show directory for the current show. */
    public static List<String> readShowLines(String filename) {
        return readLines(getShowFile(filename));
    }

    /** Reads a text file, with error handling and reporting. */
    public static List<String> readLines(File file) {
        try {
            List<String> result = Files.readAllLines(file.toPath());
            System.out.println("Read text file: " + getRelativePath(file));
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            ApplicationState.setWarning(getRelativePath(file), "Could not read text file: " + e.getMessage());
            return null;
        }
    }

    /** Loads an image from the show directory for the current show. */
    public static PImage loadShowImage(String filename) {
        if (SLStudio.applet == null) {
            throw new NotImplementedInV2Exception("loadShowImage can only be called in the Processing UI");
        }
        return SLStudio.applet.loadImage(getShowPath(filename));
    }
}
