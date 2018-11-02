package com.symmetrylabs.shows.mikey;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.LX;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.List;

public class MikeyShow implements Show {
    public static final String SHOW_NAME = "mikey";

    @Override
    public SLModel buildModel() {
        return MikeyModel.create();
    }

    @Override
    public void setupLx(SLStudioLX lx) {
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.40", (MikeyModel) lx.model);
        lx.addOutput(pixlite);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(strips);
        }

        public static MikeyModel create() {
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();

            for (int i = 0; i < 5; i++) {
                t.push();
                t.rotateZ(Math.PI * 75.f / 180.f);
                Strip.Metrics metrics = new Strip.Metrics(50, 0.75);
                strips.add(new Strip(String.format("strip-%d", i), metrics, t));
                t.pop();
                t.translate(5, 0, 0);
            }

            return new MikeyModel(strips);
        }
    }

    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
            addPixliteOutput(
                new PointsGrouping("1").addPoints(model.getStripByIndex(0).getPoints()));
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getStripByIndex(1).getPoints()));
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getStripByIndex(2).getPoints()));
            addPixliteOutput(
                new PointsGrouping("6").addPoints(model.getStripByIndex(3).getPoints()));
            addPixliteOutput(
                new PointsGrouping("7").addPoints(model.getStripByIndex(4).getPoints()));
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
