package com.symmetrylabs.util;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.symmetrylabs.slstudio.ApplicationState;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.apache.commons.collections4.iterators.IteratorChain;
import java.net.URL;

public class CubeInventory {
    protected final static String LEGACY_FILENAME = "physid_to_mac.json";
    protected final static String INVENTORY_FILENAME = "cubeinventory.json";
    protected final static boolean ALLOW_LEGACY_LOADER = false;

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
    private transient List<String> cubeErrors = new ArrayList<>();
    private transient List<String> missingMacAddrErrors = new ArrayList<>();

    protected CubeInventory() {
        allCubes = new ArrayList<PhysicalCube>();
    }

    protected CubeInventory(List<PhysicalCube> allCubes) {
        this.allCubes = allCubes;
        rebuild();
    }

    public Iterator<CharSequence> getErrors() {
        IteratorChain<CharSequence> iter = new IteratorChain<>();
        iter.addIterator(cubeErrors.iterator());
        iter.addIterator(missingMacAddrErrors.iterator());
        return iter;
    }

    public String getErrorString() {
        String res = String.join("\n", () -> getErrors());
        if (res.length() == 0) {
            return null;
        }
        return res;
    }

    private void onErrorMessagesUpdated() {
        ApplicationState.setWarning("CubeInventory", getErrorString());
    }

    public void rebuild() {
        cubeByMacAddrs.clear();
        cubeByPhysId.clear();
        unknownMacAddrs.clear();
        cubeErrors.clear();
        missingMacAddrErrors.clear();

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
                cubeErrors.add(e.getMessage());
            }
        }
        onErrorMessagesUpdated();
        onUpdated();
    }

    private void onUpdated() {
        for (Listener l : listeners) {
            l.onCubeListUpdated();
        }
    }

    public boolean save() {
        ClassLoader cl = CubeInventory.class.getClassLoader();
        /* check to see if we have the file in resources */
        URL dbUrl = cl.getResource(INVENTORY_FILENAME);
        /* this is where the file is stored in the source tree */
        File resFile = new File("src/main/resources", INVENTORY_FILENAME);
        /* if the source tree file isn't present but the resource is present, we aren't in a
           source distribution, so we can't save the file. */
        if (dbUrl != null && !resFile.exists()) {
            System.err.println(
                String.format(
                    "This build of Volume cannot save cube inventory files (resources are loaded from %s)",
                    dbUrl));
            return false;
        }
        try {
            JsonWriter writer = new JsonWriter(new FileWriter(resFile));
            writer.setIndent("  ");
            new GsonBuilder().create().toJson(this, CubeInventory.class, writer);
            writer.close();
            System.out.println("cube inventory written to " + resFile);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
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
            missingMacAddrErrors.add("No cube in inventory for discovered MAC address " + macAddr);
            onErrorMessagesUpdated();
        }
        return null;
    }

    public String getControllerId(String macAddr) {
        PhysicalCube cube = getCube(macAddr);
        if (cube != null) {
            return macAddr.equals(cube.addrA) ? cube.idA : cube.idB;
        }
        return macAddr;
    }

    public static CubeInventory loadFromDisk() {
        ClassLoader cl = CubeInventory.class.getClassLoader();
        InputStream cubedbStream = cl.getResourceAsStream(INVENTORY_FILENAME);
        if (cubedbStream != null) {
            CubeInventory res = new Gson().fromJson(
                new InputStreamReader(cubedbStream), CubeInventory.class);
            if (res != null) {
                res.rebuild();
                return res;
            }
        }
        if (!ALLOW_LEGACY_LOADER) {
            ApplicationState.setWarning("CubeInventory", "couldn't read inventory from disk");
            return new CubeInventory();
        }

        InputStream legacyFileStream = cl.getResourceAsStream(LEGACY_FILENAME);
        if (legacyFileStream == null) {
            return new CubeInventory();
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
        return new CubeInventory(cubes);
    }
}
