package com.symmetrylabs.shows.base;

import com.symmetrylabs.color.PerceptualColorScale;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.cubes.*;
import com.symmetrylabs.shows.tree.AssignableTenereController;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.mappings.SLModelControllerMapping;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.output.AbstractSLControllerBase;
//import com.symmetrylabs.slstudio.output.TreeController;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.ui.v2.SLModelMappingWindow;
import com.symmetrylabs.slstudio.ui.v2.WindowManager;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.hardware.SLControllerInventory;
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


    // top level metadata used in any show
    public static SLModelControllerMapping mapping = null; // only initialized for top level... more evidence we need a top level SLModel
    public SLControllerInventory controllerInventory = new SLControllerInventory();

    /**
     * Power related.
     */
    DiscreteParameter globalBlackoutPowerThreshhold = new DiscreteParameter("global blackout", 4095);


    public final HashMap<InetAddress, AbstractSLControllerBase> controllerByInetAddrMap = new HashMap<>();

    public final ListenableSet<AbstractSLControllerBase> controllers = new ListenableSet<>();
    public final ListenableSet<CubesController> cubesControllers = new ListenableSet<>();
//    public final ListenableSet<TreeController> treeControllers = new ListenableSet<>();
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
        AbstractSLControllerBase local_debug = new CubesController(lx, "localhost", "localdebug");
        controllers.add(local_debug);
        lx.addOutput(local_debug);
        */

        networkMonitor.opcDeviceList.addListener(new SetListener<NetworkDevice>() {
            public void onItemAdded(NetworkDevice device) {
                final AbstractSLControllerBase controller;
                try {
                    controller = new AssignableTenereController(lx, device, controllerInventory);
                    controllers.add(controller);
                    controllerByInetAddrMap.put(device.ipAddress, controller);
                    dispatcher.dispatchNetwork(() -> lx.addOutput(controller));
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }

            public void onItemRemoved(NetworkDevice device) {
                final AbstractSLControllerBase controller = getControllerByDevice(device);
                controllers.remove(controller);
                dispatcher.dispatchNetwork(() -> {
                    controller.dispose();
                    lx.removeOutput(controller);
                });
            }
        });

        //lx.addOutput(new AbstractSLControllerBase(lx, "10.200.1.255"));

        lx.engine.output.enabled.addListener(param -> {
            boolean isEnabled = ((BooleanParameter) param).isOn();
            for (AbstractSLControllerBase controller : controllers) {
                controller.enabled.setValue(isEnabled);
            }
        });

        System.out.println("set up controllers");
    }

    public AbstractSLControllerBase getControllerByDevice(NetworkDevice device) {
        for (AbstractSLControllerBase controller : controllers) {
            if (controller.networkDevice == device) {
                return controller;
            }
        }
        return null;
    }

    public Collection<AbstractSLControllerBase> getSortedControllers() {
        return new TreeSet<AbstractSLControllerBase>(controllers);
    }

//    public Collection<SymmeTreeControlleer> getSortedControllers() {
//        return new TreeSet<AbstractSLControllerBase>(controllers);
//    }

    public void addControllerSetListener(SetListener<AbstractSLControllerBase> listener) {
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
        WindowManager.addPersistent("Controllers/Output", () -> new SLOutputWindow(lx, this), false);
        WindowManager.addPersistent("Controllers/Scaling", () -> new OutputScaleWindow(lx, outputScaler), false);
    }

    public abstract String getShowName();

    public AbstractSLControllerBase getControllerByInetAddr(InetAddress sourceControllerAddr) {
        return controllerByInetAddrMap.get(sourceControllerAddr);
    }
}
