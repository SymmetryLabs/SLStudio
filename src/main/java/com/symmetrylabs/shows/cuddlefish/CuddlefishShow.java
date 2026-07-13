package com.symmetrylabs.shows.cuddlefish;

import com.google.common.collect.Lists;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.cuddlefish.ui.UICuddlefishModelingTool;
import static com.symmetrylabs.shows.cuddlefish.ui.UICuddlefishModelingTool.UNIVERSE_COUNT;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.ArtNetDmxDatagram;
import com.symmetrylabs.slstudio.output.ArtNetOutput;
import com.symmetrylabs.slstudio.output.SimplePixlite;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.model.DoubleStrip;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.LXDatagram;
import heronarts.lx.transform.LXMatrix;
import heronarts.lx.transform.LXTransform;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CuddlefishShow implements Show {
    public static final String SHOW_NAME = "cuddlefish";

    @Override
    public SLModel buildModel() {
        return CuddlefishModel.create();
    }

    public static StripIlluminator illuminator;
    public static LXChannel illumChannel;

    @Override
    public void setupLx(LX lx) {
        CuddlefishModel model = (CuddlefishModel) lx.model;
        CuddlefishPixlite pixlite = new CuddlefishPixlite(lx, "192.168.1.50", model);
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
        UICuddlefishModelingTool tool = new UICuddlefishModelingTool(ui, 0, 0, ui.rightPane.model.getContentWidth());
        tool.addToContainer(ui.rightPane.model);
        // GROUPS tab: assign strips to 8 groups used by the GroupStripFilter effect
        com.symmetrylabs.shows.cuddlefish.ui.UICuddlefishStripGroupTool groupTool =
            new com.symmetrylabs.shows.cuddlefish.ui.UICuddlefishStripGroupTool(ui, 0, 0, ui.rightPane.groups.getContentWidth());
        groupTool.addToContainer(ui.rightPane.groups);
        // Wire live strip list so dragging params moves pixels in the 3D view
        CuddlefishModel model = (CuddlefishModel) lx.model;
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
        System.out.println("CuddlefishShow: model bounds x=" + model.xMin + ".." + model.xMax +
            " y=" + model.yMin + ".." + model.yMax + " center=(" + cx + "," + cy + "," + cz + ")" +
            " xRange=" + model.xRange + " viewRadius=" + viewRadius);
        ui.preview.setRadiusBounds(1, Float.MAX_VALUE);
        ui.preview.setCenter(cx, cy, cz);
        ui.preview.setRadius(viewRadius);
        // Increase depth field so near clip never eats points during panning
        ui.preview.depth.setValue(2);
        System.out.println("CuddlefishShow: camera set center=(" + cx + "," + cy + "," + cz + ") radius=" + viewRadius);
    }

    static class CuddlefishModel extends StripsModel<Strip> {
        public CuddlefishModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static CuddlefishModel create() {
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();

            int[] counts = UICuddlefishModelingTool.loadStripCountsFromDisk();
            float[][] mapping = UICuddlefishModelingTool.loadStripsFromDisk();
            int totalStrips = 0;
            for (int c : counts) totalStrips += c;
            if (mapping == null) {
                System.out.println("CuddlefishShow: no mapping on disk, using default (" + totalStrips + " strips)");
                mapping = buildDefaultMapping(totalStrips);
            } else if (mapping.length != totalStrips) {
                System.out.println("CuddlefishShow: mapping has " + mapping.length + " strips but counts sum to " + totalStrips + " — adjusting counts to match mapping");
                // Recompute counts to match the actual strip data: keep per-universe distribution
                // but clamp/trim so the total equals mapping.length
                int loaded = mapping.length;
                int assigned = 0;
                for (int u = 0; u < counts.length; u++) {
                    int give = Math.min(counts[u], loaded - assigned);
                    counts[u] = give;
                    assigned += give;
                    if (assigned >= loaded) {
                        for (int r = u + 1; r < counts.length; r++) counts[r] = 0;
                        break;
                    }
                }
            }
            System.out.println("CuddlefishShow: building " + mapping.length + " strips from mapping");
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
            System.out.println("CuddlefishShow: created model with " + strips.size() + " strips");
            return new CuddlefishModel(strips);
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
    /**
     * Single-socket ArtNet output for all 96 cuddlefish universes.
     * All datagrams share one DatagramSocket and one LXDatagramOutput, eliminating
     * the per-universe socket overhead that caused glitching above ~51 universes.
     * Per-universe mute is handled via a boolean[] that skips individual datagrams.
     */
    public static class CuddlefishPixlite extends ArtNetOutput {
        /** Datagrams indexed by universe (0-based) for per-universe mute. */
        public static final List<ArtNetDmxDatagram> universeDatagram = new ArrayList<>();

        /**
         * Authoritative universe (0-based) -> points-in-output-order mapping.
         * Built once here and shared with UniverseSelector so the mapping tools
         * always match what is actually sent on the wire.
         */
        public static final List<List<heronarts.lx.model.LXPoint>> universePoints = new ArrayList<>();

        /**
         * Authoritative (universe, strip-within-universe) -> model strip index table,
         * built from the same counts/order the datagrams use. Lets the UI illuminate
         * the exact strip that is sent on a given universe.
         */
        public static final List<int[]> universeStripModelIndices = new ArrayList<>();

        /** Returns the model strip index for strip s (0-based) of universe u (0-based), or -1. */
        public static int getModelStripIndex(int u, int s) {
            if (u < 0 || u >= universeStripModelIndices.size()) return -1;
            int[] arr = universeStripModelIndices.get(u);
            return (s >= 0 && s < arr.length) ? arr[s] : -1;
        }

        public CuddlefishPixlite(LX lx, String ip, CuddlefishModel model) {
            super(lx, ip);
            universeDatagram.clear();
            universePoints.clear();
            universeStripModelIndices.clear();

            int[] counts = UICuddlefishModelingTool.loadStripCountsFromDisk();

            try {
                // One shared socket for all universes
                DatagramSocket sharedSocket = new DatagramSocket();
                sharedSocket.setBroadcast(false);
                SingleSocketOutput singleOut = new SingleSocketOutput(lx, sharedSocket);
                singleOut.setLogConnections(false);

                int stripIndex = 0;
                Set<Integer> seenUniverses = new HashSet<>();
                Map<Integer, Integer> pointIndexToUniverse = new HashMap<>();
                for (int u = 0; u < UNIVERSE_COUNT; u++) {
                    PointsGrouping pg = new PointsGrouping(String.valueOf(u + 1));
                    int pixelOffset = 0;
                    int[] stripIdxs = new int[counts[u]];
                    java.util.Arrays.fill(stripIdxs, -1);
                    for (int s = 0; s < counts[u]; s++) {
                        if (stripIndex >= model.strips.size()) break;
                        stripIdxs[s] = stripIndex;
                        Strip strip = model.getStripByIndex(stripIndex++);
                        int numPixels = strip.getPoints().size();
                        pg.addStripSegment(pixelOffset, pixelOffset + numPixels, !strip.metrics.grbSwap);
                        pg.addPoints(strip.getPoints());
                        pixelOffset += numPixels;
                    }
                    universeStripModelIndices.add(stripIdxs);

                    universePoints.add(new ArrayList<>(pg.getPoints()));

                    int[] indices = pg.getIndices();
                    if (indices.length == 0) {
                        universeDatagram.add(null);
                        continue;
                    }

                    // Guard against duplicate or oversized universes, and point index overlap.
                    if (!seenUniverses.add(u)) {
                        System.err.println("CuddlefishPixlite: duplicate ArtNet universe " + u + " detected for output U" + (u + 1));
                    }
                    if (indices.length > 170) {
                        System.err.println("CuddlefishPixlite: universe U" + (u + 1) + " has " + indices.length + " pixels, exceeding 170 pixel DMX limit");
                    }
                    for (int idx : indices) {
                        if (idx < 0) continue;
                        Integer otherU = pointIndexToUniverse.put(idx, u);
                        if (otherU != null) {
                            System.err.println("CuddlefishPixlite: point index " + idx + " assigned to both U" + (otherU + 1) + " and U" + (u + 1));
                        }
                    }
                    // Build per-pixel GRB flag array
                    boolean[] grbFlags = new boolean[indices.length];
                    for (PointsGrouping.StripSegment seg : pg.getStripSegments()) {
                        for (int i = seg.startIndex; i < seg.endIndex && i < grbFlags.length; i++) {
                            grbFlags[i] = seg.grbSwap;
                        }
                    }
                    // Send a full 512-channel universe (unused channels zero). The issue is
                    // not 510-vs-512 universe size: U47+ placeholders were sending TINY
                    // truncated frames (4 channels for a 1-pixel universe), which receivers
                    // handle inconsistently. Full-size frames match what MadMapper sends.
                    ArtNetDmxDatagram dgram = new ArtNetDmxDatagram(lx, ip, indices, 512, u+1);
                    dgram.setGrbFlags(grbFlags);
                    singleOut.addDatagram(dgram);
                    universeDatagram.add(dgram);
                }
                addChild(singleOut);
            } catch (SocketException e) {
                throw new RuntimeException("Failed to create cuddlefish output socket", e);
            }
        }

        /** Called by UI mute buttons. active=true means sending, false means muted. */
        public static void setUniverseMuted(int universe, boolean muted) {
            if (universe >= 0 && universe < universeDatagram.size()) {
                ArtNetDmxDatagram dgram = universeDatagram.get(universe);
                if (dgram != null) dgram.enabled.setValue(!muted);
            }
        }

        /**
         * Single-socket LXDatagramOutput.
         * Uses the shared socket from LXDatagramOutput and skips datagrams
         * whose enabled flag is false (set by setUniverseMuted).
         */
        private static class SingleSocketOutput extends LXDatagramOutput {
            /** ArtSync after each frame batch; off by default for Falcon receivers. */
            private static final boolean SEND_ARTSYNC = false;

            /**
             * Gap between consecutive packet sends. Without pacing all 96 universes
             * leave in a single burst and the receiver drops later packets, which
             * shows up as flicker on higher universe numbers.
             */
            private static final long PACKET_GAP_NANOS = 50_000; // 50us -> ~4.8ms per 96-universe frame

            SingleSocketOutput(LX lx, DatagramSocket socket) throws SocketException {
                super(lx, socket);
            }

            private static void pace(long nanos) {
                long start = System.nanoTime();
                while (System.nanoTime() - start < nanos) { /* spin */ }
            }

            @Override
            protected void onSend(heronarts.lx.PolyBuffer src) {
                List<heronarts.lx.output.LXDatagram> dgrams = getDatagrams();
                for (heronarts.lx.output.LXDatagram dgram : dgrams) {
                    if (!dgram.enabled.isOn()) continue;
                    dgram.onSend(src);
                    try {
                        socket.send(dgram.packet);
                    } catch (java.io.IOException iox) {
                        // silently skip
                    }
                    pace(PACKET_GAP_NANOS);
                }
                // One Art-Net sync packet for the whole batch.
                // Disabled by default: Falcon controllers latch stale data on dropped
                // packets when in ArtSync synchronous mode. Only enable for receivers
                // known to handle ArtSync correctly.
                if (SEND_ARTSYNC && !dgrams.isEmpty()) {
                    try {
                        // ArtSync is 14 bytes: 12-byte header + Aux1/Aux2 (must be 0)
                        byte[] syncBuf = new byte[com.symmetrylabs.slstudio.output.ArtNetDatagramUtil.HEADER_LENGTH + 2];
                        com.symmetrylabs.slstudio.output.ArtNetDatagramUtil.fillHeader(syncBuf, (short) 0x5200);
                        java.net.InetAddress addr = dgrams.get(0).getAddress();
                        if (addr != null) {
                            socket.send(new java.net.DatagramPacket(syncBuf, syncBuf.length, addr,
                                com.symmetrylabs.slstudio.output.ArtNetDatagramUtil.ARTNET_PORT));
                        }
                    } catch (java.io.IOException iox) {
                        // silently skip
                    }
                }
            }
        }
    }
}
