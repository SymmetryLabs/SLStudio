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
        // GROUPS tab: assign strips to 12 groups used by the GroupStripFilter effect
        com.symmetrylabs.shows.mikey.ui.UIStripGroupTool groupTool =
            new com.symmetrylabs.shows.mikey.ui.UIStripGroupTool(ui, 0, 0, ui.rightPane.groups.getContentWidth());
        groupTool.addToContainer(ui.rightPane.groups);
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
                float az, rx, ry, d, cv;
                int px;
                boolean grbSwap;
                if (m.length >= 10) {
                    // New 10-col format: [tx,ty,tz,az,rx,ry,px,d,cv,grb]
                    az = m[3]; rx = m[4]; ry = m[5]; px = (int) m[6]; d = m[7]; cv = m[8];
                    grbSwap = m[9] > 0.5f;
                } else if (m.length == 9) {
                    // 9-col format: [tx,ty,tz,az,rx,ry,px,d,cv] — no grb
                    az = m[3]; rx = m[4]; ry = m[5]; px = (int) m[6]; d = m[7]; cv = m[8];
                    grbSwap = false;
                } else if (m.length == 8) {
                    // 8-col format: [tx,ty,tz,az,rx,ry,px,d] — no cv/grb
                    az = m[3]; rx = m[4]; ry = m[5]; px = (int) m[6]; d = m[7]; cv = 0f;
                    grbSwap = false;
                } else {
                    // Old 6-col format: [tx,ty,tz,az,px,d] — no rx/ry/cv/grb
                    az = m[3]; rx = 0f; ry = 0f; px = (int) m[4]; d = m[5]; cv = 0f;
                    grbSwap = false;
                }
                az = az > 180f ? az - 360f : az;
                rx = rx > 180f ? rx - 360f : rx;
                ry = ry > 180f ? ry - 360f : ry;
                float rotZRad = (float) Math.toRadians(-az);
                float rotXRad = (float) Math.toRadians(-rx);
                float rotYRad = (float) Math.toRadians(-ry);
                addStrip(m[0], m[1], m[2], rotXRad, rotYRad, rotZRad, px, d, cv, grbSwap, t, strips);
            }
            System.out.println("MikeyShow: created model with " + strips.size() + " strips");
            return new MikeyModel(strips);
        }

        private static float[][] buildDefaultMapping(int totalStrips) {
            float[][] d = new float[totalStrips][10];
            int barSpacing = 24;
            for (int i = 0; i < totalStrips; i++) {
                d[i][0] = barSpacing * i; // tx
                d[i][1] = 0f;            // ty
                d[i][2] = 0f;            // tz
                d[i][3] = 90f;           // az
                d[i][4] = 0f;            // rx
                d[i][5] = 0f;            // ry
                d[i][6] = 60f;           // px
                d[i][7] = 1f;            // d
                d[i][8] = 0f;            // cv
                d[i][9] = 0f;            // grb (0 = RGB, 1 = GRB)
            }
            return d;
        }

        private static void addStrip(float tx, float ty, float tz, float rotX, float rotY, float rotZ, int pixelCount, float height, float curve, boolean grbSwap, LXTransform transform, List<Strip> strips) {
            transform.push();
            transform.translate(tx, ty, tz);
            transform.rotateX(rotX);
            transform.rotateY(rotY);
            transform.rotateZ(rotZ);
            String stripId = String.valueOf(strips.size() + 1);
            Strip.Metrics metrics = new Strip.Metrics(pixelCount, height);
            metrics.grbSwap = grbSwap;  // Store GRB flag in metrics
            Strip strip = new Strip(stripId, metrics, transform);
            // Apply bezier curve displacement in local Z after strip is placed
            if (curve != 0f) {
                List<heronarts.lx.model.LXPoint> pts = strip.getPoints();
                int n = pts.size();
                // We need absolute positions — re-derive from the strip's own transform
                LXTransform ct = new LXTransform();
                ct.translate(tx, ty, tz);
                ct.rotateX(rotX);
                ct.rotateY(rotY);
                ct.rotateZ(rotZ);
                for (int i = 0; i < n; i++) {
                    float tParam = (n > 1) ? (float) i / (n - 1) : 0f;
                    float bezier = 4f * curve * tParam * (1f - tParam);
                    ct.push();
                    ct.translate(height * i, 0, bezier);
                    pts.get(i).update(ct.x(), ct.y(), ct.z());
                    ct.pop();
                }
            }
            strips.add(strip);
            transform.pop();
        }
    }
    static class MikeyPixlite extends SimplePixlite {
        public MikeyPixlite(LX lx, String ip, MikeyModel model) {
            super(lx, ip);
            // UNIVERSE_COUNT outputs; each output carries strips per universe
            // Per-strip GRB is handled via strip segments within the shared universe
            int[] counts = UIMikeyModelingTool.loadStripCountsFromDisk();
            int stripIndex = 0;
            for (int u = 0; u < UNIVERSE_COUNT; u++) {
                PointsGrouping pg = new PointsGrouping(String.valueOf(u + 1));
                int pixelOffset = 0;
                for (int s = 0; s < counts[u]; s++) {
                    if (stripIndex >= model.strips.size()) break;
                    Strip strip = model.getStripByIndex(stripIndex++);
                    int numPixels = strip.getPoints().size();
                    // Add strip segment with its GRB setting
                    pg.addStripSegment(pixelOffset, pixelOffset + numPixels, strip.metrics.grbSwap);
                    pg.addPoints(strip.getPoints());
                    pixelOffset += numPixels;
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
