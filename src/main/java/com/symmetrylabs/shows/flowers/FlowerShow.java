package com.symmetrylabs.shows.flowers;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.LX;
import java.util.ArrayList;
import java.util.List;

public class FlowerShow implements Show {
    public static final String SHOW_NAME = "flower";

    @Override
    public SLModel buildModel() {
        return FlowerModel.create();
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        FlowerPixlite pixlite = new FlowerPixlite(
            lx, "10.200.1.101", (FlowerModel) lx.model);
        lx.addOutput(pixlite);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }

    static class FlowerPixlite extends SimplePixlite {
        public FlowerPixlite(LX lx, String ip, FlowerModel model) {
            super(lx, ip);
            addPixliteOutput(
                new PointsGrouping("1").addPoints(model.getPoints()));
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
}
