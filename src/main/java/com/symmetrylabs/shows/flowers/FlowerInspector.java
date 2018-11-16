package com.symmetrylabs.shows.flowers;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import com.symmetrylabs.shows.flowers.FlowerRecord.Harness;
import com.symmetrylabs.shows.flowers.FlowerModel.FlowerPoint;

public class FlowerInspector extends FlowerPattern implements UIFlowerTool.Listener {
    public static final String GROUP_NAME = FlowersShow.SHOW_NAME;

    public static enum Mode {
        ALL,
        PANEL,
        PIXLITE,
        HARNESS,
        HARNESS_INDEX,
    };

    private final EnumParameter<Mode> mode = new EnumParameter<Mode>("mode", Mode.ALL);
    private final DiscreteParameter panel = new DiscreteParameter("panel", new String[]{"ALL"});
    private final DiscreteParameter pixlite = new DiscreteParameter("pixlite", new String[]{"ALL"});
    private final DiscreteParameter flowerParam;
    private static final String OPTION_ALL = "ALL";

    private static final int[] PANEL_COLORS = new int[] {
        0xFFFF0000,
        0xFFFFFF00,
        0xFF00FF00,
        0xFF00FFFF,
        0xFF0000FF,
        0xFFFF00FF,
    };

    private static final int[] HARNESS_COLORS = new int[] {
        0xFFFF0000,
        0xFFFF9900,
        0xFFFFFF00,
        0xFF00FF00,
        0xFF00FF99,
        0xFF00FFFF,
        0xFF0000FF,
        0xFF8800FF,
        0xFFFF00FF,
    };

    public FlowerInspector(LX lx) {
        super(lx);
        flowerParam = new DiscreteParameter("flower", 0, 0, model.getFlowers().size());
        addParameter(flowerParam);
        addParameter(mode);
        addParameter(panel);
        addParameter(pixlite);
    }

    private FlowerModel get() {
        return model.getFlowers().get(flowerParam.getValuei());
    }

    @Override
    public void run(double elapsedMs) {
        Arrays.fill(colors, 0);
        Mode m = mode.getEnum();
        String activePanel = panel.getOption();
        if (activePanel.equals(OPTION_ALL)) {
            activePanel = null;
        }
        int activePixlite = pixlite.getOption().equals(OPTION_ALL) ?
            FlowerRecord.UNKNOWN_PIXLITE_ID : Integer.parseInt(pixlite.getOption());

        for (FlowerModel fm : model.getFlowers()) {
            FlowerData fd = fm.getFlowerData();

            int panelColor = fd.record.panelId == null ?
                0xFF000000 : PANEL_COLORS[fd.record.panelId.hashCode() % PANEL_COLORS.length];
            int pixliteColor = HARNESS_COLORS[fd.record.pixliteId % HARNESS_COLORS.length];
            int harnessColor =
                fd.record.harness == Harness.A ? 0xFFFF0000 :
                fd.record.harness == Harness.B ? 0xFF00FF00 :
                0xFF000000;
            int harnessIndexColor = fd.record.harnessIndex < 0 ?
                0xFF000000 : HARNESS_COLORS[fd.record.harnessIndex];

            boolean matches =
                activePanel == null
                || (fd.record.panelId != null && activePanel.equals(fd.record.panelId));
            matches = matches && (
                activePixlite == FlowerRecord.UNKNOWN_PIXLITE_ID || activePixlite == fd.record.pixliteId);

            if (matches) {
                for (FlowerPoint p : fm.getFlowerPoints()) {
                    switch (p.direction) {
                    case A:
                        if (m == Mode.ALL || m == Mode.PANEL) colors[p.index] = panelColor;
                        break;
                    case B:
                        if (m == Mode.ALL || m == Mode.HARNESS) colors[p.index] = harnessColor;
                        break;
                    case C:
                        if (m == Mode.ALL || m == Mode.HARNESS_INDEX) colors[p.index] = harnessIndexColor;
                        break;
                    case UP:
                        if (m == Mode.ALL || m == Mode.PIXLITE) colors[p.index] = pixliteColor;
                    }
                }
            }
        }
        for (LXPoint p : get().points) {
            colors[p.index] = 0xFFFFFFFF;
        }
    }

    @Override
    public String getCaption() {
        return get().getFlowerData().toString();
    }

    @Override
    public void onActive() {
        super.onActive();
        UIFlowerTool.get().addListener(this);
        onUpdate();
    }

    @Override
    public void onInactive() {
        super.onInactive();
        UIFlowerTool.get().removeListener(this);
    }

    @Override
    public void onFlowerSelected(FlowerData data) {
        List<FlowerModel> flowers = model.getFlowers();
        for (int i = 0; i < flowers.size(); i++) {
            if (flowers.get(i).getFlowerData().record.id == data.record.id) {
                flowerParam.setValue(i);
                return;
            }
        }
    }

    @Override
    public void onUpdate() {
        List<PanelRecord> panels = model.getPanels();
        String[] panelIds = new String[panels.size() + 1];
        panelIds[0] = OPTION_ALL;
        for (int i = 0; i < panels.size(); i++) {
            panelIds[i + 1] = panels.get(i).id;
        }
        panel.setOptions(panelIds);

        HashSet<Integer> pixliteIdSet = new HashSet<>();
        for (FlowerModel fm : model.getFlowers()) {
            pixliteIdSet.add(fm.getFlowerData().record.pixliteId);
        }
        List<Integer> pixliteIds = new ArrayList<>(pixliteIdSet);
        Collections.sort(pixliteIds);
        String[] pixliteOptions = new String[pixliteIds.size() + 1];
        pixliteOptions[0] = OPTION_ALL;
        for (int i = 0; i < pixliteIds.size(); i++) {
            pixliteOptions[i + 1] = Integer.toString(pixliteIds.get(i));
        }
        pixlite.setOptions(pixliteOptions);
    }
}
