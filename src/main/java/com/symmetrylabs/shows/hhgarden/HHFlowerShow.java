package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.LX;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.transform.LXVector;
import java.util.ArrayList;
import java.util.List;

public class HHFlowerShow implements Show {
    public static final String SHOW_NAME = "hhflower";

    @Override
    public SLModel buildModel() {
        ArrayList<FlowerModel> flowers = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            FlowerData fd = new FlowerData(new LXVector(14.f * (int) (i / 3), 0, 14.f * (i % 3)));
            flowers.add(FlowerModel.create(fd));
        }
        return new SLModel(new FlowerFixture(flowers));
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        FlowerPixlite pixlite = new FlowerPixlite(lx, "10.200.1.255", lx.model);
        lx.addOutput(pixlite);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }

    static class FlowerPixlite extends SimplePixlite {
        public FlowerPixlite(LX lx, String ip, LXModel model) {
            super(lx, ip);
            addPixliteOutput(
                new PointsGrouping("1").addPoints(model.getPoints()));
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getPoints()));
            addPixliteOutput(
                new PointsGrouping("3").addPoints(model.getPoints()));
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getPoints()));
        }

        @Override
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

    private static class FlowerFixture extends LXAbstractFixture {
        public FlowerFixture(List<FlowerModel> models) {
            for (FlowerModel model : models) {
                points.addAll(model.getPoints());
            }
        }
    }
}
