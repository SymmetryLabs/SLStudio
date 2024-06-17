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
import heronarts.lx.model.LXFixture;
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

public class MikeyShow implements Show  {
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

            FlowerFixture flowerFixture = new FlowerFixture(flowers);
            SLModel model = new SLModel(SHOW_NAME, flowerFixture);
            System.out.println("SLModel created with " + model.getPoints().size() + " points.");
            return model;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static class FlowerFixture extends LXAbstractFixture {
        private List<ForestFlowerModel> flowerModels;

        public FlowerFixture(List<ForestFlowerModel> models) {
            this.flowerModels = models;
            for (ForestFlowerModel model : models) {
                List<LXPoint> modelPoints = model.getPoints();
                points.addAll(modelPoints);
            }
        }
    public List<ForestFlowerModel> getFlowerModels() {
            return flowerModels;
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
    // Create an array of IP addresses for the 8 Pixlites
        String[] pixliteIps = {
            "10.200.1.101", "10.200.1.102", "10.200.1.103", "10.200.1.104",
            "10.200.1.105", "10.200.1.106", "10.200.1.107", "10.200.1.108"
        };

        // Create an instance of MikeyPixlite for each IP address and configure its outputs
        for (String ip : pixliteIps) {
            MikeyPixlite pixlite = new MikeyPixlite(lx, ip);
            pixlite.configureOutputs((SLModel) lx.model);
            lx.addOutput(pixlite);
        }
    }

    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip) {
            super(lx, ip);
        }

    public void configureOutputs(SLModel model) {
            LXFixture[] fixtures = model.fixtures.toArray(new LXFixture[0]);
            if (fixtures.length > 0 && fixtures[0] instanceof FlowerFixture) {
                FlowerFixture flowerFixture = (FlowerFixture) fixtures[0];
                List<ForestFlowerModel> flowerModels = flowerFixture.getFlowerModels();


            Map<Integer, PointsGrouping> pointGroupings = new HashMap<>();
            int groupIndex = 0;
            for (int i = 0; i < flowerModels.size(); i++) {
                ForestFlowerModel flowerModel = flowerModels.get(i);
                int fixtureId = groupIndex;

                PointsGrouping pg = pointGroupings.computeIfAbsent(fixtureId, k -> new PointsGrouping(String.valueOf(k)));
                pg.addPoints(flowerModel.getPoints());

                if ((i + 1) % 9 == 0) {
                    groupIndex++;
                }
            }

            // Assign PointsGroupings to Pixlite outputs, limiting to 8 outputs per Pixlite
            int outputIndex = 1;
            for (PointsGrouping pg : pointGroupings.values()) {
                addPixliteOutput(String.valueOf(outputIndex), pg);
                outputIndex++;
                if (outputIndex > 8) {
                    break;
                }
            }
        }
    }

    // public SimplePixlite addPixliteOutput(String outputNumber, PointsGrouping pointsGrouping) {
    //     try {
    //         SimplePixliteOutput spo = new SimplePixliteOutput(pointsGrouping);
    //         spo.setLogConnections(false);
    //         // Use the appropriate method to set the output number
    //         // For example: spo.setOutputIndex(Integer.parseInt(outputNumber));
    //         addChild(spo);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return this;
    //     }
    }
}