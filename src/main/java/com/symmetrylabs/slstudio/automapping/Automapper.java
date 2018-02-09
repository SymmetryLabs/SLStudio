package com.symmetrylabs.slstudio.automapping;

import java.io.File;
import java.util.*;
import java.awt.Window;
import javax.swing.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import processing.core.PMatrix3D;
import processing.core.PVector;


import heronarts.lx.LX;
import heronarts.lx.LXRunnableComponent;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXVector;
import heronarts.lx.parameter.*;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.util.NetworkUtils;
import com.symmetrylabs.util.listenable.ListenableList;
import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.layouts.cubes.CubesLayout;

import static com.symmetrylabs.util.Utils.dataPath;
import static com.symmetrylabs.util.MathUtils.max;
import static com.symmetrylabs.util.MathConstants.PI;
import static processing.core.PApplet.loadBytes;
import static processing.core.PApplet.saveBytes;

public class Automapper extends LXRunnableComponent {

    public static enum PatternMode {
        CALIBRATING,
            MAPPING,
            SHOW_PIXEL,
            ALL_ON,
            ALL_OFF,
            SHOW_CUBE
    }

    public static enum PatternState {
        S0_IDENTIFY,
            S1_BLACK,
            S2_WHITE,
            S3_BLACK,
            STATE_END;
        public static final PatternState values[] = values();
    }

    public static enum AutomappingState {
        DISCONNECTED,
            CONNECTED,
            RUNNING
    }

    public final LX lx;
    public SLStudioLX.UI ui;

    public final EnumParameter<AutomappingState> state = new EnumParameter<>("State", AutomappingState.RUNNING);
    public final StringParameter saveFile = new StringParameter("Save File", "cube_transforms.json");

    private String[] macAddresses = null;
    private int[] pixelOrder = null;

    public ListenableList<CVFixture> mappedFixtures;

    PatternState patternState;
    PatternMode mode = PatternMode.ALL_OFF;

    int baseColor = LXColor.WHITE;
    int resetFrameBaseColor = LXColor.scaleBrightness(LXColor.WHITE, 1);

    int NUM_RUNTHROUGHS = 1;
    int NUM_CALIBRATION_RUNTHROUGHS = -1;

    int RESET_FRAMES = 30;
    int RESET_BLACK_FRAMES = 16;
    int S0_FRAMES = 3;
    int S1_FRAMES = 3;
    int S2_FRAMES = 3;
    int S3_FRAMES = 3;

    int numPoints = 0;
    int runthroughCount = -1;
    int patternPixelIndex = 0;
    int frameCounter = 0;
    int showPixelIndex = 0;
    int mod = 1;
    public boolean hideLabels = false;
    String currentCubeId = null;

    Map<String, CubesModel.Cube.Type> knownCubeTypes;

    final JLabel label = new JLabel();

    private static Map<LX, Automapper> instanceByLX = new HashMap<>();

    public static synchronized Automapper getInstance(LX lx) {
        if (lx == null)
            return null;

        if (!instanceByLX.containsKey(lx)) {
            instanceByLX.put(lx, new Automapper(lx));
        }

        return instanceByLX.get(lx);
    }

    private ClientCommunicator communicator;
    private ServerDiscovery serverDiscovery;

    private Automapper(LX lx) {
        super(lx, "Automapper");

        this.lx = lx;

        if (lx instanceof SLStudioLX) {
            this.ui = ((SLStudioLX)lx).ui;
        }

        addParameter(state);
        addParameter(saveFile);

        mappedFixtures = new ListenableList<CVFixture>();

        //pixelOrder = new int[180];
        //for (int i = 0; i < pixelOrder.length; i++) {
        //    pixelOrder[i] = i;
        //}

        knownCubeTypes = loadKnownCubeTypes();

        communicator = new ClientCommunicator(this);
        serverDiscovery = new ServerDiscovery();
    }

    @Override
    public void run(double deltaMs) {
        communicator.loop(deltaMs);
        updateFrame();
    }

    @Override
    protected void onStart() {
        communicator.start();
        serverDiscovery.start();
        lx.engine.addLoopTask(this);
    }

    @Override
    protected void onStop() {
        lx.engine.removeLoopTask(this);
        serverDiscovery.stop();
        communicator.stop();
    }

    @Override
    public void dispose() {
        synchronized (Automapper.class) {
            stop();

            instanceByLX.remove(lx);
        }
    }

    Map<String, CubesModel.Cube.Type> loadKnownCubeTypes() {
        Map<String, CubesModel.Cube.Type> types = new HashMap<>();

        String data = new String(loadBytes(new File(dataPath("physid_to_size.json"))));
        Map<String, String> map = (new Gson()).fromJson(data, new TypeToken<Map<String, String>>() {}.getType());

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String k = entry.getKey();
            String stringType = entry.getValue();
            CubesModel.Cube.Type v;
            if (stringType.equals("SMALL")) {
                v = CubesModel.Cube.Type.SMALL;
            } else if (stringType.equals("MEDIUM")) {
                v = CubesModel.Cube.Type.MEDIUM;
            } else if (stringType.equals("LARGE")) {
                v = CubesModel.Cube.Type.LARGE;
            } else {
                throw new RuntimeException("UNKNOWN CUBE TYPE: " + stringType);
            }
            types.put(k, v);
        }

        return types;
    }

    public void sendStartCommand() {
        communicator.sendCommand("app.start", null);
    }

    void showError(String message, boolean sendToClient) {
        label.setText(message);

        Runnable showError = new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, label);
                communicator.sendCommand("dismissError", null);
            }
        };

        (new Thread(showError)).start();

        if (sendToClient) {
            Map<String, String> msg = new HashMap<>();
            msg.put("message", message);
            communicator.sendCommand("showError", msg);
        }
    }

    void dismissError() {
        Window win = SwingUtilities.getWindowAncestor(label);
        if (win != null) {
            win.setVisible(false);
        }
    }

    public void rotateCubes(char dir) {
        CVFixture selected = null;
        for (CVFixture cube : mappedFixtures) {
            if (cube.selected) {
                selected = cube;
                break;
            }
        }

        if (selected == null) {
            return;
        }

        PMatrix3D inv = selected.mat.get();
        inv.invert();

        PMatrix3D rot = new PMatrix3D();

        switch(dir) {
        case 'u':
            break;
        case 'd':
            rot.rotateX(PI);
            break;
        case 'l':
            rot.rotateZ(PI/2);
            break;
        case 'r':
            rot.rotateZ(-PI/2);
            break;
        case 'b':
            rot.rotateX(PI/2);
            break;
        case 'f':
            rot.rotateX(-PI/2);
            break;
        default:
            throw new RuntimeException("Invalid direction in rotate");
        }

        for (CVFixture cube : mappedFixtures) {
            cube.mat.preApply(inv);
            cube.mat.preApply(rot);
        }
    }

    public void center() {
        if (mappedFixtures.size() == 0) {
            return;
        }

        PVector sum = new PVector(0, 0, 0);
        for (CVFixture c : mappedFixtures) {
            PVector t = new PVector(c.mat.m03, c.mat.m13, c.mat.m23);
            sum.add(t);
        }
        sum.div(mappedFixtures.size());

        for (CVFixture c : mappedFixtures) {
            c.mat.m03 -= sum.x;
            c.mat.m13 -= sum.y;
            c.mat.m23 -= sum.z;

        }
    }

    void addCube(List<List<Double>> matrix, List<Double> rvec, List<Double> tvec, String id) {
        CVFixture c = new CVFixture(this, matrix, rvec, tvec, id);

        mappedFixtures.add(c);
        Collections.sort(mappedFixtures.list);

        if (ui != null) {
            ui.preview.addComponent(c);
        }
    }

    public void removeCube(String id) {
        CVFixture cube = null;
        int cubeIndex = -1;
        for (int i = 0; i < mappedFixtures.size(); i++) {
            CVFixture temp = mappedFixtures.get(i);
            if (temp.id.equals(id)) {
                cube = temp;
                cubeIndex = i;
                break;
            }
        }

        if (cube == null) {
            return;
        }

        if (ui != null) {
            ui.preview.removeComponent(cube);
        }

        mappedFixtures.remove(cubeIndex);
    }

    boolean alreadyMapped(String id) {
        for (CVFixture c : mappedFixtures) {
            if (id.equals(c.id)) {
                return true;
            }
        }
        return false;
    }

    ArrayList<Object> getMappedTransforms() {
        ArrayList<Object> transforms = new ArrayList<Object>();
        for (CVFixture c : mappedFixtures) {
            transforms.add(c.getRawMatrix());
        }
        return transforms;
    }

    ArrayList<String> getMappedIds() {
        ArrayList<String> ids = new ArrayList<String>();
        for (CVFixture c : mappedFixtures) {
            ids.add(c.id);
        }
        return ids;
    }

    ArrayList<String> getUnmappedIds() {
        ArrayList<String> ids = new ArrayList<String>();
        for (String id : macAddresses) {
            if (!alreadyMapped(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    LXModel getModelForId(String id) {
        return getModelForId(id, new LXTransform());
    }

    CubesModel.Cube.Type getCubeTypeForId(String id) {
        if (id.equals("SMALLBOI")) {
            return CubesModel.Cube.Type.SMALL;
        }

        if (!CubesLayout.macToPhysid.containsKey(id)) {
            return CubesModel.Cube.Type.LARGE;
        }

        String physId = CubesLayout.macToPhysid.get(id);
        if (!knownCubeTypes.containsKey(physId)) {
            return CubesModel.Cube.Type.LARGE;
        }

        return knownCubeTypes.get(physId);
    }

    LXModel getModelForId(String id, LXTransform t) {
        CubesModel.Cube.Type type = getCubeTypeForId(id);

        String realId;
        if (CubesLayout.macToPhysid.containsKey(id)) {
            realId = CubesLayout.macToPhysid.get(id);
        } else {
            realId = "UNKNOWN";
        }

        return new CubesModel.Cube(realId, 0, 0, 0, 0, 0, 0, t, type);
    }

    ArrayList<LXPoint> centerPoints(ArrayList<LXPoint> input) {
        ArrayList<LXPoint> output = new ArrayList<LXPoint>();
        LXVector avgPoint = new LXVector(0, 0, 0);
        for (LXPoint p : input) {
            avgPoint.add(new LXVector(p));
        }
        avgPoint.div(input.size());
        for (LXPoint p : input) {
            output.add(new LXPoint(p.x - avgPoint.x, p.y - avgPoint.y, p.z - avgPoint.z));
        }
        return output;
    }

    LXPoint[] kyleCubeModel(float ledDistance, float edgeDistance, int ledsPerStrip) {
        int n = ledsPerStrip * 12;
        int m = ledsPerStrip - 1;
        LXVector[] idealVectors = new LXVector[n];
        LXPoint[] idealPoints = new LXPoint[n];
        for (int pixelIndexInCube = 0; pixelIndexInCube < n; pixelIndexInCube++) {
            int pixelIndexInStrip = pixelIndexInCube % ledsPerStrip;
            int stripIndex = pixelIndexInCube / ledsPerStrip;

            if (stripIndex == 0) {
                idealVectors[pixelIndexInCube] = new LXVector(edgeDistance + pixelIndexInStrip, 2 * edgeDistance + m, 0);
            } else if (stripIndex == 1) {
                idealVectors[pixelIndexInCube] = new LXVector(2*edgeDistance+m, edgeDistance+(m-pixelIndexInStrip), 0);
            } else if (stripIndex == 2) {
                idealVectors[pixelIndexInCube] = new LXVector(edgeDistance+(m-pixelIndexInStrip), 0, 0);
            } else if (stripIndex == 3) {
                idealVectors[pixelIndexInCube] = new LXVector(2*edgeDistance+m, 2*edgeDistance+m, edgeDistance+pixelIndexInStrip);
            } else if (stripIndex == 4) {
                idealVectors[pixelIndexInCube] = new LXVector(2*edgeDistance+m, edgeDistance+(m-pixelIndexInStrip), 2*edgeDistance+m);
            } else if (stripIndex == 5) {
                idealVectors[pixelIndexInCube] = new LXVector(2*edgeDistance+m, 0, edgeDistance+(m-pixelIndexInStrip));
            } else if (stripIndex == 6) {
                idealVectors[pixelIndexInCube] = new LXVector(edgeDistance+(m-pixelIndexInStrip), 2*edgeDistance+m, 2*edgeDistance+m);
            } else if (stripIndex == 7) {
                idealVectors[pixelIndexInCube] = new LXVector(0, edgeDistance+(m-pixelIndexInStrip), 2*edgeDistance+m);
            } else if (stripIndex == 8) {
                idealVectors[pixelIndexInCube] = new LXVector(edgeDistance+pixelIndexInStrip, 0, 2*edgeDistance+m);
            } else if (stripIndex == 9) {
                idealVectors[pixelIndexInCube] = new LXVector(0, 2*edgeDistance+m, edgeDistance+(m-pixelIndexInStrip));
            } else if (stripIndex == 10) {
                idealVectors[pixelIndexInCube] = new LXVector(0, edgeDistance+(m-pixelIndexInStrip), 0);
            } else if (stripIndex == 11) {
                idealVectors[pixelIndexInCube] = new LXVector(0, 0, edgeDistance+pixelIndexInStrip);
            }
        }

        float maxX = 0;
        float maxY = 0;
        float maxZ = 0;


        for (int i = 0; i < n; i++) {
            LXVector v = idealVectors[i];
            v.x *= ledDistance;
            v.y *= ledDistance;
            v.z *= ledDistance;
            maxX = max(maxX, v.x);
            maxY = max(maxY, v.y);
            maxZ = max(maxZ, v.z);
        }

        for (int i = 0; i < n; i++) {
            LXVector v = idealVectors[i];
            idealPoints[i] = new LXPoint(v.x, v.y, v.z);
        }


        return idealPoints;
    }

    public LXPoint[] getRawPointsForId(String id) {
        LXModel model = getModelForId(id);
        return model.points;
    }

    List<LXPoint> getPointsForId(String id) {
        LXPoint[] rawPoints = getRawPointsForId(id);
        List<LXPoint> points = new ArrayList<LXPoint>();
        int[] po = getPixelOrder();

        if (po == null || po.length != rawPoints.length) {
            int n = rawPoints.length;
            ArrayList<Integer> orderAr = new ArrayList<Integer>();
            int[] shuffled = new int[n];
            for (int i = 0; i < n; i++) {
                orderAr.add(i);
            }

            Collections.shuffle(orderAr);

            for (int i = 0; i < n; i++) {
                shuffled[i] = orderAr.get(i);
            }

            setPixelOrder(shuffled);

            po = shuffled;
        }

        for (int i : po) {
            points.add(rawPoints[i]);
        }

        return points;
    }

    CVFixture getMappedCube(String id) {
        for (CVFixture c : mappedFixtures) {
            if (c.id.equals(id)) {
                return c;
            }
        }
        return null;
    }

    ArrayList<Double> arrayIfy(PVector vec) {
        ArrayList<Double> ar = new ArrayList<Double>();
        ar.add((double)vec.x);
        ar.add((double)vec.y);
        ar.add((double)vec.z);
        return ar;
    }

    public void saveToJSON() {
        List<Map<String, Object>> jsonCubes = new ArrayList<>();
        for (CVFixture c : mappedFixtures) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.id);
            m.put("transform", c.getRawMatrix());
            m.put("debug", c.getLabel());
            m.put("rvec", arrayIfy(c.rvec));
            m.put("tvec", arrayIfy(c.tvec));
            jsonCubes.add(m);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();


        saveBytes(new File(dataPath(saveFile.getString())), gson.toJson(jsonCubes).getBytes());
    }

    public void loadJSON() {
        String data = new String(loadBytes(new File(dataPath(saveFile.getString()))));
        ArrayList<Object> message = (new Gson()).fromJson(data, new TypeToken<ArrayList<Object>>() {
        }
        .getType());

        for (Object cube_ : message) {
            Map<String, Object> cube = (Map<String, Object>)cube_;
            List<List<Double>> jsonTransform = (List<List<Double>>)cube.get("transform");
            String mac = (String)cube.get("id");

            List<Double> rvec = (List<Double>)cube.get("rvec");
            List<Double> tvec = (List<Double>)cube.get("tvec");

            addCube(jsonTransform, rvec, tvec, mac);
        }
    }

    void setClientsConnected(boolean connected) {
        switch (state.getEnum()) {
        case DISCONNECTED:
            if (connected) {
                state.setValue(AutomappingState.CONNECTED);
            }
            break;
        case CONNECTED:
        case RUNNING:
            if (!connected) {
                state.setValue(AutomappingState.DISCONNECTED);
            }
            break;
        default:
            throw new RuntimeException("Invalid state in setClientsConnected");
        }
    }

    void setPixelOrder(int[] pixelOrder) {
        this.pixelOrder = pixelOrder;
        mod = pixelOrder.length;
    }

    int[] getPixelOrder() {
        return pixelOrder;
    }

    String[] getMacAddresses() {
        return macAddresses;
    }

    void startRunning() {
        running.setValue(true);
        //outputControl.enabled.setValue(true);
        state.setValue(AutomappingState.RUNNING);
        reset();
    }


    void startCalibration() {
        startRunning();
        mode = PatternMode.CALIBRATING;
    }

    void startMapping() {
        startRunning();

        NetworkMonitor networkMonitor = NetworkMonitor.getInstance(lx);

        System.out.println("networkMonitor.networkDevices.size(): "+networkMonitor.networkDevices.size());
        macAddresses = new String[networkMonitor.networkDevices.size()];
        int i = 0;
        for (NetworkDevice device : networkMonitor.networkDevices) {
            macAddresses[i++] = NetworkUtils.macAddrToString(device.macAddress);
        }

        patternState = PatternState.S1_BLACK;
        mode = PatternMode.MAPPING;
    }

    @Override
    protected void onReset() {
        mode = PatternMode.ALL_OFF;
        runthroughCount = -1;
        patternPixelIndex = 0;
        patternState = PatternState.S1_BLACK;
        frameCounter = 0;
        currentCubeId = null;
    }

    void disableAutomapping() {
        reset();
        state.setValue(AutomappingState.CONNECTED);
    }

    void mapNextCube(String id) {
        startRunning();
        System.out.println("THIS IS ID: " + id);
        currentCubeId = id;
        runthroughCount = -1;
        patternPixelIndex = 0;
        frameCounter = 0;
        patternState = PatternState.S1_BLACK;
        mode = PatternMode.MAPPING;
    }

    void showNextCube(String id) {
        startRunning();
        currentCubeId = id;
        runthroughCount = -1;
        patternPixelIndex = 0;
        frameCounter = 0;
        patternState = PatternState.S1_BLACK;
        mode = PatternMode.SHOW_CUBE;
    }

    void showImageForPixel(int pixelIndex) {
        startRunning();
        mode = PatternMode.SHOW_PIXEL;
        showPixelIndex = pixelIndex;
    }

    void showAll(boolean on) {
        startRunning();
        mode = on ? PatternMode.ALL_ON : PatternMode.ALL_OFF;
    }

    void updateCalibrating() {
        if (NUM_CALIBRATION_RUNTHROUGHS >= 0 && runthroughCount >= NUM_CALIBRATION_RUNTHROUGHS) {
            reset();
            return;
        }

        switch (patternState) {
        case S1_BLACK:
            frameCounter = (frameCounter+1) % S1_FRAMES;
            break;

        case S2_WHITE:
            frameCounter = (frameCounter+1) % S2_FRAMES;
            if (frameCounter == 0) {
                runthroughCount++;
            }
            break;

        default:
            throw new RuntimeException("Invalid pattern state in Calibration mode");
        }

        if (frameCounter == 0) {
            patternState = patternState == PatternState.S2_WHITE ? PatternState.S1_BLACK : PatternState.S2_WHITE;
        }
    }

    void updateMapping() {
        if (NUM_RUNTHROUGHS >= 0 && runthroughCount >= NUM_RUNTHROUGHS) {
            reset();
            return;
        }

        switch (patternState) {
        case S0_IDENTIFY:
            frameCounter = (frameCounter+1) % S0_FRAMES;
            if (frameCounter == 0) {
                patternPixelIndex = (patternPixelIndex+1) % mod;
            }
            break;

        case S1_BLACK:
            frameCounter = (frameCounter+1) % S1_FRAMES;
            break;

        case S2_WHITE:
            frameCounter = (frameCounter+1) % (patternPixelIndex == 0 ? RESET_FRAMES : S2_FRAMES);
            if (frameCounter == 0 && patternPixelIndex == 0) {
                runthroughCount++;
            }
            break;

        case S3_BLACK:
            frameCounter = (frameCounter+1) % (patternPixelIndex == 0 ? RESET_BLACK_FRAMES : S3_FRAMES);
            break;

        default:
            throw new RuntimeException("Invalid pattern state in Mapping mode");
        }

        // println(patternState);

        if (frameCounter == 0) {
            int endOrdinal = PatternState.STATE_END.ordinal();
            patternState = PatternState.values[(patternState.ordinal() + 1) % endOrdinal];
        }
    }

    void updateFrame() {
        if (!isRunning()) {
            return;
        }

        switch (mode) {
        case CALIBRATING:
            updateCalibrating();
            return;

        case MAPPING:
        case SHOW_CUBE:
            updateMapping();
            return;

        case SHOW_PIXEL:
        case ALL_ON:
        case ALL_OFF:
            return;
        }
    }

    int mappingColor(String cubeId, int i) {

        switch (patternState) {
        case S0_IDENTIFY:
            if (!cubeId.equals(currentCubeId)) {
                return LXColor.BLACK;
            }

            if (i == -1) {
                return LXColor.WHITE;
            }

            int pixelIndexInCube = getPixelOrder()[patternPixelIndex];
            return i == pixelIndexInCube ? baseColor : LXColor.BLACK;

        case S1_BLACK:
            return LXColor.BLACK;

        case S2_WHITE:
            return resetFrameBaseColor;

        case S3_BLACK:
            return LXColor.BLACK;

        default:
            throw new RuntimeException("Invalid pattern state in Mapping mode");
        }
    }

    public int getPixelColor(String cubeId, int i) {
        if (CubesLayout.macToPhysid.get(cubeId).equals("393")) {
            return LXColor.WHITE; //.hsb(map(i, 0, 105, 0, 360), 50, 100);
            // return LXColor.GREEN;
        }


        AutomappingState s = state.getEnum();

        if (s != AutomappingState.RUNNING) {


            CVFixture c = getMappedCube(cubeId);
            if (c == null) {
                return s == AutomappingState.DISCONNECTED ? LXColor.RED : LXColor.rgb(20, 20, 20);
            }

            //if (p.x == minX && p.y == minY) {
            //    pg.stroke(color(0, 100, 100));
            //} else if (p.x == minX && p.z == minZ) {
            //    pg.stroke(color(120, 100, 100));
            //} else if (p.y == minY && p.z == minZ) {
            //    pg.stroke(color(240, 100, 100));
            //} else {
            //    pg.noStroke();
            //}

            if (c.selected && c.flashOn) {
                return LXColor.WHITE;
            //} else if (i == 0 || i == 20) {
            //    return LXColor.RED;
            //} else if (i == 1 || i == 21) {
            //    return LXColor.GREEN;
            //} else if (i == 2 || i == 22) {
            //    return LXColor.BLUE;
            } else {
                return c.getColor();
            }
        }

        switch (mode) {
        case CALIBRATING:
            return patternState == PatternState.S1_BLACK ? LXColor.BLACK : resetFrameBaseColor;
        case MAPPING:
            return mappingColor(cubeId, i);
        case SHOW_PIXEL:
            return showPixelIndex == i ? LXColor.WHITE : LXColor.BLACK;
        case ALL_ON:
            return LXColor.WHITE;
        case ALL_OFF:
            return LXColor.BLACK;
        case SHOW_CUBE:
            return mappingColor(cubeId, -1);
        default:
            throw new RuntimeException("Invalid mode");
        }
    }
}
