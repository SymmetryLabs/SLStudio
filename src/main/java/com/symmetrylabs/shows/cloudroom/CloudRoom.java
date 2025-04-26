package com.symmetrylabs.shows.cloudroom;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.Strip;

import com.symmetrylabs.slstudio.model.DoubleStrip;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import java.util.ArrayList;
import java.util.List;
import com.symmetrylabs.slstudio.output.SimpleArtnetController;

public class CloudRoom implements Show {
    public static final String SHOW_NAME = "cloudroom";

    @Override
    public SLModel buildModel() {
        return CloudRoomModel.create();
    }

    @Override
    public void setupLx(LX lx) {
        CloudRoomModel model = (CloudRoomModel) lx.model;
        SimpleArtnetController artnet1 = new SimpleArtnetController(lx, "192.168.0.192"); // example IP, adjust as needed
        lx.addOutput(artnet1);
        SimpleArtnetController artnet2 = new SimpleArtnetController(lx, "192.168.0.193"); // example IP, adjust as needed
        lx.addOutput(artnet2);
    }

    static class CloudRoomModel extends StripsModel<Strip> {
        public CloudRoomModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static CloudRoomModel create() {
            int horizontalBar = 24;
            int barSpacing = 24;
            int verticalBar = 22;
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

            t.translate(barSpacing, 0, 0);
            t.push();
            t.rotateZ(1.57);
            Strip strip9 = new Strip("1", metricsL3S, t);    
            strips.add(strip9);  
            t.pop();

            t.translate(barSpacing, 0, 0);
            t.push();
            t.rotateZ(1.57);
            Strip strip10 = new Strip("1", metricsL3S, t);         //create the turn in the first strip
            strips.add(strip10);
            t.pop();

            return new CloudRoomModel(strips);
        }
    }
}
