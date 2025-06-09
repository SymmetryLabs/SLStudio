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
        MikeyModel model = (MikeyModel) lx.model;
        MikeyPixlite pixlite1 = new MikeyPixlite(lx, "192.168.0.192", model, 0);      // strips 0-7
        lx.addOutput(pixlite1);
        MikeyPixlite pixlite2 = new MikeyPixlite(lx, "192.168.0.193", model, 8);      // strips 8-15
        lx.addOutput(pixlite2);
        //add a total of 12 controllers
        MikeyPixlite pixlite3 = new MikeyPixlite(lx, "192.168.0.194", model, 16);      // strips 16-23
        lx.addOutput(pixlite3); 
        MikeyPixlite pixlite4 = new MikeyPixlite(lx, "192.168.0.195", model, 24);      // strips 24-31
        lx.addOutput(pixlite4); 
        MikeyPixlite pixlite5 = new MikeyPixlite(lx, "192.168.0.196", model, 32);      // strips 32-39
        lx.addOutput(pixlite5); 
        MikeyPixlite pixlite6 = new MikeyPixlite(lx, "192.168.0.197", model, 40);      // strips 40-47
        lx.addOutput(pixlite6); 
        MikeyPixlite pixlite7 = new MikeyPixlite(lx, "192.168.0.198", model, 48);      // strips 48-55
        lx.addOutput(pixlite7); 
        MikeyPixlite pixlite8 = new MikeyPixlite(lx, "192.168.0.199", model, 56);      // strips 56-63
        lx.addOutput(pixlite8); 
        MikeyPixlite pixlite9 = new MikeyPixlite(lx, "192.168.0.200", model, 64);      // strips 64-71
        lx.addOutput(pixlite9); 
        MikeyPixlite pixlite10 = new MikeyPixlite(lx, "192.168.0.201", model, 72);      // strips 72-79
        lx.addOutput(pixlite10); 
        MikeyPixlite pixlite11 = new MikeyPixlite(lx, "192.168.0.202", model, 80);      // strips 80-87
        lx.addOutput(pixlite11); 
        MikeyPixlite pixlite12 = new MikeyPixlite(lx, "192.168.0.203", model, 88);      // strips 88-95
        lx.addOutput(pixlite12); 
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

            // t.translate(0, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip1 = new Strip("1", metricsL3S, t);    
            // strips.add(strip1);  
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip2 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip2);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip3 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip3);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip4 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip4);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip5 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip5);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip6 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip6);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip7 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip7);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip8 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip8);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip9 = new Strip("1", metricsL3S, t);    
            // strips.add(strip9);  
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip10 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip10);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip11 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip11);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip12 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip12);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip13 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip13);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip14 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip14);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip15 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip15);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip16 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip16);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip17 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip17);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip18 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip18);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip19 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip19);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip20 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip20);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip21 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip21);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip22 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip22);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip23 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip23);
            // t.pop();

            // t.translate(barSpacing, 0, 0);
            // t.push();
            // t.rotateZ(1.57);
            // Strip strip24 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            // strips.add(strip24);
            // t.pop();

            for (int i = 0; i < 96; i++) {
                t.translate(barSpacing, 0, 0);
                t.push();
                t.rotateZ(1.57);
                Strip strip = new Strip("1", metricsL3S, t);         //create the turn in the first strip
                strips.add(strip);
                t.pop();
            }


            

        
            return new MikeyModel(strips);
        }
    }
    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model, int stripOffset) {
            super(lx, ip);
            for (int i = 0; i < 8; i++) {
                addPixliteOutput(
                    new PointsGrouping(String.valueOf(i + 1))
                        .addPoints(model.getStripByIndex(i + stripOffset).getPoints()));
            }
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
