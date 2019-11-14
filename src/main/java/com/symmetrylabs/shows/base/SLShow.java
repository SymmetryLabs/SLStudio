package com.symmetrylabs.shows.base;

import com.symmetrylabs.color.PerceptualColorScale;
import com.symmetrylabs.controllers.symmeTreeController.infrastructure.AllPortsPowerEnableMask;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.cubes.*;
import com.symmetrylabs.shows.tree.AssignableTenereController;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.mappings.SLModelControllerMapping;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.output.DiscoverableController;
//import com.symmetrylabs.slstudio.output.TreeController;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.ui.v2.SLModelMappingWindow;
import com.symmetrylabs.slstudio.ui.v2.WindowManager;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.hardware.SLControllerInventory;
import com.symmetrylabs.util.hardware.powerMon.ControllerWithPowerFeedback;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.p3lx.ui.UI2dScrollContext;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

/**
 * Base class of utilities all shows should derive from and benefit from
 */
public abstract class SLShow implements Show {
    public static final String SHOW_NAME = "slshow";

    // power mask stuff.. this will be pretty universal and should proably be an inherited class.  For now just testing;
    public AllPortsPowerEnableMask allPortsPowerEnableMask = AllPortsPowerEnableMask.loadFromDisk();

    // top level metadata used in any show
    public static SLModelControllerMapping mapping = null; // only initialized for top level... more evidence we need a top level SLModel
    public SLControllerInventory controllerInventory = new SLControllerInventory();

    /**
     * Power related.
     */
    DiscreteParameter globalBlackoutPowerThreshhold = new DiscreteParameter("global blackout", 200, 0, 4095);


    public final HashMap<InetAddress, DiscoverableController> controllerByInetAddrMap = new HashMap<>();
    public final HashMap<String, DiscoverableController> controllerByName = new HashMap<String, DiscoverableController>();

    public final ListenableSet<DiscoverableController> controllers = new ListenableSet<>();
    public final PerceptualColorScale outputScaler = new PerceptualColorScale(new double[] { 2.0, 2.1, 2.8 }, 1.0);

    private static Map<LX, WeakReference<SLShow>> instanceByLX = new WeakHashMap<>();

    public static SLShow getInstance(LX lx) {
        WeakReference<SLShow> weakRef = instanceByLX.get(lx);
        return weakRef == null ? null : weakRef.get();
    }

    public SLShow() {
        try {
            controllerInventory = SLControllerInventory.loadFromDisk();
        } catch (FileNotFoundException e) {
            System.err.println("could not load controller inventory");
            e.printStackTrace();
        }
        mapping = SLModelControllerMapping.loadFromDisk(getShowName(), controllerInventory);
    }

    public abstract SLModel buildModel();

    public void setupLx(LX lx) {
        instanceByLX.put(lx, new WeakReference<>(this));

        final NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx).start();
        final Dispatcher dispatcher = Dispatcher.getInstance(lx);

        /*
        DiscoverableController local_debug = new CubesController(lx, "localhost", "localdebug");
        controllers.add(local_debug);
        lx.addOutput(local_debug);
        */

        networkMonitor.opcDeviceList.addListener(new SetListener<NetworkDevice>() {
            public void onItemAdded(NetworkDevice device) {
                final DiscoverableController controller;
                try {
                    controller = new AssignableTenereController(lx, device, controllerInventory, allPortsPowerEnableMask);
                    controllers.add(controller);
                    controllerByInetAddrMap.put(device.ipAddress, controller);
                    controllerByName.put(controller.humanID, controller);
                    dispatcher.dispatchNetwork(() -> lx.addOutput(controller));
                    // set port mask initially
                    allPortsPowerEnableMask.applyStateToController((ControllerWithPowerFeedback) controller);
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }

            public void onItemRemoved(NetworkDevice device) {
                final DiscoverableController controller = getControllerByDevice(device);
                controllers.remove(controller);
                dispatcher.dispatchNetwork(() -> {
                    controller.dispose();
                    lx.removeOutput(controller);
                });
            }
        });

        //lx.addOutput(new DiscoverableController(lx, "10.200.1.255"));

        lx.engine.output.enabled.addListener(param -> {
            boolean isEnabled = ((BooleanParameter) param).isOn();
            for (DiscoverableController controller : controllers) {
                controller.enabled.setValue(isEnabled);
            }
        });

        System.out.println("set up controllers");
    }

    public DiscoverableController getControllerByDevice(NetworkDevice device) {
        for (DiscoverableController controller : controllers) {
            if (controller.networkDevice == device) {
                return controller;
            }
        }
        return null;
    }

    public Collection<DiscoverableController> getSortedControllers() {
        return new TreeSet<DiscoverableController>(controllers);
    }

    public void addControllerSetListener(SetListener<DiscoverableController> listener) {
        controllers.addListener(listener);
    }

    public static PointsGrouping getPointsMappedToControllerID(String humanID) {
        SLModel mappedModel = SLModel.fixtureByMappedID.get(humanID);
        // ok we found a mapping. Place it in the mappings.
        if (mappedModel != null) {

//            CONCURRENT MODIFICATION EXCEPTION: mapping.setControllerAssignment(mappedModel.modelId, humanID); // is this necessary after constructor?
            return new PointsGrouping(mappedModel.getPoints());
        }
        else return null;
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UIOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UICubesMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }

    public void setupUi(LX lx) {
//        WindowManager.addPersistent("Controllers/Inventory", () -> new CubesInventoryEditor(lx, controllerInventory), false);
        WindowManager.addPersistent("Controllers/Mapping", () -> new SLModelMappingWindow(lx, this), false);
        WindowManager.addPersistent("Controllers/Output", () -> new SLOutputWindow(lx, this), true);
        WindowManager.addPersistent("Controllers/Scaling", () -> new OutputScaleWindow(lx, outputScaler), false);
    }

    public abstract String getShowName();

    public DiscoverableController getControllerByInetAddr(InetAddress sourceControllerAddr) {
        return controllerByInetAddrMap.get(sourceControllerAddr);
    }
}
