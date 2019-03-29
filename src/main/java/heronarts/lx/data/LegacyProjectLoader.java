package heronarts.lx.data;

import com.google.gson.*;
import heronarts.lx.LX.ProjectListener;
import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXSerializable;

import java.util.IllegalFormatException;
import com.symmetrylabs.slstudio.ApplicationState;

import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;


public class LegacyProjectLoader {
    public static class IllegalVersionException extends RuntimeException {
        public IllegalVersionException(String version) {
            super(String.format("unknown version \"%s\"", version));
        }
    }

    /* file version numbers used to be stored as non-integer strings, we treat this string like version 0. */
    private static final String INITIAL_LEGACY_PROJECT_VERSION_STRING = "0.1";

    private static final String KEY_VERSION = "version";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_ENGINE = "engine";
    private static final String KEY_EXTERNALS = "externals";

    private static final String[] KEYS_MOVED_FROM_ENGINE_TO_LOOK = new String[] {
        "crossfader", "crossfaderBlendMode", "focusedChannel", "cueA", "cueB" };

    private static void moveJsonSubtree(JsonObject origRoot, JsonObject newRoot, String key) {
        if (!origRoot.has(key)) {
            return;
        }
        JsonElement subtree = origRoot.get(key);
        origRoot.remove(key);
        newRoot.add(key, subtree);
    }

    private static JsonObject upgradeVersion(int fileVersion, int runtimeVersion, JsonObject obj) {
        /* move LXEngine parameters (and all channels) onto a single LXLook when upgrading from pre-look to post-look */
        if (fileVersion < LXVersions.SLSTUDIO_WITH_LOOKS && runtimeVersion >= LXVersions.SLSTUDIO_WITH_LOOKS) {
            JsonObject engine = obj.getAsJsonObject("engine");
            JsonObject engineParams = engine.getAsJsonObject("parameters");

            JsonObject look = new JsonObject();
            moveJsonSubtree(engine, look, "channels");

            JsonObject lookParams = new JsonObject();
            look.add("parameters", lookParams);
            for (String param : KEYS_MOVED_FROM_ENGINE_TO_LOOK) {
                moveJsonSubtree(engineParams, lookParams, param);
            }

            JsonArray looks = new JsonArray();
            looks.add(look);
            engine.add("looks", looks);

            int newVersion = fileVersion == LXVersions.SLSTUDIO_ORIG
                ? LXVersions.SLSTUDIO_WITH_LOOKS : LXVersions.VOLUME_WITH_LOOKS;
            obj.addProperty(KEY_VERSION, newVersion);
            System.out.println(String.format("upgraded project file from v%d to v%d", fileVersion, newVersion));
        }
        try {
            JsonWriter writer = new JsonWriter(new FileWriter("upgraded.lxp"));
            writer.setIndent("  ");
            new GsonBuilder().create().toJson(obj, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private static JsonObject checkAndUpgradeVersion(int runtimeVersion, JsonObject obj) {
        ApplicationState.setWarning("ProjectLoader", null);
        JsonPrimitive versionElem = obj.getAsJsonPrimitive(KEY_VERSION);
        int fileVersion;

        if (versionElem.isString()) {
            if (versionElem.getAsString().equals(INITIAL_LEGACY_PROJECT_VERSION_STRING)) {
                fileVersion = 0;
            } else {
                throw new IllegalVersionException(versionElem.getAsString());
            }
        } else if (versionElem.isNumber()) {
            fileVersion = versionElem.getAsInt();
        } else {
            throw new IllegalVersionException(versionElem.toString());
        }

        /* if we have the same version, no problem */
        if (fileVersion == runtimeVersion) {
            return obj;
        }
        /* if it's an old file, we can bring it up to speed */
        if (fileVersion < runtimeVersion) {
            return upgradeVersion(fileVersion, runtimeVersion, obj);
        }
        /* if it's a newer file than our runtime, put up a warning and just hope for the best. */
        ApplicationState.setWarning(
            "ProjectLoader",
            "Project file was made for a newer version of Symmetry software, not all features may work as expected");
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
                obj = checkAndUpgradeVersion(project.getRuntimeVersion(), obj);

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
        obj.addProperty(KEY_VERSION, project.getRuntimeVersion());
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
