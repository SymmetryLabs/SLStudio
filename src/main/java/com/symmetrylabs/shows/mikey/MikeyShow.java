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
        MikeyPixlite pixlite1 = new MikeyPixlite(lx, "10.200.1.2", model, 0);      // strips 0-7
        lx.addOutput(pixlite1);
        MikeyPixlite pixlite2 = new MikeyPixlite(lx, "10.200.1.3", model, 8);      // strips 8-15
        lx.addOutput(pixlite2);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create() {
            int barSpacing = 24;
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();
            float stripRotateZ = 1.57f;
            addStrip(0+barSpacing*0, 0, 0, stripRotateZ, 60, t, strips);  //1 
            addStrip(0+barSpacing*1, 0, 0, stripRotateZ, 60, t, strips);  //2
            addStrip(0+barSpacing*2, 0, 0, stripRotateZ, 60, t, strips);  //3
            addStrip(0+barSpacing*3, 0, 0, stripRotateZ, 60, t, strips);  //4
            addStrip(0+barSpacing*4, 0, 0, stripRotateZ, 60, t, strips);  //5
            addStrip(0+barSpacing*5, 0, 0, stripRotateZ, 60, t, strips);  //6
            addStrip(0+barSpacing*6, 0, 0, stripRotateZ, 60, t, strips);  //7
            addStrip(0+barSpacing*7, 0, 0, stripRotateZ, 60, t, strips);  //8
            //add 8 more strips
            addStrip(0+barSpacing*8, 0, 0, stripRotateZ, 60, t, strips);  //9
            addStrip(0+barSpacing*9, 0, 0, stripRotateZ, 60, t, strips);  //10
            addStrip(0+barSpacing*10, 0, 0, stripRotateZ, 60, t, strips);  //11
            addStrip(0+barSpacing*11, 0, 0, stripRotateZ, 60, t, strips);  //12
            addStrip(0+barSpacing*12, 0, 0, stripRotateZ, 60, t, strips);  //13
            addStrip(0+barSpacing*13, 0, 0, stripRotateZ, 60, t, strips);  //14
            addStrip(0+barSpacing*14, 0, barSpacing, stripRotateZ, 60, t, strips);  //15
            addStrip(0+barSpacing*15, barSpacing, 0, stripRotateZ, 60, t, strips);  //16



            return new MikeyModel(strips);
        }

        private static void addStrip(float translateX, float translateY, float translateZ, float rotateZ, int pixelCount, float height, LXTransform transform, List<Strip> strips) {
            transform.push();
            transform.translate(translateX, translateY, translateZ);
            transform.rotateZ(rotateZ);
            String stripId = String.valueOf(strips.size() + 1);
            strips.add(new Strip(stripId, new Strip.Metrics(pixelCount, height), transform));
            transform.pop();
        }

        private static void addStrip(float translateX, float translateY, float translateZ, float rotateZ, int pixelCount, LXTransform transform, List<Strip> strips) {
            addStrip(translateX, translateY, translateZ, rotateZ, pixelCount, 1, transform, strips);
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
