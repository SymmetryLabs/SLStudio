package com.symmetrylabs.shows.mikey;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.shows.hhgarden.FlowerModel;
import com.symmetrylabs.shows.mikey.ParagonModelConfig;
import heronarts.lx.model.LXAbstractFixture;

import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.LX;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXMatrix;
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

    @Override
    public SLModel buildModel() {
        try {
            ParagonModelConfig config = ParagonModelConfig.load();
            List<FlowerModel> flowers = new ArrayList<>();
            LXTransform t = new LXTransform();

            // Correctly accessing the fixtures through modelConfig
            ParagonModelConfig.ConfigModel modelConfig = config.model;  // Assuming 'config' is an instance of ParagonModelConfig
            if (modelConfig != null && modelConfig.fixtures != null) {
                for (ParagonModelConfig.ConfigFixture f : modelConfig.fixtures) {
                    flowers.addAll(recursiveModelBuilder(f, t));
                }
            }
            return new SLModel(SHOW_NAME, new FlowerFixture(flowers));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class FlowerFixture extends LXAbstractFixture {
        public FlowerFixture(List<FlowerModel> models) {
            for (FlowerModel model : models) {
                points.addAll(model.getPoints());
            }
        }
    }

    private List<FlowerModel> recursiveModelBuilder(ParagonModelConfig.ConfigFixture f, LXTransform t) {
        List<FlowerModel> flowers = new ArrayList<>();
        t.push(new LXMatrix(f.origin));
        if ("flower".equals(f.fixtureType)) {
            flowers = List.of(new FlowerModel(t));
        } else {
            for (ParagonModelConfig.ConfigFixture cf : f.fixtures) {
                flowers.addAll(recursiveModelBuilder(cf, t));
            }
        }
        t.pop();

        return flowers;
    }

    // private static class Fixture extends LXAbstractFixture {
    //     private Fixture(List<FlowerModel> flowers) {
    //         for (Flower flower : flowers) {
    //             for (LXPoint p : flower.points) {
    //                 this.points.add(p);
    //             }
    //         }
    //     }
    // }


    @Override
    public void setupLx(LX lx) {
    // MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.100");
    //     pixlite.configureOutputs(model);
    //     lx.addOutput(pixlite);
    }


    static class MikeyPixlite extends SimplePixlite {
    public MikeyPixlite(LX lx, String ip) {
        super(lx, ip);
    }

    // public void configureOutputs(MikeyModel model) {
    //     Map<String, PointsGrouping> pointGroupings = new HashMap<>();

    //     for (int i = 0; i < model.getNumberOfStrips(); i++) {
    //         Strip strip = model.getStripByIndex(i);
    //         String fixtureId = "1"; // Extract fixture ID from strip label

    //         // Create a new point grouping for each fixture if not already created
    //         PointsGrouping pg = pointGroupings.computeIfAbsent(fixtureId, k -> new PointsGrouping(k));
    //         pg.addPoints(strip.getPoints());
    //     }

    //     // Create a new output for each point grouping and add it to the MikeyPixlite instance
    //     pointGroupings.values().forEach(this::addPixliteOutput);
    // }

    // public SimplePixlite addPixliteOutput(PointsGrouping pointsGrouping) {
    //     try {
    //         SimplePixliteOutput spo = new SimplePixliteOutput(pointsGrouping);
    //         spo.setLogConnections(false);
    //         addChild(spo);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return this;
    //     }
    }
}
