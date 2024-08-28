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
import com.symmetrylabs.slstudio.model.DoubleStrip;
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
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.0.0.42", (MikeyModel) lx.model);
        lx.addOutput(pixlite);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create() {
            int barAngle = -60;
            int spacing = -60;
            int verticalBar = 22;
            int horizontalBar = 24;
            int barSpacing = 24;
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();
            Strip.Metrics metricsL4W = new Strip.Metrics(verticalBar*4, 2.15f); //strip config
            Strip.Metrics metricsL3W = new Strip.Metrics(verticalBar*3, 2.15f); //strip config
            Strip.Metrics metricsL3WL = new Strip.Metrics(verticalBar*3, 2.05f); //strip config
            Strip.Metrics metricsL3S = new Strip.Metrics(60, 1.5f); //strip config

            Strip.Metrics metricsL2 = new Strip.Metrics(verticalBar*2, 1.25f); //strip config
            Strip.Metrics metricsL1 = new Strip.Metrics(verticalBar*1, .75f); //strip config

            Strip.Metrics metricsL10 = new Strip.Metrics(horizontalBar*10, 1); //strip config

            Strip.Metrics metricsS3 = new Strip.Metrics(horizontalBar*3, 1); //strip config
            Strip.Metrics metricsS2 = new Strip.Metrics(horizontalBar*2, 1); //strip config
            Strip.Metrics metricsS1 = new Strip.Metrics(horizontalBar*1, 1); //strip config

            t.translate(0, 0, 0);
            t.push();
            t.rotateZ(1.57);
            Strip strip1 = new Strip("1", metricsL3S, t);    
            strips.add(strip1);  
            t.pop();

            t.translate(barSpacing, 0, 0);
            t.push();
            t.rotateZ(1.57);
            Strip strip2 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            strips.add(strip2);
            t.pop();

            t.translate(barSpacing, 0, 0);
            t.push();
            t.rotateZ(1.57);
            Strip strip3 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            strips.add(strip3);
            t.pop();

            t.translate(barSpacing, 0, 0);
            t.push();
            t.rotateZ(1.57);
            Strip strip4 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            strips.add(strip4);
            t.pop();

            t.translate(barSpacing, 0, 0);
            t.push();
            t.rotateZ(1.57);
            Strip strip5 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            strips.add(strip5);
            t.pop();

            t.translate(barSpacing, 0, 0);
            t.push();
            t.rotateZ(1.57);
            Strip strip6 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            strips.add(strip6);
            t.pop();

            t.translate(barSpacing, 0, 0);
            t.push();
            t.rotateZ(1.57);
            Strip strip7 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            strips.add(strip7);
            t.pop();

            t.translate(barSpacing, 0, 0);
            t.push();
            t.rotateZ(1.57);
            Strip strip8 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            strips.add(strip8);
            t.pop();

            return new MikeyModel(strips);
        }
    }
    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
            addPixliteOutput(
                new PointsGrouping("31").addPoints(model.getStripByIndex(0).getPoints()));
            addPixliteOutput(
                new PointsGrouping("29").addPoints(model.getStripByIndex(1).getPoints()));

            addPixliteOutput(
                new PointsGrouping("22").addPoints(model.getStripByIndex(2).getPoints()));
            addPixliteOutput(
                new PointsGrouping("21").addPoints(model.getStripByIndex(3).getPoints()));
            addPixliteOutput(
                new PointsGrouping("19").addPoints(model.getStripByIndex(4).getPoints()));
            addPixliteOutput(
                new PointsGrouping("20").addPoints(model.getStripByIndex(5).getPoints()));
            addPixliteOutput(
                new PointsGrouping("28").addPoints(model.getStripByIndex(6).getPoints()));
            addPixliteOutput(
                new PointsGrouping("27").addPoints(model.getStripByIndex(7).getPoints()));

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
