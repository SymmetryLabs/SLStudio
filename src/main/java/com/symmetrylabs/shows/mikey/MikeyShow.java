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
            int dmx12 = 12;
            double zRotation = 1.57;
            int stripWidth = 1;
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();
            // Strip.Metrics metricsL4W = new Strip.Metrics(2, 2.15f); //strip config

            Strip.Metrics metricsStrip = new Strip.Metrics(ledStrip, stripWidth); //strip config

            Strip.Metrics metricsDMX3 = new Strip.Metrics(dmx3, 5); //strip config
            Strip.Metrics metricsDMX6 = new Strip.Metrics(dmx6, 5); //strip config
            Strip.Metrics metricsDMX9 = new Strip.Metrics(dmx9, 5); //strip config
            Strip.Metrics metricsDMX12 = new Strip.Metrics(dmx12, 5); //strip config




            //FARTHEST TWO LED STRIPS
            //----------------BOX 1 (PIXLITE CHANNELS 1-4)--------------
            t.translate(0, 0, 0);
            t.push();
            t.rotateZ(zRotation);
            Strip strip1 = new Strip("1", metricsStrip, t);         //create the first strip
            strips.add(strip1);  
            t.pop();

            t.push();
            t.translate(10, 0, 0);
            t.rotateZ(zRotation);
            Strip strip2 = new Strip("1", metricsStrip, t);         //create the turn in the first strip
            strips.add(strip2);
            t.pop();


            t.push();
            t.translate(50, 0, 0);
            t.rotateZ(zRotation);
            Strip strip3 = new Strip("1", metricsStrip, t);         //create the turn in the first strip
            strips.add(strip3);
            t.pop();

            //FARTHEST DMX LIGHTS
            t.push();
            t.translate(50, 0, 0);
            t.rotateZ(3.29);
            Strip strip4 = new Strip("1", metricsDMX9, t);         //create the second strip
            strips.add(strip4);                                                  //add the first strip to strip array
            t.pop();
            //----------------BOX 1 --------------


            //----------------BOX 2 (PIXLITE CHANNELS 5-8) ---------------
            t.push();
            t.translate(60, 0, 0);
            t.rotateZ(zRotation);
            t.translate(0,0,0);
            Strip strip5 = new Strip("1", metricsStrip, t);         //create the second strip
            strips.add(strip5);                                                  //add the first strip to strip array
            t.pop();


            t.push();
            t.translate(100, 0, 0);
            t.rotateZ(zRotation);
            Strip strip6 = new Strip("1", metricsStrip, t);         //create the turn in the first strip
            strips.add(strip6);
            t.pop();

            t.push();
            t.translate(110, 0, 0);
            t.rotateZ(zRotation);
            Strip strip7 = new Strip("1", metricsStrip, t);         //create the second strip
            strips.add(strip7);                                                  //add the first strip to strip array
            t.pop();

            //Channel 8
            //2ND SET OF DMX LIGHTS
            t.push();
            t.translate(140, 0, 0);
            t.rotateZ(-3.29);
            Strip strip8 = new Strip("1", metricsDMX9, t);         //create the second strip
            strips.add(strip8);                                                  //add the first strip to strip array
            t.pop();
            //----------------BOX 2 --------------


            //----------------BOX 3 (PIXLITE CHANNELS 9-12)--------------
            t.push();
            t.translate(150, 0, 0);
            t.rotateZ(zRotation);
            Strip strip9 = new Strip("1", metricsStrip, t);         //create the second strip
            strips.add(strip9);                                                  //add the first strip to strip array
            t.pop();

            t.push();
            t.translate(190, 0, 0);
            t.rotateZ(zRotation);
            Strip strip10 = new Strip("1", metricsStrip, t);         //create the second strip
            strips.add(strip10);                                                  //add the first strip to strip array
            t.pop();

            t.push();
            t.translate(200, 0, 0);
            t.rotateZ(zRotation);
            Strip strip11 = new Strip("1", metricsStrip, t);         //create the second strip
            strips.add(strip11);                                                  //add the first strip to strip array
            t.pop();

            //LAST SET OF DMX LIGHTS ON LEFT SIDE
            t.push();
            t.translate(120, 0, 0);
            t.rotateZ(0);
            Strip strip12 = new Strip("1", metricsDMX9, t);         //create the turn in the first strip
            strips.add(strip12);
            t.pop();


            t.push();
            t.translate(160, 0, 0);
            t.rotateZ(zRotation);
            Strip strip13 = new Strip("1", metricsStrip, t);         //create the turn in the first strip
            strips.add(strip13);
            t.pop();


            t.push();
            t.translate(170, 0, 0);
            t.rotateZ(zRotation);
            Strip strip14 = new Strip("1", metricsStrip, t);         //create the turn in the first strip
            strips.add(strip14);
            t.pop();

            t.push();
            t.translate(210, 0, 0);
            t.rotateZ(zRotation);
            Strip strip15 = new Strip("1", metricsStrip, t);         //create the turn in the first strip
            strips.add(strip15);
            t.pop();

            t.push();
            t.translate(220, 0, 0);
            t.rotateZ(zRotation);
            Strip strip16 = new Strip("1", metricsStrip, t);         //create the turn in the first strip
            strips.add(strip16);
            t.pop();


            //FAR RIGHT DMX LIGHTS
            t.push();
            t.translate(220, 0, 0);
            t.rotateZ(0);
            Strip strip17 = new Strip("1", metricsDMX9, t);         //create the turn in the first strip
            strips.add(strip17);
            t.pop();

            //TOP DMX LIGHTS
            t.push();
            t.translate(190, 70, 0);
            t.rotateZ(0);
            Strip strip18 = new Strip("1", metricsDMX9, t);         //create the turn in the first strip
            strips.add(strip18);
            t.pop();
            //----------------BOX 3 --------------


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
            //LED LIGHTS
            addPixliteOutput(
                new PointsGrouping("1").addPoints(model.getStripByIndex(0).getPoints()));
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getStripByIndex(1).getPoints()));
            addPixliteOutput(
                new PointsGrouping("3").addPoints(model.getStripByIndex(2).getPoints()));

            //DMX LIGHTS
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getStripByIndex(3).getPoints()));

            //LED LIGHTS
            addPixliteOutput(
                new PointsGrouping("5").addPoints(model.getStripByIndex(4).getPoints()));
            addPixliteOutput(
                new PointsGrouping("6").addPoints(model.getStripByIndex(5).getPoints()));
            addPixliteOutput(
                new PointsGrouping("7").addPoints(model.getStripByIndex(6).getPoints()));

            //DMX LIGHTS
            addPixliteOutput(
                new PointsGrouping("8").addPoints(model.getStripByIndex(7).getPoints()));

            //LED LIGHTS
            addPixliteOutput(
                new PointsGrouping("9").addPoints(model.getStripByIndex(8).getPoints()));
            addPixliteOutput(
                new PointsGrouping("10").addPoints(model.getStripByIndex(9).getPoints()));
            addPixliteOutput(
                new PointsGrouping("11").addPoints(model.getStripByIndex(10).getPoints()));
            //DMX LIGHTS
            addPixliteOutput(
                new PointsGrouping("12").addPoints(model.getStripByIndex(11).getPoints()));
            addPixliteOutput(
                new PointsGrouping("13").addPoints(model.getStripByIndex(12).getPoints()));
            addPixliteOutput(
                new PointsGrouping("14").addPoints(model.getStripByIndex(13).getPoints()));
            addPixliteOutput(
                new PointsGrouping("15").addPoints(model.getStripByIndex(14).getPoints()));
            addPixliteOutput(
                new PointsGrouping("16").addPoints(model.getStripByIndex(15).getPoints()));
            addPixliteOutput(
                new PointsGrouping("17").addPoints(model.getStripByIndex(16).getPoints()));
            //DMX LIGHTS
            addPixliteOutput(
                new PointsGrouping("21").addPoints(model.getStripByIndex(17).getPoints()));
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
