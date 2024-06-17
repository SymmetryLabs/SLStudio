package com.symmetrylabs.shows.mikey;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.shows.mikey.ForestFlowerModel; 
import com.symmetrylabs.shows.mikey.ParagonModelConfig;
import heronarts.lx.model.LXPoint;
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
import java.util.Collections; // Import added for singletonList

public class MikeyShow implements Show {
    public static final String SHOW_NAME = "mikey";

    @Override
    public SLModel buildModel() {
        try {
            ParagonModelConfig config = ParagonModelConfig.load();
            System.out.println("Loaded ParagonModelConfig");

            if (config == null) {
                System.out.println("Config is null");
                return null;
            }

            ParagonModelConfig.ConfigModel modelConfig = config.model;
            if (modelConfig == null) {
                System.out.println("Model config is null");
                return null;
            }

            if (modelConfig.fixtures == null) {
                System.out.println("Model config fixtures are null");
                return null;
            }

            List<ForestFlowerModel> flowers = new ArrayList<>();
            LXTransform t = new LXTransform();

            System.out.println("Number of fixtures: " + modelConfig.fixtures.length);
            for (ParagonModelConfig.ConfigFixture f : modelConfig.fixtures) {
                System.out.println("Processing fixture: " + f.label + " with fixture type: " + f.fixtureType);
                flowers.addAll(recursiveModelBuilder(f, t));
            }

            SLModel model = new SLModel(SHOW_NAME, new FlowerFixture(flowers));
            System.out.println("SLModel created with " + model.getPoints().size() + " points.");
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class FlowerFixture extends LXAbstractFixture {
        public FlowerFixture(List<ForestFlowerModel> models) {
            for (ForestFlowerModel model : models) {
                List<LXPoint> modelPoints = model.getPoints();
                // System.out.println("Adding " + modelPoints.size() + " points from ForestFlowerModel to FlowerFixture.");
                points.addAll(modelPoints);
            }
            // System.out.println("FlowerFixture created with " + points.size() + " points.");
        }
    }

    private List<ForestFlowerModel> recursiveModelBuilder(ParagonModelConfig.ConfigFixture f, LXTransform t) {
        // System.out.println("Entering recursiveModelBuilder for fixture: " + f.label);
        List<ForestFlowerModel> flowers = new ArrayList<>();
        t.push(new LXMatrix(f.origin));
        if ("flower".equals(f.fixtureType)) {
            ForestFlowerModel flowerModel = new ForestFlowerModel(t, f.id);
            String flowerId = flowerModel.getId();
            System.out.println("FlowerModel created with id: " + flowerId);
            flowers.add(flowerModel);
            // System.out.println("Added ForestFlowerModel with " + flowerModel.getPoints().size() + " points.");
        } else {
            for (ParagonModelConfig.ConfigFixture cf : f.fixtures) {
                flowers.addAll(recursiveModelBuilder(cf, t));
            }
        }
        t.pop();
        return flowers;
    }

    @Override
    public void setupLx(LX lx) {
        // Uncomment and configure if necessary
        // MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.100");
        // pixlite.configureOutputs(model);
        // lx.addOutput(pixlite);
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
        //         PointsGrouping pg = pointGroupings.computeIfAbsent(fixtureId, k -> new PointsGrouping(k));
        //         pg.addPoints(strip.getPoints());
        //     }
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
        // }
    }
}