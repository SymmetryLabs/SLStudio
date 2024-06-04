package com.symmetrylabs.shows.mikey;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.LX;
import heronarts.lx.transform.LXTransform;
import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;



public class MikeyShow implements Show {
    public static final String SHOW_NAME = "mikey";
    private static final String BASE_PATH = "/Users/Cameron/Desktop/ProgrammingStuff/SLStudio2/SLStudio/src/main/java/com/symmetrylabs/shows/mikey/";

@Override
public SLModel buildModel() {
    try {
        String mainConfigContent = new String(Files.readAllBytes(Paths.get(BASE_PATH + "mainconfig.json")));
        JSONObject mainConfig = new JSONObject(mainConfigContent);
        JSONArray fixtures = mainConfig.getJSONObject("model").getJSONArray("fixtures");

        Map<String, Strip> allStripsMap = new HashMap<>();

        for (int i = 0; i < fixtures.length(); i++) {
            JSONObject fixture = fixtures.getJSONObject(i);
            JSONArray subFixtures = fixture.getJSONArray("fixtures");

            for (int j = 0; j < subFixtures.length(); j++) {
                JSONObject subFixture = subFixtures.getJSONObject(j);
                String panelType = subFixture.getString("panelType");
                double xPos = subFixture.getDouble("xPos");
                double yPos = subFixture.getDouble("yPos");

                String panelContent = new String(Files.readAllBytes(Paths.get(BASE_PATH + panelType + ".json")));
                MeshData meshData = parseJson(panelContent, xPos, yPos);
                Map<String, Strip> stripsMap = createStrips(meshData, panelType);
                allStripsMap.putAll(stripsMap);
            }
        }

        List<Strip> allStrips = new ArrayList<>(allStripsMap.values());
        return new MikeyModel(allStrips, allStripsMap);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
}

@Override
public void setupLx(LX lx) {
    MikeyModel model = (MikeyModel) lx.model;
    List<PointsGrouping> pointsGroupings = new ArrayList<>();

    Map<String, Strip> allStripsMap = model.getAllStripsMap();
    for (Strip strip : model.getStrips()) {
        String stripId = null;
        for (Map.Entry<String, Strip> entry : allStripsMap.entrySet()) {
            if (entry.getValue() == strip) {
                stripId = entry.getKey();
                break;
            }
        }
        if (stripId != null) {
            String panelType = extractPanelTypeFromStripLabel(stripId);
            PointsGrouping pg = findOrCreatePointsGrouping(pointsGroupings, panelType);
            pg.addPoints(strip.getPoints());
        }
    }

    int numPointsGroupings = pointsGroupings.size();
    int numOutputs = 32; // Total number of available outputs across all MikeyPixlite instances

    for (int i = 0; i < numOutputs; i++) {
        int startIndex = i * numPointsGroupings / numOutputs;
        int endIndex = (i + 1) * numPointsGroupings / numOutputs;

        if (i < 11) {
            MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.101", 11);
            try {
                pixlite.configureOutputs(model, pointsGroupings.subList(startIndex, endIndex));
                lx.addOutput(pixlite);
            } catch (Exception e) {
                System.out.println("Error configuring outputs for Pixlite 1: " + e.getMessage());
            }
        } else if (i < 22) {
            MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.102", 11);
            try {
                pixlite.configureOutputs(model, pointsGroupings.subList(startIndex, endIndex));
                lx.addOutput(pixlite);
            } catch (Exception e) {
                System.out.println("Error configuring outputs for Pixlite 2: " + e.getMessage());
            }
        } else {
            MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.103", 10);
            try {
                pixlite.configureOutputs(model, pointsGroupings.subList(startIndex, endIndex));
                lx.addOutput(pixlite);
            } catch (Exception e) {
                System.out.println("Error configuring outputs for Pixlite 3: " + e.getMessage());
            }
        }
    }
}

private String extractPanelTypeFromStripLabel(String stripLabel) {
    // The strip label format is "strip-panelType-row-col"
    String[] parts = stripLabel.split("-");
    if (parts.length == 4) {
        return parts[1];
    }
    return "";
}

private PointsGrouping findOrCreatePointsGrouping(List<PointsGrouping> pointsGroupings, String panelType) {
    for (PointsGrouping pg : pointsGroupings) {
        if (pg.id.equals(panelType)) {
            return pg;
        }
    }
    PointsGrouping newPg = new PointsGrouping(panelType);
    pointsGroupings.add(newPg);
    return newPg;
}

    private MeshData parseJson(String jsonContent, double xPos, double yPos) {
        JSONObject jsonObject = new JSONObject(jsonContent);
        JSONArray coordinates = jsonObject.getJSONArray("coordinates");
        double pitch = jsonObject.getDouble("pitch");
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < coordinates.length(); i++) {
            JSONObject coord = coordinates.getJSONObject(i);
            int row = coord.getInt("row");
            int col = coord.getInt("col");
            points.add(new Point(row, col));
        }
        return new MeshData(points, pitch, xPos, yPos);
    }

    private Map<String, Strip> createStrips(MeshData meshData, String panelType) {
    Map<String, Strip> stripsMap = new HashMap<>();
    LXTransform t = new LXTransform();
    for (Point point : meshData.getPoints()) {
        int row = point.getRow();
        int col = point.getCol();
        t.push();
        t.translate((float) (col * meshData.getPitch() + meshData.getXPos()), (float) (row * meshData.getPitch() + meshData.getYPos()), 0);
        Strip.Metrics metrics = new Strip.Metrics(1, meshData.getPitch());
        String stripId = String.format("strip-%s-%d-%d", panelType, row, col);
        Strip strip = new Strip(stripId, metrics, t);
        stripsMap.put(stripId, strip);
        t.pop();
    }
    return stripsMap;
}

    private static class MeshData {
        private final List<Point> points;
        private final double pitch;
        private final double xPos;
        private final double yPos;

        public MeshData(List<Point> points, double pitch, double xPos, double yPos) {
            this.points = points;
            this.pitch = pitch;
            this.xPos = xPos;
            this.yPos = yPos;
        }

        public List<Point> getPoints() {
            return points;
        }

        public double getPitch() {
            return pitch;
        }

        public double getXPos() {
            return xPos;
        }

        public double getYPos() {
            return yPos;
        }
    }

    private static class Point {
        private final int row;
        private final int col;

        public Point(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }
    }

    static class MikeyModel extends StripsModel<Strip> {
    private final Map<String, Strip> allStripsMap;

    public MikeyModel(List<Strip> strips, Map<String, Strip> allStripsMap) {
        super(SHOW_NAME, strips);
        this.allStripsMap = allStripsMap;
    }

    public Map<String, Strip> getAllStripsMap() {
        return allStripsMap;
    }

    public static MikeyModel create() {
        List<Strip> strips = new ArrayList<>();
        LXTransform t = new LXTransform();
        t.push();
        Strip.Metrics metrics = new Strip.Metrics(278, 0.75);
        Strip strip = new Strip("strip-1", metrics, t);
        strips.add(strip);
        t.pop();
        return new MikeyModel(strips, new HashMap<>()); // Pass an empty map as the second parameter
    }

    public int getNumberOfStrips() {
        return strips.size();
    }

    public Strip getStripByIndex(int index) {
        return strips.get(index);
    }
}

static class MikeyPixlite extends SimplePixlite {
    private final String ip;
    private final int numOutputs;

    public MikeyPixlite(LX lx, String ip, int numOutputs) {
        super(lx, ip);
        this.ip = ip;
        this.numOutputs = numOutputs;
    }

    public void configureOutputs(MikeyModel model, List<PointsGrouping> pointsGroupings) throws Exception {
        int outputIndex = 0;
        for (PointsGrouping pointsGrouping : pointsGroupings) {
            if (outputIndex < numOutputs) {
                addPixliteOutput(pointsGrouping);
                outputIndex++;
            } else {
                System.out.println("Skipping points grouping: " + pointsGrouping.id + ", no available outputs.");
            }
        }
    }

    public SimplePixlite addPixliteOutput(PointsGrouping pointsGrouping) {
        try {
            SimplePixliteOutput spo = new SimplePixliteOutput(pointsGrouping);
            spo.setLogConnections(false);
            addChild(spo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
}
