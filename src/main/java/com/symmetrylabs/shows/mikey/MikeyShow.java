package com.symmetrylabs.shows.mikey;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
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

public class MikeyShow implements Show {
    public static final String SHOW_NAME = "mikey";

    @Override
public SLModel buildModel() {
    String jsonContent;
    try {
        jsonContent = new String(Files.readAllBytes(Paths.get("/Users/Cameron/Desktop/ProgrammingStuff/SLStudio2/SLStudio/src/main/java/com/symmetrylabs/shows/mikey/type-A_mesh.json")));
        JSONObject jsonObject = new JSONObject(jsonContent);
        JSONArray coordinates = jsonObject.getJSONArray("coordinates");
        double pitch = jsonObject.getDouble("pitch");  // Assuming pitch is in meters or suitable unit
        List<Strip> strips = new ArrayList<>();

        LXTransform t = new LXTransform();
        for (int i = 0; i < coordinates.length(); i++) {
            JSONObject coord = coordinates.getJSONObject(i);
            int row = coord.getInt("row");
            int col = coord.getInt("col");

            t.push();
            t.translate((float) (col * pitch), (float) (row * pitch), 0);  // Adjusts x and y positions based on pitch
            Strip.Metrics metrics = new Strip.Metrics(1, pitch); // Assuming each point as a single strip for simplicity
            strips.add(new Strip(String.format("strip-%d-%d", row, col), metrics, t));
            t.pop();
        }
        return new MikeyModel(strips);
    } catch (Exception e) {
        e.printStackTrace();
        return null; // Handle errors appropriately
    }
}

    @Override
    public void setupLx(LX lx) {
        MikeyModel model = (MikeyModel) lx.model;
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.100");
        pixlite.configureOutputs(model);
        lx.addOutput(pixlite);
    }

    static class MikeyModel extends StripsModel<Strip> {
        private List<Strip> strips;

        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
            this.strips = strips;
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
            for (int i = 0; i < model.getNumberOfStrips(); i++) {
                PointsGrouping pg = new PointsGrouping(String.valueOf(i + 1));
                pg.addPoints(model.getStripByIndex(i).getPoints());
                addPixliteOutput(pg);
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
