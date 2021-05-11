package com.symmetrylabs.shows.mikey;

import com.google.common.collect.Lists;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
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
    public void setupLx(LX lx) {
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.100", (MikeyModel) lx.model);
        lx.addOutput(pixlite);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create() {
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();

            for (int i = 0; i < 10; i++) {
                t.push();
                Strip.Metrics metrics = new Strip.Metrics(139*2, 0.75);
                Strip strip = new Strip(String.format("strip-%d", i), metrics, t);
                strips.add(strip);
//                CandyBar bar = new CandyBar();
                t.pop();
                t.translate(0, 2, 0);
            }
            return new MikeyModel(strips);
        }
    }
    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
//            CandyBar bar = new CandyBar();
//            addPixliteOutput(
//                new PointsGrouping("1").addPoints(bar.getPoints()));
            addPixliteOutput(
                new PointsGrouping("3").addPoints(model.getStripByIndex(0).getPoints()));
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getStripByIndex(1).getPoints()));
            addPixliteOutput(
                new PointsGrouping("1").addPoints(model.getStripByIndex(2).getPoints()));
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getStripByIndex(3).getPoints()));
            addPixliteOutput(
                new PointsGrouping("5").addPoints(model.getStripByIndex(4).getPoints()));
            addPixliteOutput(
                new PointsGrouping("6").addPoints(model.getStripByIndex(5).getPoints()));
            addPixliteOutput(
                new PointsGrouping("7").addPoints(model.getStripByIndex(6).getPoints()));
            addPixliteOutput(
                new PointsGrouping("8").addPoints(model.getStripByIndex(7).getPoints()));
            addPixliteOutput(
                new PointsGrouping("9").addPoints(model.getStripByIndex(8).getPoints()));
            addPixliteOutput(
                new PointsGrouping("10").addPoints(model.getStripByIndex(9).getPoints()));
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



//    static class BarConfig {
//        final String id;
//        final float x;
//        final float y;
//        final float z;
//        final float rx;
//        final float ry;
//        final float rz;
//        final int channel;
//
//        BarConfig(String id, float[] coordinates, float[] rotations) {
//            this(id, coordinates, rotations, 0);
//        }
//
//        BarConfig(String id, float[] coordinates, float[] rotations, int channel) {
//            this.id = id;
//            this.x = coordinates[0];
//            this.y = coordinates[1];
//            this.z = coordinates[2];
//            this.rx = rotations[0];
//            this.ry = rotations[1];
//            this.rz = rotations[2];
//            this.channel = channel;
//        }
//    }



}
