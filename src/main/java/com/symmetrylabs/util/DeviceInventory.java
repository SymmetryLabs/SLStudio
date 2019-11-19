//package com.symmetrylabs.util;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.stream.JsonWriter;
//import com.symmetrylabs.slstudio.ApplicationState;
//import org.apache.commons.collections4.iterators.IteratorChain;
//
//import java.io.*;
//import java.net.URL;
//import java.util.*;
//import java.util.function.Predicate;
//import java.util.regex.Pattern;
//
//// comprehensive repository of all devices owned by symmetrylabs including metadata
//public class DeviceInventory {
//    protected final static String INVENTORY_FILENAME = "device_inventory.json";
//
//    protected final static Predicate<String> MAC_ADDR_PATTERN = Pattern.compile("[A-Fa-f0-9]{2}::[A-Fa-f0-9]{2}::[A-Fa-f0-9]{2}::[A-Fa-f0-9]{2}::[A-Fa-f0-9]{2}::[A-Fa-f0-9]{2}").asPredicate();
//
//    public interface Listener {
//        void onCubeListUpdated();
//    }
//
//    public static class CubeDataError extends RuntimeException {
//        public CubeDataError(String err) {
//            super(err);
//        }
//
//        static void require(boolean check, String msg, Object... args) {
//            if (!check) {
//                throw new CubeDataError(String.format(msg, args));
//            }
//        }
//    }
//
//    public static class PhysicalCube {
//        public String addrA;
//        public String idA;
//
//        /** true if this is a legacy import cube */
//        public boolean imported;
//
//        public String getPhysicalId() {
//            return idA;
//        }
//
//        public Collection<String> getControllerIds() {
//            List<String> res = new ArrayList<>();
//            res.add(idA);
//            if (idB != null) {
//                res.add(idB);
//            }
//            return res;
//        }
//
//        public Collection<String> getControllerAddrs() {
//            List<String> res = new ArrayList<>();
//            res.add(addrA);
//            if (addrB != null) {
//                res.add(addrB);
//            }
//            return res;
//        }
//
//        @Override
//        public String toString() {
//            if (idB != null) {
//                return String.format("%s (%s:%s %s:%s)%s", idA, idA, addrA, idB, addrB, imported ? " [IMPORTED]" : "");
//            }
//            return String.format("%s (%s:%s) %s", idA, idA, addrA, imported ? " [IMPORTED]" : "");
//        }
//
//        void validate() {
//            CubeDataError.require(idA != null, "idA is null");
//            CubeDataError.require(addrA != null, "addrA is null on cube %s", idA);
//            CubeDataError.require(MAC_ADDR_PATTERN.test(addrA), "bad format for mac address A on cube %s: \"%s\"", idA, addrA);
//            // both or neither of idB and addrB must be null
//            CubeDataError.require((addrB == null) == (idB == null), "only one of idB and addrB were set on cube id %s", idA);
//            if (addrB != null) {
//                CubeDataError.require(MAC_ADDR_PATTERN.test(addrB), "bad format for mac address B on cube %s: \"%s\"", idA, addrB);
//            }
//        }
//    }
//
//    public final List<PhysicalCube> allCubes;
//
//    /* the entire state of the class is built from the single persistent field allCubes */
//    public final transient Map<String, PhysicalCube> cubeByMacAddrs = new HashMap<>();
//    public final transient Map<String, PhysicalCube> cubeByCtrlId = new HashMap<>();
//    public final transient Set<String> unknownMacAddrs = new HashSet<>();
//    private final transient List<Listener> listeners = new ArrayList<>();
//    private transient List<String> cubeErrors = new ArrayList<>();
//    private transient List<String> missingMacAddrErrors = new ArrayList<>();
//
//    protected DeviceInventory() {
//        allCubes = new ArrayList<PhysicalCube>();
//    }
//
//    protected DeviceInventory(List<PhysicalCube> allCubes) {
//        this.allCubes = allCubes;
//        rebuild();
//    }
//
//    public Iterator<CharSequence> getErrors() {
//        IteratorChain<CharSequence> iter = new IteratorChain<>();
//        iter.addIterator(cubeErrors.iterator());
//        iter.addIterator(missingMacAddrErrors.iterator());
//        return iter;
//    }
//
//    public String getErrorString() {
//        String res = String.join("\n", () -> getErrors());
//        if (res.length() == 0) {
//            return null;
//        }
//        return res;
//    }
//
//    private void onErrorMessagesUpdated() {
//        ApplicationState.setWarning("CubeInventory", getErrorString());
//    }
//
//    public PhysicalCube lookUpByPhysId(String physId) {
//        return cubeByCtrlId.get(physId);
//    }
//
//    public void rebuild() {
//        cubeByMacAddrs.clear();
//        cubeByCtrlId.clear();
//        unknownMacAddrs.clear();
//        cubeErrors.clear();
//        missingMacAddrErrors.clear();
//
//        for (PhysicalCube cube : allCubes) {
//            try {
//                cube.validate();
//                for (String id : cube.getControllerIds()) {
//                    CubeDataError.require(
//                        !cubeByCtrlId.containsKey(id), "ID %s duplicated on cube %s and %s",
//                        id, cubeByCtrlId.get(id), cube);
//                    cubeByCtrlId.put(id, cube);
//                }
//                for (String mac : cube.getControllerAddrs()) {
//                    CubeDataError.require(
//                        !cubeByMacAddrs.containsKey(mac), "MAC %s duplicated on cube %s and %s",
//                        mac, cubeByMacAddrs.get(mac), cube);
//                    cubeByMacAddrs.put(mac, cube);
//                }
//            } catch (CubeDataError e) {
//                cubeErrors.add(e.getMessage());
//            }
//        }
//        onErrorMessagesUpdated();
//        onUpdated();
//    }
//
//    private void onUpdated() {
//        for (Listener l : listeners) {
//            l.onCubeListUpdated();
//        }
//    }
//
//    public boolean save() {
//        ClassLoader cl = DeviceInventory.class.getClassLoader();
//        /* check to see if we have the file in resources */
//        URL dbUrl = cl.getResource(INVENTORY_FILENAME);
//        /* this is where the file is stored in the source tree */
//        File resFile = new File("src/main/resources", INVENTORY_FILENAME);
//        /* if the source tree file isn't present but the resource is present, we aren't in a
//           source distribution, so we can't save the file. */
//        if (dbUrl != null && !resFile.exists()) {
//            System.err.println(
//                String.format(
//                    "This build of Volume cannot save cube inventory files (resources are loaded from %s)",
//                    dbUrl));
//            return false;
//        }
//        try {
//            JsonWriter writer = new JsonWriter(new FileWriter(resFile));
//            writer.setIndent("  ");
//            new GsonBuilder().create().toJson(this, DeviceInventory.class, writer);
//            writer.close();
//            System.out.println("cube inventory written to " + resFile);
//            return true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    public void addListener(Listener listener) {
//        listeners.add(listener);
//    }
//
//    public void removeListener(Listener listener) {
//        listeners.remove(listener);
//    }
//
//    public PhysicalCube getDevice(String macAddr) {
//        PhysicalCube cube = cubeByMacAddrs.get(macAddr);
//        if (cube != null) {
//            return cube;
//        }
//        if (!unknownMacAddrs.contains(macAddr)) {
//            unknownMacAddrs.add(macAddr);
//            missingMacAddrErrors.add("No cube in inventory for discovered MAC address " + macAddr);
//            onErrorMessagesUpdated();
//        }
//        return null;
//    }
//
//    public String getControllerId(String macAddr) {
//        PhysicalCube cube = getDevice(macAddr);
//        if (cube != null) {
//            return macAddr.equals(cube.addrA) ? cube.idA : cube.idB;
//        }
//        return macAddr;
//    }
//
//    public static DeviceInventory loadFromDisk() {
//        ClassLoader cl = DeviceInventory.class.getClassLoader();
//        InputStream cubedbStream = cl.getResourceAsStream(INVENTORY_FILENAME);
//        if (cubedbStream != null) {
//            DeviceInventory res = new Gson().fromJson(
//                new InputStreamReader(cubedbStream), DeviceInventory.class);
//            if (res != null) {
//                res.rebuild();
//                return res;
//            }
//        }
//        return new DeviceInventory();
//    }
//}
