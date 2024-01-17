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
        MikeyPixlite pixlite = new MikeyPixlite(lx, "127.0.0.1", (MikeyModel) lx.model);
        lx.addOutput(pixlite);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create() {
            int ledStrip = 96;
            int dmx3 = 3;
            int dmx6 = 6;
            int dmx9 = 9;
            double zRotation = 1.5;
            int stripWidth = 1;
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();
            // Strip.Metrics metricsL4W = new Strip.Metrics(2, 2.15f); //strip config

            Strip.Metrics metricsStrip = new Strip.Metrics(ledStrip, stripWidth); //strip config

            Strip.Metrics metricsDMX3 = new Strip.Metrics(dmx3, stripWidth); //strip config
            Strip.Metrics metricsDMX6 = new Strip.Metrics(dmx6, stripWidth); //strip config
            Strip.Metrics metricsDMX9 = new Strip.Metrics(dmx9, stripWidth); //strip config




            //OUTPUT A1
            t.translate(0, 0, 0);
            t.push();
            t.rotateZ(zRotation);
            Strip strip1 = new Strip("1", metricsStrip, t);         //create the first strip
            strips.add(strip1);  
            t.pop();

            //OUTPUT A2
            t.push();
            t.translate(10, 0, 0);
            t.rotateZ(zRotation);
            Strip strip2 = new Strip("1", metricsStrip, t);         //create the turn in the first strip
            strips.add(strip2);
            t.pop();

            //OUTPUT A3
            t.push();
            t.translate(20, 0, 0);
            t.rotateZ(zRotation);
            Strip strip3 = new Strip("1", metricsStrip, t);         //create the turn in the first strip
            strips.add(strip3);
            t.pop();

            t.push();
            t.translate(30, 0, 0);
            t.rotateZ(zRotation);
            t.translate(0,0,0);
            t.rotateZ(0);
            Strip strip4 = new Strip("1", metricsStrip, t);         //create the second strip
            strips.add(strip4);                                                  //add the first strip to strip array
            t.pop();

            //OUTPUT A4
            t.push();
            t.translate(40, 0, 0);
            t.rotateZ(zRotation);
            Strip strip5 = new Strip("1", metricsStrip, t);         //create the turn in the first strip
            strips.add(strip5);
            t.pop();

            t.push();
            t.translate(50, 0, 0);
            t.rotateZ(zRotation);
            Strip strip6 = new Strip("1", metricsStrip, t);         //create the second strip
            strips.add(strip6);                                                  //add the first strip to strip array
            t.pop();

            //OUTPUT A5
            t.push();
            t.translate(60, 0, 0);
            t.rotateZ(zRotation);
            Strip strip7 = new Strip("1", metricsDMX9, t);         //create the turn in the first strip
            strips.add(strip7);
            t.pop();

            //OUTPUT B1
            t.push();
            t.translate(70, 0, 0);
            t.rotateZ(zRotation);
            Strip strip8 = new Strip("1", metricsDMX9, t);         //create the turn in the first strip
            strips.add(strip8);
            t.pop();

            // t.push();
            // t.translate(horizontalBar*4, 0, 0);
            // t.rotateZ(90);
            // t.translate(horizontalBar*1, 0, 0);
            // t.rotateZ(-90);
            // Strip strip10 = new Strip("1", metricsS3, t);         //create the second strip
            // strips.add(strip10);                                                  //add the first strip to strip array
            // t.pop();

            // //OUTPUT B2
            // t.push();
            // t.translate(horizontalBar*5, 0, 0);
            // t.rotateZ(90);
            // Strip strip11 = new Strip("1", metricsL10, t);         //create the turn in the first strip
            // strips.add(strip11);
            // t.pop();


            // //OUTPUT B3
            // t.push();
            // t.translate(horizontalBar*6, 0, 0);
            // t.rotateZ(90);
            // Strip strip13 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip13);
            // t.pop();

            // t.push();
            // t.translate(horizontalBar*6, 0, 0);
            // t.rotateZ(90);
            // t.translate(((horizontalBar*3)*1.5f)+10, 0, 0);
            // t.rotateZ(-90);
            // Strip strip14 = new Strip("1", metricsS2, t);         //create the second strip
            // strips.add(strip14);                                                  //add the first strip to strip array
            // t.pop();

            // //OUTPUT B4
            // t.push();
            // t.translate(horizontalBar*7, 0, 0);
            // t.rotateZ(90);
            // Strip strip15 = new Strip("1", metricsL4W, t);         //create the turn in the first strip
            // strips.add(strip15);
            // t.pop();

            // //OUTPUT B5
            // t.push();
            // t.translate(horizontalBar*7, 0, 0);
            // t.rotateZ(135.1f);
            // Strip strip16 = new Strip("1", metricsL10, t);         //create the turn in the first strip
            // strips.add(strip16);
            // t.pop();

            // //OUTPUT B4
            // t.push();
            // t.translate(horizontalBar*8, 0, 0);
            // t.rotateZ(90);
            // Strip strip17 = new Strip("1", metricsL10, t);         //create the turn in the first strip
            // strips.add(strip17);
            // t.pop();

            // //OUTPUT B5
            // t.push();
            // t.translate(horizontalBar*9, 0, 0);
            // t.rotateZ(135.1f);
            // Strip strip18 = new Strip("1", metricsL10, t);         //create the turn in the first strip
            // strips.add(strip18);
            // t.pop();
            // //OUTPUT B5
            // t.push();
            // t.translate(horizontalBar*10, 0, 0);
            // t.rotateZ(135.1f);
            // Strip strip19 = new Strip("1", metricsL10, t);         //create the turn in the first strip
            // strips.add(strip19);
            // t.pop();

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
                new PointsGrouping("1").addPoints(model.getStripByIndex(0).getPoints()));
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getStripByIndex(1).getPoints()));
            addPixliteOutput(
                new PointsGrouping("3").addPoints(model.getStripByIndex(2).getPoints()));
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getStripByIndex(3).getPoints()));
            addPixliteOutput(
                new PointsGrouping("5").addPoints(model.getStripByIndex(4).getPoints()));
            // //B
            addPixliteOutput(
                new PointsGrouping("6").addPoints(model.getStripByIndex(5).getPoints()));
            addPixliteOutput(
                new PointsGrouping("7").addPoints(model.getStripByIndex(6).getPoints()));
            addPixliteOutput(
                new PointsGrouping("8").addPoints(model.getStripByIndex(7).getPoints()));

        //     addPixliteOutput(
        //         new PointsGrouping("9").addPoints(model.getStripByIndex(13).getPoints()));
        //     addPixliteOutput(
        //         new PointsGrouping("10").addPoints(model.getStripByIndex(14).getPoints()));
        //     addPixliteOutput(
        //         new PointsGrouping("11").addPoints(model.getStripByIndex(15).getPoints()));
        //     addPixliteOutput(
        //         new PointsGrouping("12").addPoints(model.getStripByIndex(16).getPoints()));
        //     addPixliteOutput(
        //         new PointsGrouping("13").addPoints(model.getStripByIndex(17).getPoints()));

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
