package com.symmetrylabs.shows.defibrillator;

import com.symmetrylabs.shows.Show;
import com.symmetrylabs.shows.defibrillator.DefibrillatorFixtureLoader.StripSpec;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.output.KinetDatagram;
import heronarts.lx.output.LXDatagramOutput;
import heronarts.lx.output.LXOutputGroup;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.component.UIButton;
import heronarts.p3lx.ui.studio.UICollapsibleSection;

import java.io.File;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Show ported from the defibrillator_clean (Robot Heart) LX Studio project.
 *
 * Model and output mapping data are read directly from the original .lxf fixture
 * files (see {@code defibrillator_clean/lx_app/Fixtures/}) so that the strip
 * positions and KiNET host/port wiring stay in sync with the source-of-truth
 * fixture definitions instead of being hand-copied into Java.
 *
 * Only KiNET-driven fixtures (Color Kinetics sPDS-480 controllers) are ported here:
 * heart, brandeaux, and the "SA" angler-heart screen from shady. The shady 2D LED
 * screens (S1-S15) use Art-Net and are not yet included; they can be added using
 * SLStudio's existing {@code SimplePixlite}/{@code ArtNetDmxDatagram} machinery.
 */
public class DefibrillatorShow implements Show {
    public static final String SHOW_NAME = "defibrillator";

    /** Relative to the SLStudio working directory. */
    private static final String FIXTURES_ROOT = "defibrillator_clean/lx_app/Fixtures";
    private static final String[] INDEX_FIXTURES = { "heart", "brandeaux", "shady" };

    private static List<StripSpec> stripSpecsCache;

    /** Every KiNET port output that was wired up, exposed for the model-tab UI. */
    private final List<OutputEntry> outputEntries = new ArrayList<>();

    private static List<StripSpec> loadSpecs() {
        if (stripSpecsCache == null) {
            File fixturesRoot = new File(FIXTURES_ROOT);
            stripSpecsCache = DefibrillatorFixtureLoader.loadKinetStrips(fixturesRoot, INDEX_FIXTURES);
            System.out.println("DefibrillatorShow: loaded " + stripSpecsCache.size() + " KiNET strips from " + fixturesRoot);
        }
        return stripSpecsCache;
    }

    @Override
    public SLModel buildModel() {
        return DefibrillatorModel.create(loadSpecs());
    }

    @Override
    public void setupLx(LX lx) {
        DefibrillatorModel model = (DefibrillatorModel) lx.model;
        List<StripSpec> specs = loadSpecs();
        List<Strip> strips = model.getStrips();

        // Group strip point-indices by host, then by KiNET port, ordered by their
        // channel offset within that port's pixel data.
        Map<String, Map<Integer, List<PortSegment>>> byHostPort = new LinkedHashMap<>();
        for (int i = 0; i < specs.size(); i++) {
            StripSpec spec = specs.get(i);
            Strip strip = strips.get(i);
            int[] indices = new int[strip.getPoints().size()];
            for (int j = 0; j < indices.length; j++) {
                indices[j] = strip.getPoints().get(j).index;
            }
            byHostPort
                .computeIfAbsent(spec.host, h -> new LinkedHashMap<>())
                .computeIfAbsent(spec.kinetPort, p -> new ArrayList<>())
                .add(new PortSegment(spec.channelOffset, indices, spec.fixtureLabel, spec.friendlyTag));
        }

        int hostCount = 0;
        int portCount = 0;
        for (Map.Entry<String, Map<Integer, List<PortSegment>>> hostEntry : byHostPort.entrySet()) {
            String host = hostEntry.getKey();
            LXOutputGroup hostGroup = new LXOutputGroup(lx, "Kinet-" + host);
            for (Map.Entry<Integer, List<PortSegment>> portEntry : hostEntry.getValue().entrySet()) {
                int port = portEntry.getKey();
                List<PortSegment> segments = portEntry.getValue();
                segments.sort((a, b) -> Integer.compare(a.offset, b.offset));

                int total = 0;
                for (PortSegment seg : segments) {
                    total = Math.max(total, seg.offset + seg.indices.length);
                }
                int[] portIndices = new int[total];
                Arrays.fill(portIndices, -1);
                for (PortSegment seg : segments) {
                    System.arraycopy(seg.indices, 0, portIndices, seg.offset, seg.indices.length);
                }

                KinetDatagram datagram = new KinetDatagram(port, portIndices);
                try {
                    datagram.setAddress(host);
                } catch (UnknownHostException e) {
                    System.err.println("DefibrillatorShow: unknown host " + host + ", skipping port " + port);
                    continue;
                }
                try {
                    LXDatagramOutput portOutput = new LXDatagramOutput(lx);
                    portOutput.setLogConnections(false);
                    portOutput.addDatagram(datagram);
                    hostGroup.addChild(portOutput);
                    outputEntries.add(new OutputEntry(host, port, portOutput, describeSegments(segments)));
                    portCount++;
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
            lx.addOutput(hostGroup);
            hostCount++;
        }
        System.out.println("DefibrillatorShow: wired " + portCount + " KiNET ports across " + hostCount + " controllers");
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        new UIDefibrillatorOutputs(ui, 0, 0, ui.rightPane.model.getContentWidth())
            .addToContainer(ui.rightPane.model);
    }

    /** One KiNET port output, with the host/port it corresponds to (for display/lookup). */
    private static class OutputEntry {
        final String host;
        final int port;
        final LXDatagramOutput output;
        /** Fixture label(s)/friendly tag(s) that this port drives, e.g. "B28 (mouth1left)". */
        final String description;

        OutputEntry(String host, int port, LXDatagramOutput output, String description) {
            this.host = host;
            this.port = port;
            this.output = output;
            this.description = description;
        }
    }

    /**
     * Model-tab panel listing every KiNET port output with a click-to-toggle on/off.
     *
     * This is sized to fit all of its rows (like {@code UIMikeyModelingTool}) rather than
     * using its own small internal scroll region, so the whole thing scrolls via the shared
     * MODEL-tab scrollbar in {@code UIOverriddenRightPane} instead of a second nested one.
     */
    private class UIDefibrillatorOutputs extends UICollapsibleSection {
        private static final float ROW_H = 20f;
        private static final float ROW_GAP = 2f;
        private static final float ROWS_TOP = 24f;

        private final List<UIButton> rowButtons = new ArrayList<>();
        private final BooleanParameter enableAll =
            new BooleanParameter("enableAll").setMode(BooleanParameter.Mode.MOMENTARY);
        private final BooleanParameter disableAll =
            new BooleanParameter("disableAll").setMode(BooleanParameter.Mode.MOMENTARY);

        UIDefibrillatorOutputs(UI ui, float x, float y, float w) {
            super(ui, x, y, w, ROWS_TOP + 20);
            setTitle("KINET OUTPUTS (" + outputEntries.size() + ")");
            setPadding(5);

            new UIButton(0, 0, w / 2 - 10, 20)
                .setLabel("Enable All")
                .setParameter(enableAll)
                .addToContainer(this);
            new UIButton(w / 2, 0, w / 2 - 10, 20)
                .setLabel("Disable All")
                .setParameter(disableAll)
                .addToContainer(this);

            for (int i = 0; i < outputEntries.size(); i++) {
                OutputEntry entry = outputEntries.get(i);
                UIButton row = new UIButton(0, ROWS_TOP + i * (ROW_H + ROW_GAP), w - 10, ROW_H) {
                    @Override
                    protected void onToggle(boolean on) {
                        entry.output.enabled.setValue(on);
                    }
                };
                row.setLabel(entry.description + "  \u2014  " + entry.host + ":" + entry.port)
                    .setActive(entry.output.enabled.isOn())
                    .addToContainer(this);
                rowButtons.add(row);
            }

            getContentTarget().setHeight(ROWS_TOP + outputEntries.size() * (ROW_H + ROW_GAP));

            enableAll.addListener(p -> {
                if (enableAll.getValueb()) {
                    for (int i = 0; i < outputEntries.size(); i++) {
                        outputEntries.get(i).output.enabled.setValue(true);
                        rowButtons.get(i).setActive(true);
                    }
                }
            });
            disableAll.addListener(p -> {
                if (disableAll.getValueb()) {
                    for (int i = 0; i < outputEntries.size(); i++) {
                        outputEntries.get(i).output.enabled.setValue(false);
                        rowButtons.get(i).setActive(false);
                    }
                }
            });
        }
    }

    /** Builds a human-readable name for a port from the fixture labels/tags of its segments. */
    private static String describeSegments(List<PortSegment> segments) {
        java.util.LinkedHashSet<String> names = new java.util.LinkedHashSet<>();
        for (PortSegment seg : segments) {
            names.add(seg.friendlyTag != null ? seg.fixtureLabel + " (" + seg.friendlyTag + ")" : seg.fixtureLabel);
        }
        return String.join(" + ", names);
    }

    private static class PortSegment {
        final int offset;
        final int[] indices;
        final String fixtureLabel;
        final String friendlyTag;

        PortSegment(int offset, int[] indices, String fixtureLabel, String friendlyTag) {
            this.offset = offset;
            this.indices = indices;
            this.fixtureLabel = fixtureLabel;
            this.friendlyTag = friendlyTag;
        }
    }

    static class DefibrillatorModel extends StripsModel<Strip> {
        DefibrillatorModel(List<Strip> strips) {
            super(SHOW_NAME, strips);
        }

        static DefibrillatorModel create(List<StripSpec> specs) {
            List<Strip> strips = new ArrayList<>();
            for (StripSpec spec : specs) {
                strips.add(buildStrip(spec));
            }
            System.out.println("DefibrillatorModel: built " + strips.size() + " strips");
            return new DefibrillatorModel(strips);
        }

        private static Strip buildStrip(StripSpec spec) {
            float len = (float) Math.sqrt(
                spec.dirX * spec.dirX + spec.dirY * spec.dirY + spec.dirZ * spec.dirZ);
            float ux = len > 0 ? spec.dirX / len : 1;
            float uy = len > 0 ? spec.dirY / len : 0;
            float uz = len > 0 ? spec.dirZ / len : 0;

            List<LXPoint> points = new ArrayList<>(spec.numPoints);
            for (int i = 0; i < spec.numPoints; i++) {
                float px = spec.x + ux * spec.spacing * i;
                float py = spec.y + uy * spec.spacing * i;
                float pz = spec.z + uz * spec.spacing * i;
                points.add(new LXPoint(px, py, pz));
            }
            Strip.Metrics metrics = new Strip.Metrics(spec.numPoints, spec.spacing);
            return new Strip(spec.id, metrics, points);
        }
    }
}
