package heronarts.lx.data;

import heronarts.lx.LX.ProjectListener;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXSerializable;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;


public class LegacyProjectLoader {
    /* file version numbers used to be stored as non-integer strings, we treat this string like version 0. */
    private static final String INITIAL_LEGACY_PROJECT_VERSION_STRING = "0.1";

    public static final int CURRENT_VERSION = 1;

    private final static String KEY_VERSION = "version";
    private final static String KEY_TIMESTAMP = "timestamp";
    private final static String KEY_ENGINE = "engine";
    private final static String KEY_EXTERNALS = "externals";

    private static JsonObject upgradeVersion(JsonObject obj) {
        // TODO
        return obj;
    }

    public static void load(Project project, LX lx) {
        Preconditions.checkArgument(project.isLegacyProject());
        Path projectFilePath = project.getAll(ProjectFileType.LegacyProjectFile).get(0);
        File file = projectFilePath.toFile();

        try {
            FileReader fr = null;
            try {
                fr = new FileReader(file);
                JsonObject obj = new Gson().fromJson(fr, JsonObject.class);
                obj = upgradeVersion(obj);

                lx.componentRegistry.resetProject();
                lx.componentRegistry.setIdCounter(getMaxId(obj, lx.componentRegistry.getIdCounter()) + 1);
                lx.engine.load(lx, obj.getAsJsonObject(KEY_ENGINE));
                if (obj.has(KEY_EXTERNALS)) {
                    JsonObject externalsObj = obj.getAsJsonObject(KEY_EXTERNALS);
                    Map<String, LXSerializable> externals = lx.getExternals();
                    for (String key : externals.keySet()) {
                        if (externalsObj.has(key)) {
                            externals.get(key).load(lx, externalsObj.getAsJsonObject(key));
                        }
                    }
                }
                lx.setProject(project, ProjectListener.Change.OPEN);
                System.out.println("Project loaded successfully from " + file.toString());
            } catch (IOException iox) {
                System.err.println("Could not load project file: " + iox.getLocalizedMessage());
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException ignored) {}
                }
            }
        } catch (Exception x) {
            System.err.println("Exception in loadProject: " + x.getLocalizedMessage());
            x.printStackTrace(System.err);
        }
    }

    public static void save(Project project, LX lx) {
        Preconditions.checkArgument(project.isLegacyProject());
        Path projectFilePath = project.getAll(ProjectFileType.LegacyProjectFile).get(0);
        File file = projectFilePath.toFile();

        JsonObject obj = new JsonObject();
        obj.addProperty(KEY_VERSION, "0.1");
        obj.addProperty(KEY_TIMESTAMP, System.currentTimeMillis());
        obj.add(KEY_ENGINE, LXSerializable.Utils.toObject(lx, lx.engine));
        JsonObject externalsObj = new JsonObject();
        Map<String, LXSerializable> externals = lx.getExternals();
        for (String key : externals.keySet()) {
            externalsObj.add(key, LXSerializable.Utils.toObject(lx, externals.get(key)));
        }
        obj.add(KEY_EXTERNALS, externalsObj);
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(file));
            writer.setIndent("  ");
            new GsonBuilder().create().toJson(obj, writer);
            writer.close();
            System.out.println("Project saved successfully to " + file.toString());
            lx.componentRegistry.resetProject();
            lx.setProject(project, ProjectListener.Change.SAVE);
        } catch (IOException iox) {
            System.err.println(iox.getLocalizedMessage());
        }
    }

    private static int getMaxId(JsonObject obj, int max) {
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            if (entry.getKey().equals(LXComponent.KEY_ID)) {
                int id = entry.getValue().getAsInt();
                if (id > max) {
                    max = id;
                }
            } else if (entry.getValue().isJsonArray()) {
                for (JsonElement arrElement : entry.getValue().getAsJsonArray()) {
                    if (arrElement.isJsonObject()) {
                        max = getMaxId(arrElement.getAsJsonObject(), max);
                    }
                }
            } else if (entry.getValue().isJsonObject()) {
                max = getMaxId(entry.getValue().getAsJsonObject(), max);
            }
        }
        return max;
    }
}
