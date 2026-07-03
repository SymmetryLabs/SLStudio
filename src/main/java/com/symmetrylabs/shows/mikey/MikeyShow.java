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
        MikeyPixlite pixlite1 = new MikeyPixlite(lx, "10.200.1.42", model, 0);      // strips 0-7
        lx.addOutput(pixlite1);
        // MikeyPixlite pixlite2 = new MikeyPixlite(lx, "192.168.0.193", model, 8);      // strips 8-15
        // lx.addOutput(pixlite2);
    }

    public static class MikeyModel extends StripsModel<Strip> {
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
            Strip.Metrics metrics = new Strip.Metrics(307+306+204+204+307+307+204+307, 2.15f); //strip config
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
            t.rotateZ(0);
            Strip strip1 = new Strip("1", metrics, t);    
            strips.add(strip1);  
            t.pop();

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
        
            return new MikeyModel(strips);
        }
    }
    public static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model, int stripOffset) {
            super(lx, ip);
            for (int i = 0; i < 1; i++) {
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
