package com.symmetrylabs.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.symmetrylabs.slstudio.ApplicationState;
import org.apache.commons.collections4.iterators.IteratorChain;

import javax.naming.ldap.Control;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class SLInventory {
    protected final static String LEGACY_FILENAME = "physid_to_mac.json";
    protected final static String INVENTORY_FILENAME = "cubeinventory.json";
    protected final static boolean ALLOW_LEGACY_LOADER = false;

    protected final static Predicate<String> MAC_ADDR_PATTERN = Pattern.compile("[A-Fa-f0-9]{12}").asPredicate();

    public interface Listener {
        void onControllerListUpdated();
    }

    public static class ControllerDataError extends RuntimeException {
        public ControllerDataError(String err) {
            super(err);
        }

        static void require(boolean check, String msg, Object... args) {
            if (!check) {
                throw new ControllerDataError(String.format(msg, args));
            }
        }
    }

    public static class PhysicalFixture {
        public String addrA;
        public String idA;

        /** true if this is a legacy import cube */
        public boolean imported;

        public String getPhysicalId() {
            return idA;
        }

        public Collection<String> getControllerIds() {
            List<String> res = new ArrayList<>();
            res.add(idA);
            return res;
        }

        public Collection<String> getControllerAddrs() {
            List<String> res = new ArrayList<>();
            res.add(addrA);
            return res;
        }

        @Override
        public String toString() {
            return String.format("%s (%s:%s %s:%s)%s", idA, idA, addrA, imported ? " [IMPORTED]" : "");
        }

        void validate() {
            ControllerDataError.require(idA != null, "idA is null");
            ControllerDataError.require(addrA != null, "addrA is null on cube %s", idA);
            ControllerDataError.require(MAC_ADDR_PATTERN.test(addrA), "bad format for mac address A on cube %s: \"%s\"", idA, addrA);
        }
    }

    public final List<PhysicalFixture> allFixtures;

    /* the entire state of the class is built from the single persistent field allCubes */
    public final transient Map<String, PhysicalFixture> cubeByMacAddrs = new HashMap<>();
    public final transient Map<String, PhysicalFixture> cubeByCtrlId = new HashMap<>();
    public final transient Set<String> unknownMacAddrs = new HashSet<>();
    private final transient List<Listener> listeners = new ArrayList<>();
    private transient List<String> cubeErrors = new ArrayList<>();
    private transient List<String> missingMacAddrErrors = new ArrayList<>();

    protected SLInventory() {
        allFixtures = new ArrayList<PhysicalFixture>();
    }

    protected SLInventory(List<PhysicalFixture> allCubes) {
        this.allFixtures = allCubes;
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

    public PhysicalFixture lookUpByPhysId(String physId) {
        return cubeByCtrlId.get(physId);
    }

    public void rebuild() {
        cubeByMacAddrs.clear();
        cubeByCtrlId.clear();
        unknownMacAddrs.clear();
        cubeErrors.clear();
        missingMacAddrErrors.clear();

        for (PhysicalFixture cube : allFixtures) {
            try {
                cube.validate();
                for (String id : cube.getControllerIds()) {
                    ControllerDataError.require(
                        !cubeByCtrlId.containsKey(id), "ID %s duplicated on cube %s and %s",
                        id, cubeByCtrlId.get(id), cube);
                    cubeByCtrlId.put(id, cube);
                }
                for (String mac : cube.getControllerAddrs()) {
                    ControllerDataError.require(
                        !cubeByMacAddrs.containsKey(mac), "MAC %s duplicated on cube %s and %s",
                        mac, cubeByMacAddrs.get(mac), cube);
                    cubeByMacAddrs.put(mac, cube);
                }
            } catch (ControllerDataError e) {
                cubeErrors.add(e.getMessage());
            }
        }
        onErrorMessagesUpdated();
        onUpdated();
    }

    private void onUpdated() {
        for (Listener l : listeners) {
            l.onControllerListUpdated();
        }
    }

    public boolean save() {
        ClassLoader cl = SLInventory.class.getClassLoader();
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
            new GsonBuilder().create().toJson(this, SLInventory.class, writer);
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

    public PhysicalFixture getCube(String macAddr) {
        PhysicalFixture cube = cubeByMacAddrs.get(macAddr);
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
        PhysicalFixture slFixture = getCube(macAddr);
        if (slFixture != null) {
            return slFixture.addrA;
        }
        return macAddr;
    }

    public static SLInventory loadFromDisk() {
        ClassLoader cl = SLInventory.class.getClassLoader();
        InputStream cubedbStream = cl.getResourceAsStream(INVENTORY_FILENAME);
        if (cubedbStream != null) {
            SLInventory res = new Gson().fromJson(
                new InputStreamReader(cubedbStream), SLInventory.class);
            if (res != null) {
                res.rebuild();
                return res;
            }
        }
        if (!ALLOW_LEGACY_LOADER) {
            ApplicationState.setWarning("CubeInventory", "couldn't read inventory from disk");
            return new SLInventory();
        }

        InputStream legacyFileStream = cl.getResourceAsStream(LEGACY_FILENAME);
        if (legacyFileStream == null) {
            return new SLInventory();
        }

        List<PhysicalFixture> cubes = new ArrayList<>();
        JsonObject legacyRecords = new Gson().fromJson(
            new InputStreamReader(legacyFileStream), JsonObject.class);
        for (Map.Entry<String, JsonElement> elem : legacyRecords.entrySet()) {
            PhysicalFixture c = new PhysicalFixture();
            c.idA = elem.getKey();
            c.addrA = elem.getValue().getAsString();
            c.imported = true;
            cubes.add(c);
        }
        return new SLInventory(cubes);
    }
}
