package com.symmetrylabs.shows.mikey;

import com.google.common.collect.Lists;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.mikey.ui.UIMikeyModelingTool;
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
        // Single Pixlite outputting to 54 universes (one per strip)
        MikeyPixlite pixlite = new MikeyPixlite(lx, "10.200.1.2", model);
        lx.addOutput(pixlite);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UIMikeyModelingTool tool = new UIMikeyModelingTool(ui, 0, 0, ui.rightPane.model.getContentWidth());
        tool.addToContainer(ui.rightPane.model);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create() {
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();

            float[][] mapping = UIMikeyModelingTool.loadMappingFromDisk();
            if (mapping == null) {
                System.out.println("MikeyShow: using default mapping (54 strips)");
                mapping = buildDefaultMapping();
            }
            System.out.println("MikeyShow: building " + mapping.length + " strips from mapping");
            for (int i = 0; i < mapping.length; i++) {
                float[] m = mapping[i];
                addStrip(m[0], m[1], m[2], m[3], (int) m[4], m[5], t, strips);
            }
            System.out.println("MikeyShow: created model with " + strips.size() + " strips");
            return new MikeyModel(strips);
        }

        private static float[][] buildDefaultMapping() {
            float[][] d = new float[54][6];
            int barSpacing = 24;
            for (int i = 0; i < 54; i++) {
                d[i][0] = barSpacing * i; // tx - spaced in a line
                d[i][1] = 0f;           // ty - same y for all
                d[i][2] = 0f;           // tz - same z for all
                d[i][3] = 1.57f;        // rz
                d[i][4] = 60f;          // px
                d[i][5] = 1f;           // h
            }
            return d;
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
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
            // 54 strips, each strip gets its own universe (universes 0-53)
            for (int i = 0; i < 54; i++) {
                addPixliteOutputMultiUniverse(
                    new PointsGrouping(String.valueOf(i))
                        .addPoints(model.getStripByIndex(i).getPoints()),
                    i,  // firstUniverse = strip index (0-53)
                    1   // maxUniverses = 1 per strip
                );
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
