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
    private static final float LED_PER_M = 60.0f;
    private static final float LED_PITCH_CM = 100.0f / LED_PER_M;
    public static final String SHOW_NAME = "mikey";

    @Override
    public SLModel buildModel() {
        return MikeyModel.create();
    }

    @Override
    public void setupLx(LX lx) {
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.40", (MikeyModel) lx.model);
        lx.addOutput(pixlite);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(strips);
        }

        public static MikeyModel create() {
            List<Strip> strips = new ArrayList<Strip>();
            Strip.Metrics m = new Strip.Metrics(5, LED_PITCH_CM);

            for (int i = 0; i < 20; i++) {
                LXTransform t = new LXTransform();
                t.translate(1.7f * LED_PITCH_CM * i, i % 2 == 0 ? 0 : 4 * LED_PITCH_CM, 0)
                    .rotateZ(i % 2 == 0 ? Math.PI / 2 : 3 * Math.PI / 2);
                strips.add(new Strip(Integer.toString(i), m, t));
            }

            return new MikeyModel(strips);
        }
    }

    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
            addPixliteOutput(
                new PointsGrouping("1").addPoints(model.getPoints()));
            /*
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getStripByIndex(1).getPoints()));
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getStripByIndex(2).getPoints()));
            addPixliteOutput(
                new PointsGrouping("6").addPoints(model.getStripByIndex(3).getPoints()));
            addPixliteOutput(
                new PointsGrouping("7").addPoints(model.getStripByIndex(4).getPoints()));
            */
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
