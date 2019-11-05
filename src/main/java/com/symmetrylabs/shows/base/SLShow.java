package com.symmetrylabs.shows.base;

import com.symmetrylabs.color.PerceptualColorScale;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.cubes.*;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.output.CubeModelControllerMapping;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.output.SLController;
//import com.symmetrylabs.slstudio.output.TreeController;
import com.symmetrylabs.slstudio.ui.v2.WindowManager;
import com.symmetrylabs.util.CubeInventory;
import com.symmetrylabs.util.NetworkChannelDebugMonitor.DebugPortMonitor;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.p3lx.ui.UI2dScrollContext;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.WeakHashMap;

/**
 * Base class of utilities all shows should derive from and benefit from
 */
public abstract class SLShow implements Show {
    public static final String SHOW_NAME = "slshow";

    public final ListenableSet<SLController> controllers = new ListenableSet<>();
    public final ListenableSet<CubesController> cubesControllers = new ListenableSet<>();
//    public final ListenableSet<TreeController> treeControllers = new ListenableSet<>();
    public final CubeInventory cubeInventory;
    public final CubeModelControllerMapping mapping;
    public final PerceptualColorScale outputScaler = new PerceptualColorScale(new double[] { 2.0, 2.1, 2.8 }, 1.0);

    private static Map<LX, WeakReference<SLShow>> instanceByLX = new WeakHashMap<>();

    public static SLShow getInstance(LX lx) {
        WeakReference<SLShow> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public SLShow() {
        cubeInventory = CubeInventory.loadFromDisk();
        mapping = CubeModelControllerMapping.loadFromDisk(getShowName(), cubeInventory);
    }

    public abstract SLModel buildModel();

    public void setupLx(LX lx) {
        instanceByLX.put(lx, new WeakReference<>(this));

        final NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx).start();
        final Dispatcher dispatcher = Dispatcher.getInstance(lx);

        /*
        SLController local_debug = new CubesController(lx, "localhost", "localdebug");
        controllers.add(local_debug);
        lx.addOutput(local_debug);
        */

        networkMonitor.opcDeviceList.addListener(new SetListener<NetworkDevice>() {
            public void onItemAdded(NetworkDevice device) {
                if (device.productId.equals("symmeTree")){
//                    treeControllers.add(new TreeController(controller).powerMonitorThreadStart()/*start the powermon thread*/); // after cubes controller extends SLController
//                    dispatcher.dispatchNetwork(() -> lx.addOutput(controller));
                }
                final SLController controller = new SLController(lx, device, new PointsGrouping(), device.deviceId);
//                controller.set16BitColorEnabled(device.featureIds.contains("rgb16"));
                controllers.add(controller);
//                if (controller.networkDevice.productId.equals("Cubes")){
//                    cubesControllers.add(controller); // after cubes controller extends SLController
//                }

                if (controller.networkDevice.productId.equals("symmeTree")){
//                    treeControllers.add(new TreeController(controller).powerMonitorThreadStart()/*start the powermon thread*/); // after cubes controller extends SLController
//                    dispatcher.dispatchNetwork(() -> lx.addOutput(controller));
                }

                dispatcher.dispatchNetwork(() -> lx.addOutput(controller));
                //controller.enabled.setValue(false);
            }

            public void onItemRemoved(NetworkDevice device) {
                final SLController controller = getControllerByDevice(device);
                controllers.remove(controller);
                dispatcher.dispatchNetwork(() -> {
                    controller.dispose();
                    lx.removeOutput(controller);
                });
            }
        });

        //lx.addOutput(new SLController(lx, "10.200.1.255"));

        lx.engine.output.enabled.addListener(param -> {
            boolean isEnabled = ((BooleanParameter) param).isOn();
            for (SLController controller : controllers) {
                controller.enabled.setValue(isEnabled);
            }
        });

        System.out.println("set up controllers");

        DebugPortMonitor debugPortMonitor = new DebugPortMonitor();
        debugPortMonitor.start();
    }

    public SLController getControllerByDevice(NetworkDevice device) {
        for (SLController controller : controllers) {
            if (controller.networkDevice == device) {
                return controller;
            }
        }
        return null;
    }

    public Collection<SLController> getSortedControllers() {
        return new TreeSet<SLController>(controllers);
    }

//    public Collection<SymmeTreeControlleer> getSortedControllers() {
//        return new TreeSet<SLController>(controllers);
//    }

    public void addControllerSetListener(SetListener<SLController> listener) {
        controllers.addListener(listener);
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UIOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UICubesMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }

    public void setupUi(LX lx) {
        WindowManager.addPersistent("Controllers/Inventory", () -> new InventoryEditor(lx, cubeInventory), false);
        WindowManager.addPersistent("Controllers/Mapping", () -> new MappingWindow(lx, (SLModel) lx.model), false);
        WindowManager.addPersistent("Controllers/Output", () -> new SLOutputWindow(lx, this), false);
        WindowManager.addPersistent("Controllers/Scaling", () -> new OutputScaleWindow(lx, outputScaler), false);
    }

    public abstract String getShowName();
}
