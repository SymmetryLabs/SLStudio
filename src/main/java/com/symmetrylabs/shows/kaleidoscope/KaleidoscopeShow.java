package com.symmetrylabs.shows.kaleidoscope;

import com.google.common.collect.Lists;
import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.kaleidoscope.ui.UIKaleidoscopeModelingTool;
import static com.symmetrylabs.shows.kaleidoscope.ui.UIKaleidoscopeModelingTool.CHANNEL_COUNT;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.CandyBar;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.output.ArtNetDmxDatagram;
import com.symmetrylabs.slstudio.output.ArtNetOutput;
import com.symmetrylabs.slstudio.output.SimplePixlite;
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
import java.util.List;
import java.util.Map;

public class KaleidoscopeShow implements Show {
    public static final String SHOW_NAME = "kaleidoscope";

    /** WLED controllers. Channels (controller outputs) are assigned to controllers in order: the first CHANNELS_PER_CONTROLLER[0] channels to CONTROLLER_IPS[0], the next CHANNELS_PER_CONTROLLER[1] to CONTROLLER_IPS[1]. */
    public static final String[] CONTROLLER_IPS = { "10.200.1.2", "10.200.1.3" };
    public static final int[] CHANNELS_PER_CONTROLLER = { 4, 8 };
    /** WLED packs its whole LED buffer into consecutive ArtNet universes of 170 pixels, starting here. */
    public static final int ARTNET_START_UNIVERSE = 0;
    public static final int PIXELS_PER_UNIVERSE = 170;

    /** Controller index (0-based) that owns channel ch (0-based). */
    public static int controllerForChannel(int ch) {
        int acc = 0;
        for (int c = 0; c < CHANNELS_PER_CONTROLLER.length; c++) {
            acc += CHANNELS_PER_CONTROLLER[c];
            if (ch < acc) return c;
        }
        return CHANNELS_PER_CONTROLLER.length - 1;
    }

    /** First channel (0-based) of controller c. */
    public static int firstChannelOfController(int c) {
        int acc = 0;
        for (int i = 0; i < c && i < CHANNELS_PER_CONTROLLER.length; i++) acc += CHANNELS_PER_CONTROLLER[i];
        return acc;
    }

    @Override
    public SLModel buildModel() {
        return KaleidoscopeModel.create();
    }

    public static StripIlluminator illuminator;
    public static LXChannel illumChannel;

    @Override
    public void setupLx(LX lx) {
        KaleidoscopeModel model = (KaleidoscopeModel) lx.model;
        KaleidoscopePixlite pixlite = new KaleidoscopePixlite(lx, model);
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
        UIKaleidoscopeModelingTool tool = new UIKaleidoscopeModelingTool(ui, 0, 0, ui.rightPane.model.getContentWidth());
        tool.addToContainer(ui.rightPane.model);
        // GROUPS tab: assign strips to 12 groups used by the GroupStripFilter effect
        com.symmetrylabs.shows.kaleidoscope.ui.UIKaleidoscopeStripGroupTool groupTool =
            new com.symmetrylabs.shows.kaleidoscope.ui.UIKaleidoscopeStripGroupTool(ui, 0, 0, ui.rightPane.groups.getContentWidth());
        groupTool.addToContainer(ui.rightPane.groups);
        // Wire live strip list so dragging params moves pixels in the 3D view
        KaleidoscopeModel model = (KaleidoscopeModel) lx.model;
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
        System.out.println("KaleidoscopeShow: model bounds x=" + model.xMin + ".." + model.xMax +
            " y=" + model.yMin + ".." + model.yMax + " center=(" + cx + "," + cy + "," + cz + ")" +
            " xRange=" + model.xRange + " viewRadius=" + viewRadius);
        ui.preview.setRadiusBounds(1, Float.MAX_VALUE);
        ui.preview.setCenter(cx, cy, cz);
        ui.preview.setRadius(viewRadius);
        // Increase depth field so near clip never eats points during panning
        ui.preview.depth.setValue(2);
        System.out.println("KaleidoscopeShow: camera set center=(" + cx + "," + cy + "," + cz + ") radius=" + viewRadius);
    }

    static class KaleidoscopeModel extends StripsModel<Strip> {
        public KaleidoscopeModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        public static KaleidoscopeModel create() {
            List<Strip> strips = new ArrayList<Strip>();
            LXTransform t = new LXTransform();

            int[] counts = UIKaleidoscopeModelingTool.loadStripCountsFromDisk();
            float[][] mapping = UIKaleidoscopeModelingTool.loadStripsFromDisk();
            int totalStrips = 0;
            for (int c : counts) totalStrips += c;
            if (mapping == null) {
                System.out.println("KaleidoscopeShow: no mapping on disk, using default (" + totalStrips + " strips)");
                mapping = buildDefaultMapping(totalStrips);
            } else if (mapping.length != totalStrips) {
                System.out.println("KaleidoscopeShow: mapping has " + mapping.length + " strips but counts sum to " + totalStrips + " — adjusting counts to match mapping");
                // Recompute counts to match the actual strip data: keep per-channel distribution
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
            System.out.println("KaleidoscopeShow: building " + mapping.length + " strips from mapping");
            for (int i = 0; i < mapping.length; i++) {
                float[] m = mapping[i];
                float az, rx, ry, d, cv, diameter;
                int px;
                boolean grbSwap, circle;
                if (m.length >= 12) {
                    az = m[3]; rx = m[4]; ry = m[5]; px = (int) m[6]; d = m[7]; cv = m[8];
                    grbSwap = m[9] > 0.5f;
                    circle = m[10] > 0.5f;
                    diameter = m[11];
                } else if (m.length >= 10) {
                    // New 10-col format: [tx,ty,tz,az,rx,ry,px,d,cv,grb]
                    az = m[3]; rx = m[4]; ry = m[5]; px = (int) m[6]; d = m[7]; cv = m[8];
                    grbSwap = m[9] > 0.5f;
                    circle = false;
                    diameter = 24f;
                } else if (m.length == 9) {
                    // 9-col format: [tx,ty,tz,az,rx,ry,px,d,cv] — no grb
                    az = m[3]; rx = m[4]; ry = m[5]; px = (int) m[6]; d = m[7]; cv = m[8];
                    grbSwap = false;
                    circle = false;
                    diameter = 24f;
                } else if (m.length == 8) {
                    // 8-col format: [tx,ty,tz,az,rx,ry,px,d] — no cv/grb
                    az = m[3]; rx = m[4]; ry = m[5]; px = (int) m[6]; d = m[7]; cv = 0f;
                    grbSwap = false;
                    circle = false;
                    diameter = 24f;
                } else {
                    // Old 6-col format: [tx,ty,tz,az,px,d] — no rx/ry/cv/grb
                    az = m[3]; rx = 0f; ry = 0f; px = (int) m[4]; d = m[5]; cv = 0f;
                    grbSwap = false;
                    circle = false;
                    diameter = 24f;
                }
                az = az > 180f ? az - 360f : az;
                rx = rx > 180f ? rx - 360f : rx;
                ry = ry > 180f ? ry - 360f : ry;
                float rotZRad = (float) Math.toRadians(-az);
                float rotXRad = (float) Math.toRadians(-rx);
                float rotYRad = (float) Math.toRadians(-ry);
                addStrip(m[0], m[1], m[2], rotXRad, rotYRad, rotZRad, px, d, cv, grbSwap, circle, diameter, t, strips);
            }
            System.out.println("KaleidoscopeShow: created model with " + strips.size() + " strips");
            return new KaleidoscopeModel(strips);
        }

        private static float[][] buildDefaultMapping(int totalStrips) {
            float[][] d = new float[totalStrips][12];
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
                d[i][10] = 0f;
                d[i][11] = 24f;
            }
            return d;
        }

        private static void addStrip(float tx, float ty, float tz, float rotX, float rotY, float rotZ, int pixelCount, float height, float curve, boolean grbSwap, boolean circle, float diameter, LXTransform transform, List<Strip> strips) {
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
            if (curve != 0f || circle) {
                List<heronarts.lx.model.LXPoint> pts = strip.getPoints();
                int n = pts.size();
                // We need absolute positions — re-derive from the strip's own transform
                LXTransform ct = new LXTransform();
                ct.translate(tx, ty, tz);
                ct.rotateX(rotX);
                ct.rotateY(rotY);
                ct.rotateZ(rotZ);
                float radius = diameter / 2f;
                for (int i = 0; i < n; i++) {
                    float tParam = (n > 1) ? (float) i / (n - 1) : 0f;
                    float bezier = 4f * curve * tParam * (1f - tParam);
                    ct.push();
                    if (circle) {
                        float angle = (n > 0) ? (float) (2 * Math.PI * i / n) : 0f;
                        ct.translate(radius * (float) Math.cos(angle), radius * (float) Math.sin(angle), 0);
                    } else {
                        ct.translate(height * i, 0, bezier);
                    }
                    pts.get(i).update(ct.x(), ct.y(), ct.z());
                    ct.pop();
                }
            }
            strips.add(strip);
            transform.pop();
        }
    }
    /**
     * Single-socket ArtNet output for the WLED controllers.
     * Each WLED box has one flat LED buffer: its channel outputs are contiguous
     * slices of that buffer, and ArtNet universes are the flat buffer chopped every
     * PIXELS_PER_UNIVERSE pixels starting at ARTNET_START_UNIVERSE — universe
     * boundaries do not align to channels or strips. All datagrams share one
     * DatagramSocket and one LXDatagramOutput. Per-channel mute is handled by
     * rewriting each datagram's index array (index -1 sends black).
     */
    public static class KaleidoscopePixlite extends ArtNetOutput {
        /** Datagrams in send order: controller 0's universes, then controller 1's. */
        public static final List<ArtNetDmxDatagram> universeDatagram = new ArrayList<>();

        /**
         * Points of each datagram in output order (parallel to universeDatagram).
         * Shared with UniverseSelectorKaleidoscope so the mapping tools always match
         * what is actually sent on the wire.
         */
        public static final List<List<heronarts.lx.model.LXPoint>> universePoints = new ArrayList<>();

        /** Human-readable label per datagram, e.g. "10.200.1.2 U0" (parallel to universeDatagram). */
        public static final List<String> universeLabels = new ArrayList<>();

        /**
         * Per channel (controller output), the model strip indices routed to it,
         * built from the same counts/order the datagrams use. Lets the UI illuminate
         * the exact strip that is sent on a given channel.
         */
        public static final List<int[]> channelStripModelIndices = new ArrayList<>();

        /** Per datagram: original point indices (before channel-mute rewriting). */
        private static final List<int[]> datagramOrigIndices = new ArrayList<>();
        /** Per datagram: channel id of each pixel slot (parallel to datagramOrigIndices). */
        private static final List<int[]> datagramPixelChannel = new ArrayList<>();

        /** Per-channel mute flags. */
        private static final boolean[] channelMuted = new boolean[CHANNEL_COUNT];

        /** Returns the model strip index for strip s (0-based) of channel ch (0-based), or -1. */
        public static int getModelStripIndex(int ch, int s) {
            if (ch < 0 || ch >= channelStripModelIndices.size()) return -1;
            int[] arr = channelStripModelIndices.get(ch);
            return (s >= 0 && s < arr.length) ? arr[s] : -1;
        }

        /** One pixel slot in a controller's flat LED buffer. */
        private static class FlatPixel {
            final heronarts.lx.model.LXPoint point;
            final boolean grb;
            final int channel;
            FlatPixel(heronarts.lx.model.LXPoint point, boolean grb, int channel) {
                this.point = point;
                this.grb = grb;
                this.channel = channel;
            }
        }

        /**
         * Flattens the strips of every controller's channels into one buffer per
         * controller (channel order, strip order within channel, pixel order within
         * strip) and chops each into PIXELS_PER_UNIVERSE chunks. Shared by the
         * datagram constructor and computeUniversePoints so the chunk boundaries
         * cannot diverge. Returns, per controller, the list of chunks.
         * If channelStripIndices is non-null it is filled with the per-channel
         * model strip index tables.
         */
        private static List<List<List<FlatPixel>>> buildControllerChunks(List<Strip> strips, int[] counts, List<int[]> channelStripIndices) {
            List<List<List<FlatPixel>>> out = new ArrayList<>();
            int stripIndex = 0;
            int ch = 0;
            for (int c = 0; c < CONTROLLER_IPS.length; c++) {
                List<FlatPixel> flat = new ArrayList<>();
                for (int k = 0; k < CHANNELS_PER_CONTROLLER[c]; k++, ch++) {
                    int count = (ch < counts.length) ? counts[ch] : 0;
                    int[] stripIdxs = new int[count];
                    java.util.Arrays.fill(stripIdxs, -1);
                    for (int s = 0; s < count; s++) {
                        if (stripIndex >= strips.size()) break;
                        stripIdxs[s] = stripIndex;
                        Strip strip = strips.get(stripIndex++);
                        boolean grbFlag = !strip.metrics.grbSwap;
                        for (heronarts.lx.model.LXPoint p : strip.getPoints()) {
                            flat.add(new FlatPixel(p, grbFlag, ch));
                        }
                    }
                    if (channelStripIndices != null) channelStripIndices.add(stripIdxs);
                }
                List<List<FlatPixel>> chunks = new ArrayList<>();
                for (int start = 0; start < flat.size(); start += PIXELS_PER_UNIVERSE) {
                    chunks.add(new ArrayList<>(flat.subList(start, Math.min(start + PIXELS_PER_UNIVERSE, flat.size()))));
                }
                out.add(chunks);
            }
            return out;
        }

        /**
         * Points of every ArtNet universe in send order (controller 0's universes
         * U0,U1,…, then controller 1's U0,U1,…), computed with the same
         * flat-per-controller 170-pixel chunking the datagrams use.
         */
        public static List<List<heronarts.lx.model.LXPoint>> computeUniversePoints(List<Strip> strips, int[] counts) {
            List<List<heronarts.lx.model.LXPoint>> out = new ArrayList<>();
            for (List<List<FlatPixel>> chunks : buildControllerChunks(strips, counts, null)) {
                for (List<FlatPixel> chunk : chunks) {
                    List<heronarts.lx.model.LXPoint> pts = new ArrayList<>(chunk.size());
                    for (FlatPixel fp : chunk) pts.add(fp.point);
                    out.add(pts);
                }
            }
            return out;
        }

        public KaleidoscopePixlite(LX lx, KaleidoscopeModel model) {
            super(lx, CONTROLLER_IPS[0]);
            universeDatagram.clear();
            universePoints.clear();
            universeLabels.clear();
            channelStripModelIndices.clear();
            datagramOrigIndices.clear();
            datagramPixelChannel.clear();
            java.util.Arrays.fill(channelMuted, false);

            int[] counts = UIKaleidoscopeModelingTool.loadStripCountsFromDisk();

            try {
                // One shared socket for all datagrams
                DatagramSocket sharedSocket = new DatagramSocket();
                sharedSocket.setBroadcast(false);
                SingleSocketOutput singleOut = new SingleSocketOutput(lx, sharedSocket);
                singleOut.setLogConnections(false);

                List<List<List<FlatPixel>>> chunksPerController =
                    buildControllerChunks(model.getStrips(), counts, channelStripModelIndices);
                Map<Integer, Integer> pointIndexToDatagram = new HashMap<>();

                for (int c = 0; c < chunksPerController.size(); c++) {
                    List<List<FlatPixel>> chunks = chunksPerController.get(c);
                    int totalPixels = 0;
                    for (List<FlatPixel> chunk : chunks) totalPixels += chunk.size();
                    for (int k = 0; k < chunks.size(); k++) {
                        List<FlatPixel> chunk = chunks.get(k);
                        int[] indices = new int[chunk.size()];
                        boolean[] grbFlags = new boolean[chunk.size()];
                        int[] chan = new int[chunk.size()];
                        List<heronarts.lx.model.LXPoint> pts = new ArrayList<>(chunk.size());
                        String label = CONTROLLER_IPS[c] + " U" + (ARTNET_START_UNIVERSE + k);
                        for (int i = 0; i < chunk.size(); i++) {
                            FlatPixel fp = chunk.get(i);
                            indices[i] = fp.point.index;
                            grbFlags[i] = fp.grb;
                            chan[i] = fp.channel;
                            pts.add(fp.point);
                            if (fp.point.index >= 0) {
                                Integer otherD = pointIndexToDatagram.put(fp.point.index, universeDatagram.size());
                                if (otherD != null) {
                                    System.err.println("KaleidoscopePixlite: point index " + fp.point.index +
                                        " assigned to both " + universeLabels.get(otherD) + " and " + label);
                                }
                            }
                        }
                        // Send a full 512-channel universe (unused channels zero).
                        ArtNetDmxDatagram dgram = new ArtNetDmxDatagram(lx, CONTROLLER_IPS[c], indices, 512, ARTNET_START_UNIVERSE + k);
                        dgram.setGrbFlags(grbFlags);
                        singleOut.addDatagram(dgram);
                        universeDatagram.add(dgram);
                        universePoints.add(pts);
                        universeLabels.add(label);
                        datagramOrigIndices.add(indices);
                        datagramPixelChannel.add(chan);
                    }
                    System.out.println("KaleidoscopePixlite: " + CONTROLLER_IPS[c] + " flat buffer " +
                        totalPixels + " pixels -> " + chunks.size() + " ArtNet universe(s)");
                }
                addChild(singleOut);
            } catch (SocketException e) {
                throw new RuntimeException("Failed to create kaleidoscope output socket", e);
            }
        }

        /**
         * Called by UI mute buttons. Muted channels contribute black pixels: every
         * datagram containing that channel's pixels gets its index array rewritten
         * (index -1 sends black via unmappedPointColor), because a universe can
         * contain pixels from two different channels.
         */
        public static void setChannelMuted(int ch, boolean muted) {
            if (ch < 0 || ch >= channelMuted.length) return;
            channelMuted[ch] = muted;
            for (int d = 0; d < universeDatagram.size(); d++) {
                ArtNetDmxDatagram dgram = universeDatagram.get(d);
                if (dgram == null) continue;
                int[] orig = datagramOrigIndices.get(d);
                int[] chan = datagramPixelChannel.get(d);
                boolean touches = false;
                for (int cc : chan) {
                    if (cc == ch) { touches = true; break; }
                }
                if (!touches) continue;
                int[] idx = new int[orig.length];
                for (int i = 0; i < orig.length; i++) {
                    idx[i] = channelMuted[chan[i]] ? -1 : orig[i];
                }
                dgram.setIndices(idx);
            }
        }

        /**
         * Single-socket LXDatagramOutput.
         * Uses the shared socket from LXDatagramOutput and skips datagrams
         * whose enabled flag is false.
         */
        private static class SingleSocketOutput extends LXDatagramOutput {
            /** ArtSync after each frame batch; off by default for Falcon receivers. */
            private static final boolean SEND_ARTSYNC = false;

            /**
             * Gap between consecutive packet sends so datagrams do not leave in a
             * single burst and overwhelm the receivers.
             */
            private static final long PACKET_GAP_NANOS = 50_000; // 50us between packets

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
