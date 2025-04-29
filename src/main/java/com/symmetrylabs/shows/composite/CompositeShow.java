package com.symmetrylabs.shows.composite;

import java.util.*;
import org.json.JSONObject;

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
    public static final String SHOW_NAME = "composite";
    
    private final ListenableSet<SLController> controllers = new ListenableSet<>();
    private final ListenableSet<CubesController> cubesControllers = new ListenableSet<>();
    
    // Map to track all assigned indices for overlap check
    private static Map<String, int[]> assignedIndicesMap = new HashMap<>();

    public Collection<CubesController> getSortedControllers() {
        return new TreeSet<>(cubesControllers);
    }

    public void addControllerSetListener(SetListener<CubesController> listener) {
        cubesControllers.addListener(listener);
    }

    public String getShowName() {
        return SHOW_NAME;
    }

    @Override
    public void setupUi(com.symmetrylabs.slstudio.SLStudioLX lx, com.symmetrylabs.slstudio.SLStudioLX.UI ui) {
        new com.symmetrylabs.shows.cubes.UICubesOutputs(lx, ui, this, 0, 0, ui.rightPane.utility.getContentWidth())
            .addToContainer(ui.rightPane.utility);
        new com.symmetrylabs.shows.cubes.UICubesMappingPanel(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth())
            .addToContainer(ui.rightPane.utility);
    }

    @Override
    public SLModel buildModel() {
        return CompositeModel.create();
    }

    @Override
    public void setupLx(LX lx) {
        // Ensure model is built and attached to LX before any listeners
        if (!(lx.model instanceof CompositeModel)) {
            throw new IllegalStateException("LX must be constructed with a CompositeModel");
        }

        CompositeModel model = (CompositeModel) lx.model;
        MikeyShow.MikeyModel mikeyModel = model.getMikeyModel();
        CubesModel cubesModel = model.getCubesModel();
        Dispatcher dispatcher = Dispatcher.getInstance(lx);
        NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx).start();

        // Validate point ranges and check for overlaps
        int mikeyStart = -1, mikeyEnd = -1;
        int cubesStart = -1, cubesEnd = -1;

        if (mikeyModel != null && !mikeyModel.getPoints().isEmpty()) {
            mikeyStart = mikeyModel.getPoints().get(0).index;
            mikeyEnd = mikeyModel.getPoints().get(mikeyModel.getPoints().size() - 1).index;
            System.out.println("[CompositeShow] MikeyModel points range: " + mikeyStart + 
                " to " + mikeyEnd + " (" + mikeyModel.getPoints().size() + " points)");

            // Validate that Mikey indices are sequential and within expected range
            for (int i = 0; i < mikeyModel.getPoints().size(); i++) {
                int idx = mikeyModel.getPoints().get(i).index;
                if (idx < mikeyStart || idx > mikeyEnd) {
                    System.err.println("[CompositeShow] WARNING: Mikey point index " + idx + 
                        " outside expected range [" + mikeyStart + "," + mikeyEnd + "]");
                }
            }
        }
        
        if (cubesModel != null && !cubesModel.getPoints().isEmpty()) {
            cubesStart = cubesModel.getPoints().get(0).index;
            cubesEnd = cubesModel.getPoints().get(cubesModel.getPoints().size() - 1).index;
            System.out.println("[CompositeShow] CubesModel points range: " + cubesStart + 
                " to " + cubesEnd + " (" + cubesModel.getPoints().size() + " points)");

            // Validate that cube indices are sequential and within expected range
            for (int i = 0; i < cubesModel.getPoints().size(); i++) {
                int idx = cubesModel.getPoints().get(i).index;
                if (idx < cubesStart || idx > cubesEnd) {
                    System.err.println("[CompositeShow] WARNING: Cube point index " + idx + 
                        " outside expected range [" + cubesStart + "," + cubesEnd + "]");
                }
            }
        }

        // Check for overlap between Mikey and Cubes point ranges
        if (mikeyStart != -1 && cubesStart != -1) {
            if ((mikeyStart <= cubesEnd && mikeyEnd >= cubesStart) || 
                (cubesStart <= mikeyEnd && cubesEnd >= mikeyStart)) {
                System.err.println("[CompositeShow] ERROR: Point index overlap detected between MikeyModel and CubesModel!");
                System.err.println("MikeyModel range: [" + mikeyStart + "," + mikeyEnd + "]");
                System.err.println("CubesModel range: [" + cubesStart + "," + cubesEnd + "]");
            }
        }

        // Load MAC-to-cubeId mapping from physid_to_mac.json
        Map<String, String> macToCubeId = new HashMap<>();
        try {
            java.nio.file.Path path = java.nio.file.Paths.get("src/main/resources/physid_to_mac.json");
            String json = new String(java.nio.file.Files.readAllBytes(path));
            JSONObject obj = new JSONObject(json);
            for (String key : obj.keySet()) {
                macToCubeId.put(key, obj.getString(key));
            }
        } catch (Exception e) {
            System.err.println("[CompositeShow] Error loading physid_to_mac.json: " + e.getMessage());
        }

        // Add network device listener for cubes
        networkMonitor.opcDeviceList.addListener(new SetListener<NetworkDevice>() {
            @Override
            public void onItemAdded(NetworkDevice device) {
                String physicalId = device.deviceId;
                CubesModel.Cube matchedCube = null;
                String matchedCubeId = null;

                // Search for cubeId whose mapped MAC matches this device's MAC
                for (Map.Entry<String, String> entry : macToCubeId.entrySet()) {
                    if (entry.getValue().equalsIgnoreCase(physicalId)) {
                        matchedCubeId = entry.getKey();
                        // Now find the cube with this ID
                        for (CubesModel.Cube cube : model.cubes) {
                            if (cube.id != null && cube.id.equals(matchedCubeId)) {
                                matchedCube = cube;
                                break;
                            }
                        }
                        break;
                    }
                }

                if (matchedCube == null) {
                    System.err.println("[CompositeShow] No matching cube found for device " + device.deviceId);
                    return;
                }

                // Create controller and points grouping
                CubesController cubesController = new CubesController(lx, device, matchedCubeId);
                PointsGrouping points = new PointsGrouping(matchedCubeId);

                // Get strips from the matched cube
                if (!(matchedCube instanceof StripsModel)) {
                    System.err.println("[CompositeShow] ERROR: Matched cube is not a StripsModel: " + 
                        matchedCube.getClass().getName());
                    return;
                }

                List<Strip> strips = ((StripsModel) matchedCube).getStrips();

                // Validate strip count
                if (strips.size() != 12) {
                    System.err.println("[CompositeShow] ERROR: Cube " + matchedCubeId + 
                        " has " + strips.size() + " strips, expected 12");
                    return;
                }

                // Track points for duplicate checking
                Set<LXPoint> assignedPoints = new HashSet<>();

                // Define strip order based on physical wiring
                int[] stripOrder = {6, 7, 8, 9, 10, 11, 0, 1, 2, 3, 4, 5};
                
                System.out.println("[CompositeShow] Assigning points for cube " + matchedCubeId);
                
                // First validate all strips have points
                for (int stripIndex : stripOrder) {
                    Strip strip = strips.get(stripIndex);
                    if (strip.points == null || strip.points.length == 0) {
                        System.err.println("[CompositeShow] ERROR: Strip " + stripIndex + 
                            " has no points in cube " + matchedCubeId);
                        return;
                    }
                }
                
                // Now assign points in order
                for (int stripIndex : stripOrder) {
                    Strip strip = strips.get(stripIndex);
                    
                    // Log the points we're about to add
                    System.out.println("[CompositeShow] Adding strip " + stripIndex + " points [" + 
                        strip.points[0].index + " to " + strip.points[strip.points.length-1].index + 
                        "] to cube " + matchedCubeId);
                    
                    // Check for duplicate point assignments
                    for (LXPoint point : strip.points) {
                        if (assignedPoints.contains(point)) {
                            System.err.println("[CompositeShow] WARNING: Point " + point.index + 
                                " already assigned in cube " + matchedCubeId);
                            continue;
                        }
                        assignedPoints.add(point);
                    }
                    points.addPoints(strip.points);
                }
                
                // Set the points on the controller
                cubesController.setPoints(points);
                System.out.println("[CompositeShow] Total points assigned to cube " + matchedCubeId + 
                    ": " + assignedPoints.size());
                
                // Add to static map for overlap check
                if (assignedIndicesMap != null) {
                    assignedIndicesMap.put(cubesController.id, points.getIndices());
                }
                
                // Enable 16-bit color if supported
                cubesController.set16BitColorEnabled(device.featureIds.contains("rgb16"));
                
                // Add to both controller sets and register output
                cubesControllers.add(cubesController);
                dispatcher.dispatchNetwork(() -> lx.addOutput(cubesController));
            }

            @Override
            public void onItemRemoved(NetworkDevice device) {
                CubesController cubesControllerToRemove = null;
                for (CubesController cc : cubesControllers) {
                    if (cc.networkDevice.equals(device)) {
                        cubesControllerToRemove = cc;
                        break;
                    }
                }
                if (cubesControllerToRemove != null) {
                    cubesControllers.remove(cubesControllerToRemove);
                    final CubesController toRemove = cubesControllerToRemove;
                    dispatcher.dispatchNetwork(() -> {
                        toRemove.dispose();
                        lx.removeOutput(toRemove);
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
        // lx.addOutput(new MikeyShow.MikeyPixlite(lx, "192.168.0.193", mikeyModel, 8));
    }

    private SLController getCompositeControllerByDevice(NetworkDevice device) {
        for (SLController controller : controllers) {
            if (controller.networkDevice.equals(device)) {
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

        public final java.util.List<com.symmetrylabs.shows.cubes.CubesModel.Tower> towers;
        public final java.util.List<com.symmetrylabs.shows.cubes.CubesModel.Cube> cubes;
        public final java.util.List<com.symmetrylabs.shows.cubes.CubesModel.Face> faces;

        public CompositeModel(MikeyShow.MikeyModel mikeyModel, CubesModel cubesModel) {
            super(combinePoints(mikeyModel, cubesModel));
            this.mikeyModel = mikeyModel;
            this.cubesModel = cubesModel;

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
