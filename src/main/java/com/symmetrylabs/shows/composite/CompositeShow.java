package com.symmetrylabs.shows.composite;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.mikey.MikeyShow;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesController;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.LX;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * CompositeShow merges MikeyShow and YsiadsPartyShow so both models and outputs coexist.
 */
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.listenable.SetListener;
import com.symmetrylabs.util.dispatch.Dispatcher;
import heronarts.lx.parameter.BooleanParameter;

public class CompositeShow extends com.symmetrylabs.shows.cubes.CubesShow {
    private final ListenableSet<SLController> controllers = new ListenableSet<>();
    private final ListenableSet<CubesController> cubesControllers = new ListenableSet<>();

    @Override
    public String getShowName() {
        return SHOW_NAME;
    }

    public void setupUi(com.symmetrylabs.slstudio.SLStudioLX lx, com.symmetrylabs.slstudio.SLStudioLX.UI ui) {
        new com.symmetrylabs.shows.cubes.UICubesOutputs(lx, ui, this, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.utility);
        new com.symmetrylabs.shows.cubes.UICubesMappingPanel(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth()).addToContainer(ui.rightPane.utility);
    }
    public static final String SHOW_NAME = "composite";

    @Override
    public SLModel buildModel() {
        return CompositeModel.create();
    }

    @Override
    public void setupLx(LX lx) {
        // Ensure model is built and attached to LX before any listeners
        // LX.model is final and must be set at construction.
        // If we do not have the correct model, abort with a clear error.
        if (!(lx.model instanceof CompositeModel)) {
            throw new IllegalStateException("LX must be constructed with a CompositeModel. Please ensure this in your app startup logic.");
        }
        CompositeModel model = (CompositeModel) lx.model;
        MikeyShow.MikeyModel mikeyModel = model.getMikeyModel();
        Dispatcher dispatcher = Dispatcher.getInstance(lx);
        NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx).start();

        // Now it is safe to register listeners
        networkMonitor.opcDeviceList.addListener(new SetListener<NetworkDevice>() {
            public void onItemAdded(NetworkDevice device) {
                String physicalId = device.deviceId;
                PointsGrouping points = new PointsGrouping(physicalId);
                CubesController cubesController = null;
                // Debug: log the size of model.cubes
                System.out.println("[CompositeShow] model.cubes size: " + model.cubes.size());
                // Find and wire up the matching cube
                for (com.symmetrylabs.shows.cubes.CubesModel.Cube cube : model.cubes) {
                    String cubeId = cube.modelId;
                    System.out.println("[CompositeShow] Checking cubeId=" + cubeId + " against deviceId=" + physicalId);
                    if (cubeId != null && cubeId.equals(physicalId)) {
                        System.out.println("[CompositeShow] Match found! Creating CubesController for cubeId=" + cubeId);
                        // Create CubesController for this cube
                        cubesController = new CubesController(lx, device, model.inventory, outputScaler);
                        cubesControllers.add(cubesController);
                        System.out.println("[CompositeShow] Added CubesController: " + cubesController + " for device " + device.deviceId);
                        final CubesController finalCubesController = cubesController;
                        dispatcher.dispatchNetwork(() -> lx.addOutput(finalCubesController));
                        // Print all outputs after addition
                        // Print the output that was just added
                        System.out.println("[CompositeShow] LX Output (just added): " + finalCubesController);
                        // Optionally set 16-bit color if device supports
                        cubesController.set16BitColorEnabled(device.featureIds.contains("rgb16"));
                        // Only one controller per device/cube
                        break;
                    }
                }
                // Existing SLController logic for Pixlite or other outputs
                PointsGrouping slPoints = new PointsGrouping(physicalId);
                for (com.symmetrylabs.shows.cubes.CubesModel.Cube cube : model.cubes) {
                    String cubeId = cube.modelId;
                    if (cubeId != null && cubeId.equals(physicalId)) {
                        List<com.symmetrylabs.slstudio.model.Strip> strips = ((com.symmetrylabs.slstudio.model.StripsModel) cube).getStrips();
                        for (com.symmetrylabs.slstudio.model.Strip strip : strips) {
                            slPoints.addPoints(strip.points);
                        }
                    }
                }
                SLController slController = new SLController(lx, device, slPoints, physicalId);
                controllers.add(slController);
                dispatcher.dispatchNetwork(() -> lx.addOutput(slController));
            }
            public void onItemRemoved(NetworkDevice device) {
                // Remove CubesController
                CubesController cubesControllerToRemove = null;
                for (CubesController c : cubesControllers) {
                    if (c.networkDevice == device) {
                        cubesControllerToRemove = c;
                        break;
                    }
                }
                if (cubesControllerToRemove != null) {
                    cubesControllers.remove(cubesControllerToRemove);
                    final CubesController finalToRemove = cubesControllerToRemove;
                    dispatcher.dispatchNetwork(() -> {
                        finalToRemove.dispose();
                        lx.removeOutput(finalToRemove);
                    });
                }
                // Remove SLController
                SLController slController = getCompositeControllerByDevice(device);
                if (slController != null) {
                    controllers.remove(slController);
                    dispatcher.dispatchNetwork(() -> {
                        //lx.removeOutput(slController);
                    });
                }
            }
        });

        // Enable/disable all controllers when engine output is toggled
        lx.engine.output.enabled.addListener(param -> {
            boolean isEnabled = ((BooleanParameter) param).isOn();
            for (SLController controller : controllers) {
                controller.enabled.setValue(isEnabled);
            }
        });

        // Add Mikey Pixlite outputs
        lx.addOutput(new MikeyShow.MikeyPixlite(lx, "192.168.1.42", mikeyModel, 0));
        lx.addOutput(new MikeyShow.MikeyPixlite(lx, "192.168.0.193", mikeyModel, 8));
    }

    // Use a differently named method to avoid conflict with CubesShow.getControllerByDevice
    public SLController getCompositeControllerByDevice(NetworkDevice device) {
        for (SLController controller : controllers) {
            if (controller.networkDevice == device) {
                return controller;
            }
        }
        return null;
    }

    /**
     * CompositeModel contains both MikeyModel and YsiadsPartyModel as children.
     */
    public static class CompositeModel extends com.symmetrylabs.slstudio.model.SLModel {
        public final MikeyShow.MikeyModel mikeyModel;
        public final CubesModel cubesModel;
        // Expose cubesModel fields for compatibility (corrected packages and types)
        public final com.symmetrylabs.slstudio.output.CubeModelControllerMapping mapping;
        public final com.symmetrylabs.util.hardware.CubeInventory inventory;
        public final java.util.List<com.symmetrylabs.shows.cubes.CubesModel.Tower> towers;
        public final java.util.List<com.symmetrylabs.shows.cubes.CubesModel.Cube> cubes;
        public final java.util.List<com.symmetrylabs.shows.cubes.CubesModel.Face> faces;

        public CompositeModel(MikeyShow.MikeyModel mikeyModel, CubesModel cubesModel) {
            super("CompositeModel", new CompositeFixture(mikeyModel, cubesModel));
            this.mikeyModel = mikeyModel;
            this.cubesModel = cubesModel;
            // Defensive: fail fast if mapping or inventory is null
            if (cubesModel.mapping == null)
                throw new IllegalStateException("cubesModel.mapping is null in CompositeModel constructor!");
            if (cubesModel.inventory == null)
                throw new IllegalStateException("cubesModel.inventory is null in CompositeModel constructor!");
            // Copy cubesModel fields for compatibility with code expecting them on the model
            this.mapping = cubesModel.mapping;
            this.inventory = cubesModel.inventory;
            this.towers = cubesModel.getTowers();
            this.cubes = cubesModel.getCubes();
            this.faces = cubesModel.getFaces();
        }

        private static class CompositeFixture extends heronarts.lx.model.LXAbstractFixture {
            CompositeFixture(MikeyShow.MikeyModel mikeyModel, CubesModel cubesModel) {
                if (mikeyModel != null && mikeyModel.getPoints() != null) {
                    this.points.addAll(mikeyModel.getPoints());
                }
                if (cubesModel != null && cubesModel.getPoints() != null) {
                    this.points.addAll(cubesModel.getPoints());
                }
            }
        }

        public Iterator<? extends LXModel> getChildren() {
            List<LXModel> children = new ArrayList<>();
            children.add(mikeyModel);
            children.add(cubesModel);
            return children.iterator();
        }

        public static CompositeModel create() {
            MikeyShow.MikeyModel mikeyModel = MikeyShow.MikeyModel.create();
            // Use YsiadsPartyShow to build the cubes model
            CubesModel cubesModel = (CubesModel) new com.symmetrylabs.shows.ysiadsparty.YsiadsPartyShow().buildModel();
            return new CompositeModel(mikeyModel, cubesModel);
        }

        public MikeyShow.MikeyModel getMikeyModel() { return mikeyModel; }
        public CubesModel getCubesModel() { return cubesModel; }

        private static List<LXPoint> combinePoints(LXModel... models) {
            List<LXPoint> all = new ArrayList<>();
            for (LXModel m : models) {
                Object pts = m.points;
                if (pts instanceof List) {
                    all.addAll((List<LXPoint>) pts);
                } else if (pts instanceof LXPoint[]) {
                    all.addAll(Arrays.asList((LXPoint[]) pts));
                }
            }
            return all;
        }
    }
}
