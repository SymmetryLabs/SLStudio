//package com.symmetrylabs.layouts.dynamic_JSON;
//
//import com.symmetrylabs.layouts.Layout;
//import com.symmetrylabs.layouts.cubes.CubesController;
//import com.symmetrylabs.layouts.cubes.CubesLayout;
//import com.symmetrylabs.slstudio.SLStudioLX;
//import com.symmetrylabs.slstudio.model.SLModel;
//import com.symmetrylabs.slstudio.output.ArtNetDatagram;
//import com.symmetrylabs.slstudio.output.MappingPixlite;
//import com.symmetrylabs.slstudio.output.SimplePixlite;
//import com.symmetrylabs.util.listenable.SetListener;
//import com.symmetrylabs.util.listenable.ListenableSet;
//import heronarts.lx.LX;
//import heronarts.lx.model.LXAbstractFixture;
//import heronarts.lx.model.LXFixture;
//import heronarts.lx.model.LXPoint;
//import heronarts.lx.output.FadecandyOutput;
//import heronarts.lx.output.LXDatagramOutput;
//import heronarts.lx.output.OPCDatagram;
//
//import java.lang.ref.WeakReference;
//import java.util.*;
//
//public class DynamicLayout extends CubesLayout implements Layout {
//    ListenableSet<CubesController> controllers = new ListenableSet<>();
//    public MappingPixlite[] mappingPixlites;
//    SimplePixlite[] pixlites;
//
//    static final float globalOffsetX = 0;
//    static final float globalOffsetY = 0;
//    static final float globalOffsetZ = 0;
//
//    static final float globalRotationX = 0;
//    static final float globalRotationY = -45;
//    static final float globalRotationZ = 0;
//
//    static final float objOffsetX = 0;
//    static final float objOffsetY = 0;
//    static final float objOffsetZ = 0;
//
//    static final float objRotationX = 0;
//    static final float objRotationY = 0;
//    static final float objRotationZ = 0;
//
//    static final float CUBE_WIDTH = 24;
//    static final float CUBE_HEIGHT = 24;
//    static final float TOWER_WIDTH = 24;
//    static final float TOWER_HEIGHT = 24;
//    static final float CUBE_SPACING = 2.5f;
//
//    static final float TOWER_VERTICAL_SPACING = 2.5f;
//    static final float TOWER_RISER = 14;
//    static final float SP = 24;
//    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;
//
//    static final float INCHES_PER_METER = 39.3701f;
//
//    static Map<String, String> macToPhysid = new HashMap<>();
//    static Map<String, String> physidToMac = new HashMap<>();
//
//    List<LXFixture> dynamicfixtures = new ArrayList<>();
//
//
//    private static Map<LX, WeakReference<DynamicLayout>> instanceByLX = new WeakHashMap<>();
//
//    public static DynamicLayout getInstance(LX lx) {
//        WeakReference<DynamicLayout> weakRef = instanceByLX.get(lx);
//        return weakRef == null ? null : weakRef.get();
//    }
//
//    public static void addArtNetOutput(LX lx, LXAbstractFixture mapOutput) throws Exception {
//        int[] indices = new int[mapOutput.getPoints().size()];
//        int idx = 0;
//        for (LXPoint p : mapOutput.getPoints()){
//            indices[idx++] = p.index;
//        }
//        lx.engine.addOutput(
//            new LXDatagramOutput(lx).addDatagram(
//                new ArtNetDatagram(lx, "192.168.0.113", indices, 10)
////        .setAddress("10.200.1.128")
//            )
//        );
//    }
//
//    public static void addDatagramOPCOutput(LX lx, String ip_addr) throws Exception {
//        lx.engine.addOutput(
//            new LXDatagramOutput(lx).addDatagram(
//                new OPCDatagram(lx.model)
//                    .setAddress(ip_addr)
//                    .setPort(7890)
//
//            )
//        );
//    }
//    public static void addFadecandyOutput(LX lx) throws Exception {
////    lx.engine.addOutput(new FadecandyOutput(lx, "localhost", 7890, lx.model));
//        lx.engine.addOutput(new FadecandyOutput(lx, "192.168.0.113", 1234, lx.model));
//    }
//
//    public SLModel buildModel() {
//        SLModel cubesModel = super.buildModel();
//
////        byte[] bytes = SLStudio.applet.loadBytes("physid_to_mac.json");
////        if (bytes != null) {
////            try {
////                JsonObject json = new Gson().fromJson(new String(bytes), JsonObject.class);
////                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
////                    macToPhysid.put(entry.getValue().getAsString(), entry.getKey());
////                    physidToMac.put(entry.getKey(), entry.getValue().getAsString());
////                }
////            }  catch (JsonSyntaxException e) {
////                e.printStackTrace();
////            }
////        }
////
////        // Any global transforms
////        LXTransform globalTransform = new LXTransform();
////        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
////        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
////        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
////        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);
////
////        /* Cubes ----------------------------------------------------------*/
////        // Read in the JSON and begin constructing model and assigning outputs.
////        CandyBar candy = new CandyBar();
////        dynamicfixtures.add(candy);
////
////        for (int i = 0; i < 20; i ++){
////            LocatedForm located = new LocatedForm(globalTransform, candy);
////            globalTransform.translate(0,5,0);
////            dynamicfixtures.add(located);
////        }
////
////        LXFixture[] yeee =  new LXFixture[dynamicfixtures.size()];
////        yeee = dynamicfixtures.toArray(yeee);
////
//
//        SLModel model = new SLModel(cubesModel);
//        return model;
//    }
//
//    /*
//    public static LXModel importObjModel() {
//        return new LXModel(new ObjImporter("data", globalTransform).getModels().toArray(new LXModel[0]));
//    }
//    */
//    public void setupLx(SLStudioLX lx) {
//        super.setupLx(lx);
////        instanceByLX.put(lx, new WeakReference<>(this));
////
//////        try {
//////            addArtNetOutput(lx, (LXAbstractFixture) dynamicfixtures.get(0));
//////        } catch (Exception e) {
//////            e.printStackTrace();
//////        }
//////        for (int i = 0; i < dynamicfixtures.size(); i++){
//////        }
////
////        try {
////            addFadecandyOutput(lx);
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//////        try {
//////            addDatagramOPCOutput(lx, "10.200.1.192");
//////            addDatagramOPCOutput(lx, "10.200.1.193");
//////            addDatagramOPCOutput(lx, "10.200.1.194");
//////            addDatagramOPCOutput(lx, "10.200.1.195");
//////            addDatagramOPCOutput(lx, "10.200.1.196");
//////        } catch (Exception e) {
//////            e.printStackTrace();
//////        }
////
//////        controllers = CubesLayout.setupCubesOutputs(lx);
//////        pixlites = new SimplePixliteConfigs().setupPixlites(lx, dynamicfixtures);
//////        lx.addOutput(SimplePixliteConfigs.setupPixlite(lx, dynamicfixtures.get(0),"10.200.1.153", 1));
////
//////        for (SimplePixlite pl: pixlites) {
//////            lx.addOutput(pl);
//////        }
////
////        final NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx).start();
////        final Dispatcher dispatcher = Dispatcher.getInstance(lx);
////
////
////        networkMonitor.deviceList.addListener(new SetListener<NetworkDevice>() {
////            public void onItemAdded(int index, NetworkDevice device) {
////                String macAddr = NetworkUtils.macAddrToString(device.macAddress);
////                String physid = macToPhysid.get(macAddr);
////                if (physid == null) {
////                    physid = macAddr;
////                    System.err.println("WARNING: MAC address not in physid_to_mac.json: " + macAddr);
////                }
////                final CubesController controller = new CubesController(lx, device, physid);
////                controllers.add(index, controller);
////                dispatcher.dispatchEngine(new Runnable() {
////                    public void run() {
////                        lx.addOutput(controller);
////                    }
////                });
//////                controller.enabled.setValue(false);
////            }
////
////            public void onItemRemoved(int index, NetworkDevice device) {
////                final CubesController controller = controllers.remove(index);
////                dispatcher.dispatchEngine(new Runnable() {
////                    public void run() {
//////                        lx.removeOutput(controller);
////                    }
////                });
////            }
////        });
////
////        lx.addOutput(new CubesController(lx, "10.200.1.255"));
////        //lx.addOutput(new LIFXOutput());
////
////        lx.engine.output.enabled.addListener(param -> {
////            boolean isEnabled = ((BooleanParameter) param).isOn();
////            for (CubesController controller : controllers) {
////                controller.enabled.setValue(isEnabled);
////            }
////        });
//    }
//
//    public void addControllerSetListener(SetListener<CubesController> listener) {
//        controllers.addListener(listener);
//    }
//
//    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
//        super.setupUi(lx, ui);
////        UI2dScrollContext utility = ui.rightPane.utility;
////        new UIOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
////        new UIMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
//    }
//}
//
