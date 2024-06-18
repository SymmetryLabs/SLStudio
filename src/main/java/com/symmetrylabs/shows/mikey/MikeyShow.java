package com.symmetrylabs.shows.mikey;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.LX;
import heronarts.lx.transform.LXTransform;
import heronarts.lx.transform.LXMatrix;
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
        List<ForestFlowerModel> flowers = new ArrayList<>();
        t.push(new LXMatrix(f.origin));
        if ("flower".equals(f.fixtureType)) {
            ForestFlowerModel flowerModel = new ForestFlowerModel(t, f.id);
            String flowerId = flowerModel.getId();
            System.out.println("FlowerModel created with id: " + flowerId);
            flowers.add(flowerModel);
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

        // Distribute points among Pixlite controllers
        LXFixture[] fixtures = ((SLModel) lx.model).fixtures.toArray(new LXFixture[0]);
        List<PointsGrouping> allPointGroupings = new ArrayList<>();

        // Creating PointsGroupings from FlowerModels
        if (fixtures.length > 0 && fixtures[0] instanceof FlowerFixture) {
            FlowerFixture flowerFixture = (FlowerFixture) fixtures[0];
            List<ForestFlowerModel> flowerModels = flowerFixture.getFlowerModels();

            for (int i = 0; i < flowerModels.size(); i += 9) {
                PointsGrouping pg = new PointsGrouping(String.valueOf(allPointGroupings.size()));
                for (int j = 0; j < 9 && (i + j) < flowerModels.size(); j++) {
                    pg.addPoints(flowerModels.get(i + j).getPoints());
                }
                allPointGroupings.add(pg);
            }
        }

        int totalGroupings = allPointGroupings.size();
        int groupingsPerPixlite = 8;

        int groupIndex = 0;
        for (int i = 0; i < pixliteIps.length; i++) {
            MikeyPixlite pixlite = new MikeyPixlite(lx, pixliteIps[i]);
            int start = i * groupingsPerPixlite;
            int end = Math.min(start + groupingsPerPixlite, totalGroupings);

            if (start >= totalGroupings) {
                System.out.println("No more groupings to assign to Pixlite " + pixliteIps[i]);
                continue;
            }

            System.out.println("Pixlite " + pixliteIps[i] + " assigned groupings from " + start + " to " + (end - 1));

            List<PointsGrouping> assignedGroupings = new ArrayList<>();
            for (int j = start; j < end; j++) {
                assignedGroupings.add(allPointGroupings.get(j));
            }

            pixlite.configureOutputs(assignedGroupings, groupIndex);
            groupIndex += assignedGroupings.size();
            lx.addOutput(pixlite);
        }
    }

    static class MikeyPixlite extends SimplePixlite {
        private final String ip;

        public MikeyPixlite(LX lx, String ip) {
            super(lx, ip);
            this.ip = ip;
        }

        public void configureOutputs(List<PointsGrouping> pointGroupings, int startIndex) {
            // Assign PointsGroupings to Pixlite outputs, limiting to 8 outputs per Pixlite
            for (int outputIndex = 0; outputIndex < pointGroupings.size(); outputIndex++) {
                PointsGrouping pg = pointGroupings.get(outputIndex);
                int groupIndex = startIndex + outputIndex;
                System.out.println("Pixlite " + ip + " assigning output index " + (outputIndex + 1) + " to group " + groupIndex);
                addPixliteOutput(String.valueOf(outputIndex + 1), pg);
            }
        }

        // public SimplePixlite addPixliteOutput(String outputNumber, PointsGrouping pointsGrouping) {
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
