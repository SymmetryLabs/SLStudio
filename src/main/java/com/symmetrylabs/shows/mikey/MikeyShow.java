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
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.129", (MikeyModel) lx.model);
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
            int dmxLightsOffset = -20;
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
            //Strip-1
            t.translate(0, 0, 0);
            t.push();
            t.rotateZ(zRotation);
            Strip strip1 = new Strip("1", metricsDMX12, t);         //create the first strip
            strips.add(strip1);  
            t.pop();

            t.translate(12, 0, 24);
            t.push();
            t.rotateZ(zRotation);
            Strip strip2 = new Strip("1", metricsDMX12, t);         //create the first strip
            strips.add(strip2);  
            t.pop();

            t.translate(24, 0, 24*2);
            t.push();
            t.rotateZ(zRotation);
            Strip strip3 = new Strip("1", metricsDMX12, t);         //create the first strip
            strips.add(strip3);  
            t.pop();

            t.translate(36, 0, 24*3);
            t.push();
            t.rotateZ(zRotation);
            Strip strip4 = new Strip("1", metricsDMX12, t);         //create the first strip
            strips.add(strip4);  
            t.pop();



            return new MikeyModel(strips);
        }
    }
    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
            //BOX 1
            //LED LIGHTS
            addPixliteOutput(
                new PointsGrouping("1").addPoints(model.getStripByIndex(0).getPoints()));
            addPixliteOutput(
                new PointsGrouping("2").addPoints(model.getStripByIndex(1).getPoints()));
            addPixliteOutput(
                new PointsGrouping("3").addPoints(model.getStripByIndex(2).getPoints()));
            addPixliteOutput(
                new PointsGrouping("4").addPoints(model.getStripByIndex(3).getPoints()));

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
