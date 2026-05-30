package com.symmetrylabs.shows.mikey;

import com.google.common.collect.Lists;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.mikey.ui.UIMikeyModelingTool;
import static com.symmetrylabs.shows.mikey.ui.UIMikeyModelingTool.UNIVERSE_COUNT;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.model.DoubleStrip;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
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

    public static StripIlluminator illuminator;
    public static LXChannel illumChannel;

    @Override
    public void setupLx(LX lx) {
        MikeyModel model = (MikeyModel) lx.model;
        MikeyPixlite pixlite = new MikeyPixlite(lx, "192.168.1.50", model);
        lx.addOutput(pixlite);
        // Dedicated ADD-blend channel: always runs, adds white on top when a strip is selected
        illumChannel = lx.engine.addChannel();
        illumChannel.label.setValue("StripIllum");
        illumChannel.fader.setValue(1.0);
        illumChannel.blendMode.setValue(0);  // index 0 = AddBlend
        illuminator = new StripIlluminator(lx);
        illumChannel.addPattern(illuminator);
        illumChannel.goPattern(illuminator);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UIMikeyModelingTool tool = new UIMikeyModelingTool(ui, 0, 0, ui.rightPane.model.getContentWidth());
        tool.addToContainer(ui.rightPane.model);
        // Wire live strip list so dragging params moves pixels in the 3D view
        MikeyModel model = (MikeyModel) lx.model;
        tool.setModel(model, model.getStrips());
        tool.setLX(lx);
        // Set camera to center on model and use the model's bounding box for radius.
        // SLStudio.onUIReady hardcodes setMaxRadius(150*FEET=1800) and setRadius(25*FEET=300)
        // before calling setupUi, so we must override both here.
        float cx = model.cx;
        float cy = model.cy;
        float cz = model.cz;
        float viewRadius = Math.max(model.xRange, Math.max(model.yRange, model.zRange)) * 0.75f;
        if (viewRadius < 100) viewRadius = 100;
        System.out.println("MikeyShow: model bounds x=" + model.xMin + ".." + model.xMax +
            " y=" + model.yMin + ".." + model.yMax + " center=(" + cx + "," + cy + "," + cz + ")" +
            " xRange=" + model.xRange + " viewRadius=" + viewRadius);
        ui.preview.setRadiusBounds(1, Float.MAX_VALUE);
        ui.preview.setCenter(cx, cy, cz);
        ui.preview.setRadius(viewRadius);
        // Increase depth field so near clip never eats points during panning
        ui.preview.depth.setValue(2);
        System.out.println("MikeyShow: camera set center=(" + cx + "," + cy + "," + cz + ") radius=" + viewRadius);
    }

    static class MikeyModel extends StripsModel<Strip> {
        public MikeyModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static MikeyModel create() {
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();

            int[] counts = UIMikeyModelingTool.loadStripCountsFromDisk();
            float[][] mapping = UIMikeyModelingTool.loadStripsFromDisk();
            int totalStrips = 0;
            for (int c : counts) totalStrips += c;
            if (mapping == null || mapping.length != totalStrips) {
                System.out.println("MikeyShow: using default mapping (" + totalStrips + " strips)");
                mapping = buildDefaultMapping(totalStrips);
            }
            System.out.println("MikeyShow: building " + mapping.length + " strips from mapping");
            for (int i = 0; i < mapping.length; i++) {
                float[] m = mapping[i];
                float az = m[3] > 180f ? m[3] - 360f : m[3];  // map 0-360 to -180..+180
                float rotZRad = (float) Math.toRadians(-az);  // negate: Mad Mapper uses clockwise-positive
                addStrip(m[0], m[1], m[2], rotZRad, (int) m[4], m[5], t, strips);
            }
            System.out.println("MikeyShow: created model with " + strips.size() + " strips");
            return new MikeyModel(strips);
        }

        private static float[][] buildDefaultMapping(int totalStrips) {
            float[][] d = new float[totalStrips][6];
            int barSpacing = 24;
            for (int i = 0; i < totalStrips; i++) {
                d[i][0] = barSpacing * i; // tx - spaced in a line
                d[i][1] = 0f;            // ty - same y for all
                d[i][2] = 0f;            // tz - same z for all
                d[i][3] = 90f;           // rz (degrees)
                d[i][4] = 60f;           // px
                d[i][5] = 1f;            // h
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
            // UNIVERSE_COUNT outputs; each output carries a variable number of strips per universe
            int[] counts = UIMikeyModelingTool.loadStripCountsFromDisk();
            int[] blackOffsets = UIMikeyModelingTool.loadBlackOffsetsFromDisk();
            int stripIndex = 0;
            for (int u = 0; u < UNIVERSE_COUNT; u++) {
                PointsGrouping pg = new PointsGrouping(String.valueOf(u + 1));
                for (int s = 0; s < counts[u]; s++) {
                    // Prepend black offset pixels for this strip (index=-1, outputs as black)
                    int bk = (stripIndex < blackOffsets.length) ? blackOffsets[stripIndex] : 0;
                    pg.addBlackPixels(bk);
                    pg.addPoints(model.getStripByIndex(stripIndex++).getPoints());
                }
                addPixliteOutput(pg);
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
