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
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.4", (MikeyModel) lx.model);
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
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();
            Strip.Metrics metricsL4W = new Strip.Metrics(verticalBar*4, 2.15f); //strip config
            Strip.Metrics metricsL3W = new Strip.Metrics(verticalBar*3, 2.15f); //strip config
            Strip.Metrics metricsL3WL = new Strip.Metrics(verticalBar*3, 2.05f); //strip config
            Strip.Metrics metricsL3S = new Strip.Metrics(verticalBar*3, 1.5f); //strip config

            Strip.Metrics metricsL2 = new Strip.Metrics(verticalBar*2, 1.25f); //strip config
            Strip.Metrics metricsL1 = new Strip.Metrics(verticalBar*1, .75f); //strip config

            Strip.Metrics metricsS3 = new Strip.Metrics(horizontalBar*3, 1); //strip config
            Strip.Metrics metricsS2 = new Strip.Metrics(horizontalBar*2, 1); //strip config
            Strip.Metrics metricsS1 = new Strip.Metrics(horizontalBar*1, 1); //strip config

            //OUTPUT A1
            t.translate(0, 0, 0);
            t.push();
            t.rotateZ(45);
            Strip strip1 = new Strip("1", metricsL4W, t);         //create the first strip
            strips.add(strip1);  
            t.pop();

            //OUTPUT A2
            t.push();
            t.translate(horizontalBar, 0, 0);
            t.rotateZ(45);
            Strip strip2 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            strips.add(strip2);
            t.pop();

            t.push();
            t.translate(horizontalBar, 0, 0);
            t.rotateZ(45);
            t.translate(((horizontalBar*3)*1.5f)+15, 0, 0);
            t.rotateZ(-45+90+45);
            Strip strip3 = new Strip("1", metricsS1, t);         //create the second strip
            strips.add(strip3);                                                  //add the first strip to strip array
            t.pop();

            //OUTPUT A3
            t.push();
            t.translate(horizontalBar*2, 0, 0);
            t.rotateZ(45);
            Strip strip4 = new Strip("1", metricsL2, t);         //create the turn in the first strip
            strips.add(strip4);
            t.pop();

            t.push();
            t.translate(horizontalBar*2, 0, 0);
            t.rotateZ(45);
            t.translate((verticalBar*2)+12, 0, 0);
            t.rotateZ(-45+90+45);
            Strip strip5 = new Strip("1", metricsS2, t);         //create the second strip
            strips.add(strip5);                                                  //add the first strip to strip array
            t.pop();

            //OUTPUT A4
            t.push();
            t.translate(horizontalBar*3, 0, 0);
            t.rotateZ(45);
            Strip strip6 = new Strip("1", metricsL1, t);         //create the turn in the first strip
            strips.add(strip6);
            t.pop();

            t.push();
            t.translate(horizontalBar*3, 0, 0);
            t.rotateZ(45);
            t.translate(horizontalBar*1, 0, 0);
            t.rotateZ(-45+90+45);
            Strip strip7 = new Strip("1", metricsS3, t);         //create the second strip
            strips.add(strip7);                                                  //add the first strip to strip array
            t.pop();

            //OUTPUT A5
            t.push();
            t.translate(0, 0, 0);
            t.rotateZ(0);
            Strip strip8 = new Strip("1", metricsS3, t);         //create the turn in the first strip
            strips.add(strip8);
            t.pop();

            //OUTPUT B1
            t.push();
            t.translate(horizontalBar*4, 0, 0);
            t.rotateZ(90);
            Strip strip9 = new Strip("1", metricsL1, t);         //create the turn in the first strip
            strips.add(strip9);
            t.pop();

            t.push();
            t.translate(horizontalBar*4, 0, 0);
            t.rotateZ(90);
            t.translate(horizontalBar*1, 0, 0);
            t.rotateZ(-90);
            Strip strip10 = new Strip("1", metricsS3, t);         //create the second strip
            strips.add(strip10);                                                  //add the first strip to strip array
            t.pop();

            //OUTPUT B2
            t.push();
            t.translate(horizontalBar*5, 0, 0);
            t.rotateZ(90);
            Strip strip11 = new Strip("1", metricsL2, t);         //create the turn in the first strip
            strips.add(strip11);
            t.pop();

            t.push();
            t.translate(horizontalBar*5, 0, 0);
            t.rotateZ(90);
            t.translate((verticalBar*2)+8, 0, 0);
            t.rotateZ(-90);
            Strip strip12 = new Strip("1", metricsS2, t);         //create the second strip
            strips.add(strip12);                                                  //add the first strip to strip array
            t.pop();

            //OUTPUT B3
            t.push();
            t.translate(horizontalBar*6, 0, 0);
            t.rotateZ(90);
            Strip strip13 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            strips.add(strip13);
            t.pop();

            t.push();
            t.translate(horizontalBar*6, 0, 0);
            t.rotateZ(90);
            t.translate(((horizontalBar*3)*1.5f)+10, 0, 0);
            t.rotateZ(-90);
            Strip strip14 = new Strip("1", metricsS1, t);         //create the second strip
            strips.add(strip14);                                                  //add the first strip to strip array
            t.pop();

            //OUTPUT B4
            t.push();
            t.translate(horizontalBar*7, 0, 0);
            t.rotateZ(90);
            Strip strip15 = new Strip("1", metricsL4W, t);         //create the turn in the first strip
            strips.add(strip15);
            t.pop();

            //OUTPUT B5
            t.push();
            t.translate(horizontalBar*7, 0, 0);
            t.rotateZ(135.1f);
            Strip strip16 = new Strip("1", metricsS3, t);         //create the turn in the first strip
            strips.add(strip16);
            t.pop();

            return new MikeyModel(strips);
        }
    }
    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
            // for (int i = startStrip; i <= endStrip; i++){
            //     addPixliteOutput(
            //     new PointsGrouping((i+1)+"").addPoints(model.getStripByIndex(i).getPoints()));
            // }
            //A
            addPixliteOutput(
                new PointsGrouping("3").addPoints(model.getStripByIndex(0).getPoints()));
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getStripByIndex(1).getPoints()).addPoints(model.getStripByIndex(2).getPoints()));
            addPixliteOutput(
                new PointsGrouping("5").addPoints(model.getStripByIndex(3).getPoints()).addPoints(model.getStripByIndex(4).getPoints()));
            addPixliteOutput(
                new PointsGrouping("6").addPoints(model.getStripByIndex(5).getPoints()).addPoints(model.getStripByIndex(6).getPoints()));
            addPixliteOutput(
                new PointsGrouping("1").addPoints(model.getStripByIndex(7).getPoints()));
            //B
            addPixliteOutput(
                new PointsGrouping("10").addPoints(model.getStripByIndex(8).getPoints()).addPoints(model.getStripByIndex(9).getPoints()));
            addPixliteOutput(
                new PointsGrouping("9").addPoints(model.getStripByIndex(10).getPoints()).addPoints(model.getStripByIndex(11).getPoints()));
            addPixliteOutput(
                new PointsGrouping("16").addPoints(model.getStripByIndex(12).getPoints()).addPoints(model.getStripByIndex(13).getPoints()));
            addPixliteOutput(
                new PointsGrouping("13").addPoints(model.getStripByIndex(14).getPoints()));
            addPixliteOutput(
                new PointsGrouping("14").addPoints(model.getStripByIndex(15).getPoints()));

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
