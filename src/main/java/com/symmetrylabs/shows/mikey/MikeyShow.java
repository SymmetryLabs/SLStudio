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

            List<Strip> allStrips = new ArrayList<>();

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
                    List<Strip> strips = createStrips(meshData);
                    allStrips.addAll(strips);
                }
            }

            return new MikeyModel(allStrips);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setupLx(LX lx) {
        MikeyModel model = (MikeyModel) lx.model;
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.100");
        pixlite.configureOutputs(model);
        lx.addOutput(pixlite);
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

    private List<Strip> createStrips(MeshData meshData) {
        List<Strip> strips = new ArrayList<>();
        LXTransform t = new LXTransform();
        for (Point point : meshData.getPoints()) {
            int row = point.getRow();
            int col = point.getCol();
            t.push();
            t.translate((float) (col * meshData.getPitch() + meshData.getXPos()), (float) (row * meshData.getPitch() + meshData.getYPos()), 0);
            Strip.Metrics metrics = new Strip.Metrics(1, meshData.getPitch());
            strips.add(new Strip(String.format("strip-%d-%d", row, col), metrics, t));
            t.pop();
        }
        return strips;
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
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }
    



        public static MikeyModel create() {
            List<Strip> strips = new ArrayList<>();
            LXTransform t = new LXTransform();
            t.push();
            Strip.Metrics metrics = new Strip.Metrics(278, 0.75);
            Strip strip = new Strip("strip-1", metrics, t);
            strips.add(strip);
            t.pop();
            return new MikeyModel(strips);
        }

        public int getNumberOfStrips() {
            return strips.size();
        }

        public Strip getStripByIndex(int index) {
            return strips.get(index);
        }
    }

    static class MikeyPixlite extends SimplePixlite {
    public MikeyPixlite(LX lx, String ip) {
        super(lx, ip);
    }

    public void configureOutputs(MikeyModel model) {
        Map<String, PointsGrouping> pointGroupings = new HashMap<>();

        for (int i = 0; i < model.getNumberOfStrips(); i++) {
            Strip strip = model.getStripByIndex(i);
            String fixtureId = "1"; // Extract fixture ID from strip label

            // Create a new point grouping for each fixture if not already created
            PointsGrouping pg = pointGroupings.computeIfAbsent(fixtureId, k -> new PointsGrouping(k));
            pg.addPoints(strip.getPoints());
        }

        // Create a new output for each point grouping and add it to the MikeyPixlite instance
        pointGroupings.values().forEach(this::addPixliteOutput);
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
