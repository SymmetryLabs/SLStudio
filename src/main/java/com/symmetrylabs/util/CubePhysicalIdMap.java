package com.symmetrylabs.util;

import com.google.common.base.Preconditions;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.symmetrylabs.slstudio.ApplicationState;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import com.google.gson.stream.JsonReader;
import java.io.FileReader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import com.google.gson.JsonElement;
import java.io.InputStreamReader;
import java.io.InputStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CubePhysicalIdMap {
    protected final static String LEGACY_FILENAME = "physid_to_mac.json";
    protected final static String CUBEDB_FILENAME = "cubedb.json";

    protected final static Predicate<String> MAC_ADDR_PATTERN = Pattern.compile("[A-Fa-f0-9]{12}").asPredicate();

    public interface Listener {
        void onCubeListUpdated();
    }

    public static class CubeDataError extends RuntimeException {
        public CubeDataError(String err) {
            super(err);
        }

        static void require(boolean check, String msg, Object... args) {
            if (!check) {
                throw new CubeDataError(String.format(msg, args));
            }
        }
    }

    public static class PhysicalCube {
        public String addrA;
        public String idA;
        public String addrB;
        public String idB;

        /** true if this is a legacy import cube */
        public boolean imported;

        public String getPhysicalId() {
            return idA;
        }

        public Collection<String> getControllerIds() {
            List<String> res = new ArrayList<>();
            res.add(idA);
            if (idB != null) {
                res.add(idB);
            }
            return res;
        }

        public Collection<String> getControllerAddrs() {
            List<String> res = new ArrayList<>();
            res.add(addrA);
            if (addrB != null) {
                res.add(addrB);
            }
            return res;
        }

        @Override
        public String toString() {
            if (idB != null) {
                return String.format("%s (%s:%s %s:%s)%s", idA, idA, addrA, idB, addrB, imported ? " [IMPORTED]" : "");
            }
            return String.format("%s (%s:%s) %s", idA, idA, addrA, imported ? " [IMPORTED]" : "");
        }

        void validate() {
            CubeDataError.require(idA != null, "idA is null");
            CubeDataError.require(addrA != null, "addrA is null on cube %s", idA);
            CubeDataError.require(MAC_ADDR_PATTERN.test(addrA), "bad format for mac address A on cube %s: \"%s\"", idA, addrA);
            // both or neither of idB and addrB must be null
            CubeDataError.require((addrB == null) == (idB == null), "only one of idB and addrB were set on cube id %s", idA);
            if (addrB != null) {
                CubeDataError.require(MAC_ADDR_PATTERN.test(addrB), "bad format for mac address B on cube %s: \"%s\"", idA, addrB);
            }
        }
    }

    public final List<PhysicalCube> allCubes;

    /* the entire state of the class is built from the single persistent field allCubes */
    public final transient Map<String, PhysicalCube> cubeByMacAddrs = new HashMap<>();
    public final transient Map<String, PhysicalCube> cubeByPhysId = new HashMap<>();
    public final transient Set<String> unknownMacAddrs = new HashSet<>();
    private final transient List<Listener> listeners = new ArrayList<>();
    private transient String cubeErrors;
    private transient String missingMacAddrErrors;

    protected CubePhysicalIdMap() {
        allCubes = new ArrayList<PhysicalCube>();
    }

    protected CubePhysicalIdMap(List<PhysicalCube> allCubes) {
        this.allCubes = allCubes;
        rebuild();
    }

    public String getErrors() {
        if (missingMacAddrErrors == null) return cubeErrors;
        if (cubeErrors == null) return missingMacAddrErrors;
        return cubeErrors + "\n" + missingMacAddrErrors;
    }

    private void onErrorMessagesUpdated() {
        ApplicationState.setWarning("CubePhysicalIdMap", getErrors());
    }

    public void rebuild() {
        cubeByMacAddrs.clear();
        cubeByPhysId.clear();
        unknownMacAddrs.clear();
        cubeErrors = null;
        missingMacAddrErrors = null;

        StringBuilder errs = new StringBuilder();
        for (PhysicalCube cube : allCubes) {
            try {
                cube.validate();
                for (String id : cube.getControllerIds()) {
                    CubeDataError.require(
                        !cubeByPhysId.containsKey(id), "ID %s duplicated on cube %s and %s",
                        id, cubeByPhysId.get(id), cube);
                    cubeByPhysId.put(id, cube);
                }
                for (String mac : cube.getControllerAddrs()) {
                    CubeDataError.require(
                        !cubeByMacAddrs.containsKey(mac), "MAC %s duplicated on cube %s and %s",
                        mac, cubeByMacAddrs.get(mac), cube);
                    cubeByMacAddrs.put(mac, cube);
                }
            } catch (CubeDataError e) {
                errs.append(e.getMessage() + "\n");
            }
        }
        cubeErrors = errs.toString().trim();
        if (cubeErrors.length() == 0) {
            cubeErrors = null;
        }
        onErrorMessagesUpdated();
        onUpdated();
    }

    private void onUpdated() {
        for (Listener l : listeners) {
            l.onCubeListUpdated();
        }
    }

    public void save() {
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(CUBEDB_FILENAME));
            writer.setIndent("  ");
            new GsonBuilder().create().toJson(this, CubePhysicalIdMap.class, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public PhysicalCube getCube(String macAddr) {
        PhysicalCube cube = cubeByMacAddrs.get(macAddr);
        if (cube != null) {
            return cube;
        }
        if (!unknownMacAddrs.contains(macAddr)) {
            unknownMacAddrs.add(macAddr);
            missingMacAddrErrors = "No cube registered in inventory for discovered MAC addresses: " + String.join(", ", unknownMacAddrs);
            onErrorMessagesUpdated();
        }
        return null;
    }

    public String getPhysicalId(String macAddr) {
        PhysicalCube cube = getCube(macAddr);
        if (cube != null) {
            return cube.getPhysicalId();
        }
        return macAddr;
    }

    public static CubePhysicalIdMap loadFromDisk() {
        ClassLoader cl = CubePhysicalIdMap.class.getClassLoader();
        InputStream cubedbStream = cl.getResourceAsStream(CUBEDB_FILENAME);
        if (cubedbStream != null) {
            CubePhysicalIdMap res = new Gson().fromJson(
                new InputStreamReader(cubedbStream), CubePhysicalIdMap.class);
            res.rebuild();
            return res;
        }

        InputStream legacyFileStream = cl.getResourceAsStream(LEGACY_FILENAME);
        if (legacyFileStream == null) {
            return new CubePhysicalIdMap();
        }

        List<PhysicalCube> cubes = new ArrayList<>();
        JsonObject legacyRecords = new Gson().fromJson(
            new InputStreamReader(legacyFileStream), JsonObject.class);
        for (Map.Entry<String, JsonElement> elem : legacyRecords.entrySet()) {
            PhysicalCube c = new PhysicalCube();
            c.idA = elem.getKey();
            c.addrA = elem.getValue().getAsString();
            c.imported = true;
            cubes.add(c);
        }
        return new CubePhysicalIdMap(cubes);
    }
}
